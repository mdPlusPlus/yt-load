/**
 * @author Mathias Dickenscheid
 * @since 2014-08-31
 */
package eu.nethazard.yt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import eu.nethazard.yt.muxing.FFMPEGUtil;

public class YTMedia {

	//TODO id
	private final String title;
	private final int itag;
	private final String type;
	private String[] codecs;
	private final URL url;
	private final boolean container;
	
	public YTMedia(String title, int itag, String type, URL url){
		this.title = title;
		this.itag = itag;
		this.url = url;
		
		if(type.contains(";")){
			//example: -->video/mp4; codecs="avc1.64001F, mp4a.40.2"<--
			this.type = type.substring(0, type.indexOf(";"));
			String toRemove = " codecs=\"";
			String codecsString = type.substring(type.indexOf(";") + toRemove.length()+1, type.length()-1);
			this.codecs = codecsString.split(",");
		}
		else{
			this.codecs = type.split(",");
			if(type.equals("video/x-flv")){	//weird behaviour
				this.type = "video/x-flv";
				this.codecs = new String[]{"unknown", "unknown"}; //{"flv1", "mpeg"/"mp3"} ?
			}
			else if(type.startsWith("avc")){
				this.type = "video/mp4";
			}
			else if(type.startsWith("mp4a")){
				this.type = "audio/mp4";
			}
			else if(type.equals("vorbis")){
				this.type = "audio/vorbis";
			}
			else if(type.equals("vp9")){
				this.type = "video/webm";
			}
			else{
				this.type = "unknown/" + type;
			}
		}
		
		if(codecs.length > 1){
			container = true;
		}
		else{
			container = false;
		}
	}
	
	public String getTitle(){
		return title;
	}

	public int getItag() {
		return itag;
	}

	public String getType() {
		return type;
	}
	
	public String getCodecs(){
		return convertCodecsToString();
	}
	
	private String convertCodecsToString(){
		StringBuilder codecsStringBuilder = new StringBuilder();
		
		codecsStringBuilder.append("{");
		for(int i = 0; i < codecs.length-1; i++){
			codecsStringBuilder.append(codecs[i] + ",");
		}
		codecsStringBuilder.append(codecs[codecs.length-1] + "}");
		
		return codecsStringBuilder.toString();
	}

	public URL getUrl() {
		return url;
	}
	
	public boolean isContainer(){
		return container;
	}
	
	public String toString(){
		return "YTMedia:" 
				+ " itag=" + itag
				+ " container=" + container
				+ " type=" + type
				+ " codecs=" + convertCodecsToString()
				+ " url=" + url;
	}
	
	public String downloadAndConvertToMp3(String targetDir) throws IOException, InterruptedException{
		String path = targetDir + File.separator + YTMediaUtil.cleanTitle(title) + "(" + itag + ")" + ".mp3";
		if(container || (!container && type.startsWith("audio"))){
			String in = downloadTo(targetDir);
			File inFile = new File(in);
			File outFile = new File(path);
			FFMPEGUtil.convertToMp3(inFile, outFile);
			
		}
		else{
			System.err.println("can't convert video-only file to mp3");
		}
		
		return path;
	}
	
	public String downloadTo(String targetDir){
		String ext = getFileExtension();
		String path = targetDir + File.separator + YTMediaUtil.cleanTitle(title) + "(" + itag + ")" + "." + ext;
		
		File f = new File(path);
		if(!f.exists() || (f.exists() && Config.OVERWRITE_EXISTING_FILES)){
			try {
				if(Config.VERBOSE){
					System.out.println("Downloading to " + f.getAbsolutePath());
				}

				BufferedInputStream in = new BufferedInputStream(url.openStream());
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(path));

				byte[] buffer = new byte[4096];
				int read;
				while((read = in.read(buffer)) != -1){
					out.write(buffer, 0, read);
				}
				
				in.close();
				out.close();

				if(Config.VERBOSE){
					System.out.println("Download finished");
				}
			}
			catch (IOException e) {
				path = "null";
				//TODO
				e.printStackTrace();
			}
		}
		else{
			System.err.println("file already exists: " + f.getAbsolutePath());
		}
		
		return path;
	}
	
	private String getFileExtension(){
		String ext = type.substring(type.indexOf("/") + 1);
		if(!container){
			String avType = type.substring(0, type.indexOf("/"));
			ext = ext + "_" + avType;
		}
		if(ext.startsWith("x-")){
			//example: x-flv
			ext = ext.substring(2);
		}
		return ext;
	}
	
}
