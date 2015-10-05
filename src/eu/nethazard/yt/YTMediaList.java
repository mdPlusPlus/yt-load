/**
 * @author Mathias Dickenscheid
 * @since 2014-08-31
 */
package eu.nethazard.yt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import eu.nethazard.yt.muxing.FFMPEGUtil;


public class YTMediaList {
	
	//TODO enforce https:// ?
	public static YTMediaList fromID(String youtubeID){
		String template = "https://www.youtube.com/watch?v=";
		return fromString(template + youtubeID);
	}
	
	public static YTMediaList fromString(String youtubeURL){
		try {
			return fromURL(new URL(youtubeURL));
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static YTMediaList fromURI(URI youtubeURI){
		try {
			return fromURL(youtubeURI.toURL());
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static YTMediaList fromURL(URL youtubeURL){
		YTMediaList obj = null;
		try {
			obj = new YTMediaList(youtubeURL);
		}
		catch(JSONException e){
			obj = null;
			System.err.println("video not available (GEMA?)");
			//e.printStackTrace(); //for debugging
		}
		catch(IOException e) {
			//encrypted content
			obj = null;
			e.printStackTrace();
		}
		return obj;
	}
	//
	
	private final String title;
	private final String youtubeID;
	private final int streamLength;
	private boolean encrypted;
	
	private final URL youtubeURL;
	private final String playerConfig;
	
	private List<YTMedia> ytMediaList;
	
	private YTMediaList(URL youtubeURL) throws IOException{
		this.youtubeURL = stripUrl(youtubeURL);
		ytMediaList = new ArrayList<YTMedia>();
		playerConfig 	= getPlayerConfigString();
		JSONObject json = new JSONObject(playerConfig).getJSONObject("args");
		title 			= json.getString("title");
		youtubeID 		= json.getString("video_id");
		streamLength 	= json.getInt("length_seconds");
		
		
		String dashmpd = json.getString("dashmpd"); // manifest URL
		String url_encoded_fmt_stream_map = json.getString("url_encoded_fmt_stream_map");
		String adaptive_fmts = json.getString("adaptive_fmts");
		
		encrypted = isManifestEncrypted(dashmpd);
		if(encrypted){
			throw new IOException("encrypted content: " + youtubeURL); //TODO EncryptedContentException ?
		}
		else{
			List<YTMedia> dashList = workDash(dashmpd);
			List<YTMedia> fmtsList = workFmts(url_encoded_fmt_stream_map);
			List<YTMedia> adaptiveFmtsList = workAdaptiveFmts(adaptive_fmts);
			
			/*
			//TODO remove debug code
			for(int i  = 0; i < dashList.size(); i++){
				System.out.println("dashList.get(" + i + "): " + dashList.get(i)); //TODO check verbosity
			}
			for(int i  = 0; i < fmtsList.size(); i++){
				System.out.println("fmtsList.get(" + i + "): " + fmtsList.get(i)); //TODO check verbosity
			}
			for(int i  = 0; i < adaptiveFmtsList.size(); i++){
				System.out.println("adaptiveFmtsList.get(" + i + "): " + adaptiveFmtsList.get(i)); //TODO check verbosity
			}
			*/
			
			ytMediaList.addAll(dashList);
			ytMediaList.addAll(fmtsList);
			ytMediaList.addAll(adaptiveFmtsList);
		}
	}
	
	//only for debugging
	public String getPlayerConfig(){
		return playerConfig;
	}
	
	public int getLength(){
		return streamLength;
	}
	
	public String getID(){
		return youtubeID;
	}
	
	public URL getURL(){
		return youtubeURL;
	}
	
	public String getTitle(){
		return title;
	}
	
	private URL stripUrl(URL toStrip) throws MalformedURLException{
		String urlBefore = toStrip.toString();
		String result;
		int ampIndex = urlBefore.indexOf("&");
		if(ampIndex != -1){
			result = urlBefore.substring(0, ampIndex);
		}
		else{
			result = urlBefore;
		}
		return new URL(result);
	}
	
	private String getPlayerConfigString(){
		String playerConfigString = null;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(youtubeURL.openStream()));
			List<String> lines = new ArrayList<String>();
			
			String line = br.readLine(); 
			while(line != null){
				lines.add(line.trim());
				line = br.readLine();
			}
			br.close();
			
			int playerStringIndex = -1;
			int currentIndex = 0;
			while((playerStringIndex == -1) && (currentIndex < lines.size())){
				if(lines.get(currentIndex).startsWith("<script>var ytplayer")){
					playerStringIndex = currentIndex;
				}
				currentIndex++;
			}
			
			if(playerStringIndex == -1){
				System.err.println("playerStringIndex == -1");
			}
			else{
				String removeStart = "<script>var ytplayer = ytplayer || {};ytplayer.config = ";
				String removeEnd = "</script>";
				
				String playerLine = lines.get(playerStringIndex);
				playerConfigString = playerLine.substring(removeStart.length(), playerLine.indexOf(removeEnd));
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return playerConfigString;
	}
	
	public boolean isEncrypted(){
		return encrypted;
	}
	
	private boolean isManifestEncrypted(String dashmpd) throws IOException{
		boolean encrypted = false;
		try{
			URL manifestXmlUrl = new URL(dashmpd);
			InputStream is = manifestXmlUrl.openStream();
			is.close();
		}
		catch(IOException e){
			if(e.getMessage().startsWith("Server returned HTTP response code: 403 for URL:")){
				encrypted = true;
			}
			else{
				throw e;
			}
		}
		return encrypted;
	}
	
	private List<YTMedia> workDash(String dashmpd) throws MalformedURLException, IOException {
		List<YTMedia> dashList = new ArrayList<YTMedia>();

		//download manifest XML
		StringBuilder dashManifestBuilder = new StringBuilder();
		BufferedReader dashManifestIS = new BufferedReader(new InputStreamReader(new URL(dashmpd).openStream()));
		char[] manifestBuffer = new char[4096];
		int readFromManifestIS;
		while((readFromManifestIS = dashManifestIS.read(manifestBuffer)) != -1){
			for(int i = 0; i<readFromManifestIS; i++){
				char toAppend = manifestBuffer[i];
				dashManifestBuilder.append(toAppend);
			}
		}
		dashManifestIS.close();
		String dashManifestXML = dashManifestBuilder.toString();

		//convert manifest XML to JSON
		JSONObject dashManifestJSON = XML.toJSONObject(dashManifestXML);
		//reduce to "MPD" key, reduce to "Period" key, get "AdaptationSet" array 
		JSONArray adaptationSets = dashManifestJSON.getJSONObject("MPD").getJSONObject("Period").getJSONArray("AdaptationSet");

		//add entries to list
		for(int i = 0; i < adaptationSets.length(); i++){
			//get "Representation" array
			JSONObject currentEntry = (JSONObject) adaptationSets.get(i);

			JSONArray arr = currentEntry.optJSONArray("Representation");
			if(arr != null){
				//is a JSONArray
				for(int j = 0; j<arr.length(); j++){
					JSONObject obj = arr.getJSONObject(j);
					addToDashList(dashList, obj);
				}
			}
			else{
				//is NOT a JSONArray
				JSONObject obj = currentEntry.optJSONObject("Representation");
				if(obj != null){
					addToDashList(dashList, obj);
				}
				else{
					//should not be necessary, but just in case
					System.err.println("AdaptationSet has no 'Representation' key (no JSONArray and no JSONObject)");
				}
			}
		}

		return dashList;
	}
	
	private void addToDashList(List<YTMedia> dashList,JSONObject obj) throws MalformedURLException {
		int dashItag = obj.getInt("id");
		String dashType = obj.getString("codecs"); //TODO may be confusing because we use "codecs" to fill "type"
		obj = obj.getJSONObject("BaseURL");
		URL dashURL = new URL(obj.getString("content"));
		YTMedia media = new YTMedia(title, dashItag, dashType, dashURL);
		dashList.add(media); 
	}
	
	private List<YTMedia> workFmts(String fmts) throws UnsupportedEncodingException, NumberFormatException, MalformedURLException{
		List<YTMedia> fmtsList = new ArrayList<YTMedia>();
		
		//to keep track of lines already read (5 lines form 1 entry for fmtsList
		int linesRead = 0;
		//holding the values during the reading of the 5 lines, cleared after
		Map<String, String> ytMediaAsMap = new TreeMap<String, String>();
		
		//split the string in single parameters
		String[] fmtsArr = fmts.split("&");
		for(int i = 0; i < fmtsArr.length; i++){
			String current = fmtsArr[i];
			//trimming whitespace for convenience with "startsWith()"
			current = current.trim();
			//split if it has multiple values
			String[] split = current.split(",");
			//check for every value
			for(int j = 0; j < split.length; j++){
				String temp = split[j];
				//again trimming for convenience
				temp = temp.trim();
				if(temp.startsWith("url=") || temp.startsWith("type=")){
					temp = URLDecoder.decode(temp, "UTF-8");
				}
				/*
				 * if the signature is in here, it means it is encrypted!
				 * for the time being, we ignore it, better is to abort or decrypt
				 * TODO: check out how to decrypt it 
				 */
				if(!temp.startsWith("s=")){
					String tempKey = temp.substring(0, temp.indexOf("="));
					String tempValue = temp.substring(temp.indexOf("=") + 1);
					ytMediaAsMap.put(tempKey, tempValue);
					linesRead++;
					//read five lines each time (type, itag, quality, url, fallback_host) -  not in order!
					if(linesRead == 5){
						YTMedia media = new YTMedia(title, Integer.parseInt(ytMediaAsMap.get("itag")), ytMediaAsMap.get("type"), new URL(ytMediaAsMap.get("url")));
						fmtsList.add(media);
						ytMediaAsMap.clear();
						linesRead = 0;
					}					
				}
			}
		}
		
		return fmtsList;
	}
	
	private List<YTMedia> workAdaptiveFmts(String adaptiveFmts) throws NumberFormatException, MalformedURLException, UnsupportedEncodingException{
		List<YTMedia> adaptiveFmtsList = new ArrayList<YTMedia>();
		
		//split string by "," -> one entry in adaptiveFtmsList each
		String[] commaSplit = adaptiveFmts.split(",");
		for(int i = 0; i < commaSplit.length; i++){
			Map<String, String> ytMediaAsMap = new TreeMap<String, String>();
			//split by "&" to get single key-value-pairs
			String[] ampSplit = commaSplit[i].split("&");
			for(int j = 0; j < ampSplit.length; j++){
				String temp = ampSplit[j];
				if(temp.startsWith("url=") || temp.startsWith("type=")){
					temp = URLDecoder.decode(temp, "UTF-8");
				}
				String tempKey = temp.substring(0, temp.indexOf("="));
				String tempValue = temp.substring(temp.indexOf("=") + 1);
				ytMediaAsMap.put(tempKey, tempValue);
			}
			YTMedia media = new YTMedia(title, Integer.parseInt(ytMediaAsMap.get("itag")), ytMediaAsMap.get("type"), new URL(ytMediaAsMap.get("url")));
			adaptiveFmtsList.add(media);
		}
		
		return adaptiveFmtsList;
	}
	
	public int getIndexFromItag(int itag){
		return getIndexFromItag(ytMediaList, itag);
	}
	
	private int getIndexFromItag(List<YTMedia> list, int itag){
		int index = -1;
		
		for(int i = 0; i < list.size(); i++){
			int currentItag = list.get(i).getItag();
			if(index == -1){
				//System.out.println("list.get(" + i +").getItag()=" + currentItag); //TODO check verbosity
				if(currentItag == itag){
					index = i;
				}
			}
			else{
				break;
			}
		}
		
		return index;
	}
	
	private YTMedia getFromFirstFoundItag(List<YTMedia> list, int[] prioItags){
		YTMedia result = null;
		
		int firstFoundIndex = -1;
		for(int i = 0; i < prioItags.length; i++){
			int currentItag = prioItags[i];
			if(firstFoundIndex == -1){
				firstFoundIndex = getIndexFromItag(list, currentItag);
			}
			else{
				result = list.get(firstFoundIndex);
				break;
			}
		}
		
		return result;
	}
	
	public List<YTMedia> getMuxedList(){
		List<YTMedia> muxedList = new ArrayList<>();
		
		for(int i = 0; i < ytMediaList.size(); i++){
			YTMedia current = ytMediaList.get(i);
			if(current.isContainer()){
				muxedList.add(current);
			}
		}
		
		return muxedList;
	}
	
	public List<YTMedia> getAudioList(){
		List<YTMedia> audioList = new ArrayList<>();
		
		for(int i = 0; i < ytMediaList.size(); i++){
			YTMedia current = ytMediaList.get(i);
			if(!current.isContainer()){
				if(current.getType().startsWith("audio/")){
					audioList.add(current);
				}
			}
		}
		
		return audioList;
	}
	
	public List<YTMedia> getVideoList(){
		List<YTMedia> videoList = new ArrayList<>();
		
		for(int i = 0; i < ytMediaList.size(); i++){
			YTMedia current = ytMediaList.get(i);
			if(!current.isContainer()){
				if(current.getType().startsWith("video/")){
					videoList.add(current);
				}
			}
		}
		
		return videoList;
	}
	
	public YTMedia getBestMuxed(){
		/*
		 * source: http://users.ohiohills.com/fmacall/YTCRACK.TXT
		 * itag=  video  resolution/bitrate
		 * value  type    ( w x h )  flags
		 * =====  =====  ==================
		 *   38    MP4   2048 x 1080
		 *   37    MP4   1920 x 1080
		 *   46    WEB   1920 x 1080 
		 *   22    MP4   1280 x 720
		 *   45    WEB   1280 x 720
		 *   35    FLV    640 x 480
		 *   44    WEB    640 x 480
		 *   18    MP4    480 x 360
		 *   34    FLV    480 x 360
		 *   43    WEB    480 x 360
		 *    5    FLV    320 x 240
		 *   36    3GP    320 x 240
		 *   17    3GP    176 x 144
		 *   
		 *   even more on http://www.jwz.org/hacks/youtubedown
		 */
		int[] muxedPrioItags = new int[]{38, 37, 46, 22, 45, 35, 44, 18, 34, 43, 5, 36, 17};
		
		return getFromFirstFoundItag(getMuxedList(), muxedPrioItags);
	}
	
	public YTMedia getBestAudio(){
		//only mp4 audio for muxing! (not vorbis, webm and others)
		int[] audioPrioItags = new int[] {141, 140, 139};
		//TODO are there more mp4 audio itags?
		return getFromFirstFoundItag(getAudioList(), audioPrioItags);
	}
	
	public YTMedia getBestVideo(){
		//only mp4 video for muxing! (not webm and others)
		/*
		 * 266,138 = 4k
		 * 264 = 2k
		 * (266, 299, and 298 are new, experimental)
		 * (298, 299 -> 60 fps)
		 */
		int[] videoPrioItags = new int[] {266, 138, 264, 299, 137, 298, 136, 135, 134, 133, 160};
		//TODO are there more mp4 video itags?
		return getFromFirstFoundItag(getVideoList(), videoPrioItags);
	}
	
	public String downloadAndMuxBest(String targetDir) throws FileNotFoundException, IOException{
		YTMedia bestVideo = getBestVideo();
		YTMedia bestAudio = getBestAudio();
		
		String ext = "mp4"; //hardcoded mp4 for now 
		String path = targetDir + File.separator + YTMediaUtil.cleanTitle(title) + "(" + bestVideo.getItag() + "_" + bestAudio.getItag() + ")" + "." + ext;
		
		File f = new File(path);
		if(!f.exists() || (f.exists() && Config.OVERWRITE_EXISTING_FILES)){
			//download audio and video stream
			String videoPath = bestVideo.downloadTo(targetDir);
			String audioPath = bestAudio.downloadTo(targetDir);
			
			//mux
			File videoFile = new File(videoPath);
			File audioFile = new File(audioPath);			
			
			try {
				boolean successful = FFMPEGUtil.mux(videoFile, audioFile, f);
				if(!successful){
					path = null;
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				path = null;
			}
		}
		else{
			System.err.println("file already exists: " + f.getAbsolutePath());
		}
		
		
		return path;
	}

	public List<Integer> getAvailableItags(){
		List<Integer> itagList = new LinkedList<Integer>();
		
		//first into a set so we eliminate potential duplicate entries
		Set<Integer> tempSet = new TreeSet<Integer>();
		for(YTMedia m : ytMediaList){
			tempSet.add(m.getItag());
		}
		//then into the list
		for(Integer itag : tempSet){
			itagList.add(itag);
		}
		//no .sort() necessary because TreeSet is ordered

		return itagList;
	}
	
	public void printAvailableItags(){
		List<Integer> itagList = this.getAvailableItags();
		StringBuilder sb = new StringBuilder();
		sb.append("available itags [");
		sb.append(itagList.size());
		sb.append("]: ");
		for(int i = 0; i < itagList.size(); i++){
			Integer current = itagList.get(i);
			sb.append(current.toString());
			if(!current.equals(itagList.get(itagList.size()-1))) {
				sb.append(", ");
			}
		}
		System.out.println(sb.toString());
	}
}
