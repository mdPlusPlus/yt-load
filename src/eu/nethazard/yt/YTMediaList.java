/**
 * @author Mathias Dickenscheid
 * @since 2014-08-31
 */
package eu.nethazard.yt;

import eu.nethazard.yt.muxing.FFMPEGUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;


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
		ytMediaList 	= new ArrayList<YTMedia>();
		playerConfig 	= getPlayerConfigString(); //TODO could be null when index is -1
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

			if(playerStringIndex == -1){ //TODO when age-gated try https://www.youtube.com/embed/VIDEO_ID
				System.err.println("playerStringIndex == -1");
				System.err.println("YouTube: \"Melde dich an, um dein Alter zu bestätigen\"");
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
		return getFromFirstFoundItag(getMuxedList(), Config.ITAGS_MUXED);
	}
	
	public YTMedia getBestAudio(){
		//only mp4 audio for muxing! (not webm and others)
		return getFromFirstFoundItag(getAudioList(), Config.ITAGS_AUDIO_MP4);
	}
	
	public YTMedia getBestVideo(){
		//only mp4 video for muxing! (not webm and others)
		return getFromFirstFoundItag(getVideoList(), Config.ITAGS_VIDEO_MP4);
	}
	
	public String downloadAndMuxBest(String targetDir) throws FileNotFoundException, IOException {
		return downloadAndMuxIfPossible(targetDir, getBestVideo().getItag(), getBestAudio().getItag());
	}

	public String downloadAndMuxIfPossible(String targetDir, int videoItag, int audioItag) throws FileNotFoundException, IOException {
		YTMedia video = getYTMediaFromItag(videoItag);
		YTMedia audio = getYTMediaFromItag(audioItag);

		String ext = "";
		boolean videoOnly = false;
		boolean audioOnly = false;
		if(video != null && audio == null){
			//video only
			ext = "mp4";
			videoOnly = true;
		}
		if(video == null && audio != null){
			//audio only
			ext = "m4a";
			audioOnly = true;
		}
		if(video != null && audio != null){
			//video and audio
			ext = "mp4";
		}
		if(video == null && audio == null){
			//no YTMedia with according itag found
			return null;
		}


		//download audio and video stream
		String videoPath = "";
		if(videoOnly) {
			videoPath = video.downloadTo(targetDir);
			if(Config.VERBOSE) {
				System.out.println("videoPath: " + videoPath);
			}
			return videoPath;
		}

		String audioPath = "";
		if(audioOnly){
			audioPath = audio.downloadTo(targetDir);
			if(Config.VERBOSE) {
				System.out.println("audioPath: " + audioPath);
			}
			return audioPath;
		}

		if(!videoOnly && !audioOnly) {
			//mux
			String muxPath = targetDir + File.separator + YTMediaUtil.cleanTitle(title) + "(" + videoItag + "_" + audioItag + ")" + "." + ext;
			if(Config.VERBOSE) {
				System.out.println("muxPath: " + muxPath);
			}
			File muxFile = new File(muxPath);
			if(!muxFile.exists() || (muxFile.exists() && Config.OVERWRITE_EXISTING_FILES)){

				videoPath = video.downloadTo(targetDir);
				audioPath = audio.downloadTo(targetDir);
				if(Config.VERBOSE) {
					System.out.println("videoPath: " + videoPath);
					System.out.println("audioPath: " + audioPath);
				}
				File videoFile = new File(videoPath);
				File audioFile = new File(audioPath);

				try {
					boolean successful = FFMPEGUtil.mux(videoFile, audioFile, muxFile);
					if (!successful) {
						muxPath = null;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					muxPath = null;
				}

				return muxPath;
			}
			else{
				System.err.println("file already exists: " + muxFile.getAbsolutePath());
			}
		}

		return null;
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

	public List<Integer> getAvailableAudioMp4Itags(){
		List<Integer> audioMp4Itags = new LinkedList<Integer>();
		List<Integer> itagList = this.getAvailableItags();
		for(int i = 0; i < Config.ITAGS_AUDIO_MP4.length; i++) {
			if (itagList.contains(Config.ITAGS_AUDIO_MP4[i])){
				audioMp4Itags.add(Config.ITAGS_AUDIO_MP4[i]);
			}
		}

		return audioMp4Itags;
	}

	public List<Integer> getAvailableVideoMp4Itags(){
		List<Integer> videoMp4Itags = new LinkedList<Integer>();
		List<Integer> itagList = this.getAvailableItags();
		for(int i = 0; i < Config.ITAGS_VIDEO_MP4.length; i++) {
			if (itagList.contains(Config.ITAGS_VIDEO_MP4[i])){
				videoMp4Itags.add(Config.ITAGS_VIDEO_MP4[i]);
			}
		}

		return videoMp4Itags;
	}

	public YTMedia getYTMediaFromItag(int itag) {
		if(getAvailableItags().contains(itag)){
			Iterator<YTMedia> it = ytMediaList.iterator();
			while(it.hasNext()) {
				YTMedia current = it.next();
				if(current.getItag() == itag) {

					return current;
				}
			}

			return null;
		}
		else {

			return null;
		}

	}
}
