/**
 * @author Mathias Dickenscheid
 * @since 2014-08-31
 * @version 1.0
 */
package eu.nethazard.yt.muxing;

import java.io.File;
import java.io.IOException;

import eu.nethazard.yt.Config;

public class FFMPEGUtil {
	
	public static boolean convertToMp3(File audioFile, File target) throws IOException, InterruptedException{
		if (Config.VERBOSE) {
			System.out.println("convertToMp3(...)");
		}

		boolean result = false;
		
		File ffmpegBin = getFfmpegBin();
		if(ffmpegBin != null && audioFile != null && audioFile.canRead()){
			String ffmpeg = ffmpegBin.getAbsolutePath();
			String audio = audioFile.getAbsolutePath();
			
			//TODO only 128kbit/s CBR for now
			String[] command = new String[]{ffmpeg, "-i", audio, "-f", "mp3", target.getAbsolutePath()}; 
			
			if(target.exists() && Config.OVERWRITE_EXISTING_FILES){
				target.delete();
			}
			
			if(!target.exists()){
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
				pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				pb.redirectError(ProcessBuilder.Redirect.INHERIT);

				Process p = pb.start();
				if(Config.VERBOSE) {
					System.out.println("Converting started ...");
				}
				p.waitFor();
				if(Config.VERBOSE) {
					System.out.println("Converting finished.");
				}
				if(Config.REMOVE_TRACKS_AFTER_CONVERTING){
					audioFile.delete();
				}
				result = true;
			}
			else{
				result = false;
				System.err.println("file " + target.getAbsolutePath() + " already exists");
			}
			
		}
		return result;
	}
	
	public static boolean mux(File videoFile, File audioFile, File target) throws IOException, InterruptedException{
		if (Config.VERBOSE) {
			System.out.println("mux(...)");
		}

		boolean result = false;
		
		File ffmpegBin = getFfmpegBin();
		if(ffmpegBin != null && videoFile.canRead() && audioFile.canRead()){
			String ffmpeg = ffmpegBin.getAbsolutePath();
			String video = videoFile.getAbsolutePath();
			String audio = audioFile.getAbsolutePath();
			
			String[] command = new String[]{ffmpeg, "-i", video, "-i", audio, "-c", "copy", "-shortest", target.getAbsolutePath()};
			
			if(target.exists() && Config.OVERWRITE_EXISTING_FILES){
				target.delete();
			}
			
			if(!target.exists()){
				ProcessBuilder pb = new ProcessBuilder(command);
				pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
				pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				pb.redirectError(ProcessBuilder.Redirect.INHERIT);

				Process p = pb.start();
				if(Config.VERBOSE) {
					System.out.println("Muxing started ...");
				}

				p.waitFor();
				if(Config.VERBOSE) {
					System.out.println("Muxing finished.");
				}

				if(Config.REMOVE_TRACKS_AFTER_MUXING){
					videoFile.delete();
					audioFile.delete();
				}
				result = true;
			}
			else{
				result = false;
				System.err.println("file " + target.getAbsolutePath() + " already exists");
			}
		}
		else{
			result = false;
		}
		return result;
	}
	
	private static File getFfmpegBin(){
		File result = null;
		
		//1. is ffmpeg already installed? (check PATH)
		String ffmpegLocation = getFfmpegInstallLocation();
		if(ffmpegLocation != null){
			result = new File(ffmpegLocation);
		}
		else{
			//2. check os
			String os = getOS();
			String arch = getArch();
			if(os.equals("solaris") || os.equals("UNKNOWN") || arch.equals("UNKNOWN")){
				System.err.println("OS not supported - OS is: " + getOSString());
			}
			else{
				ffmpegLocation = "res" + File.separator + "ffmpeg" + File.separator + getOSString() + File.separator + "ffmpeg"; //TODO change so we can put into .jar
				if(os.equals("win")){
					ffmpegLocation = ffmpegLocation + ".exe";
				}
				File temp = new File(ffmpegLocation);
				if(temp.exists() && temp.isFile() && temp.canRead()){
					result = temp;
				}
				else{
					System.err.println("something went wrong while locating ffmpeg");
				}
			}
		}
		return result;
	}
	
	private static String getFfmpegInstallLocation(){
		String location = null;
		
		String pathEnv = System.getenv("PATH");
		String[] pathEnvSplit = pathEnv.split(File.pathSeparator);
		for(int i = 0; i < pathEnvSplit.length; i++){
			File check = new File(pathEnvSplit[i]);
			if(location == null){
				if(check.isFile()){
					//if ffmpeg binary is hardcoded into PATH
					if(check.getPath().endsWith(File.separator + "ffmpeg") || check.getPath().endsWith(File.separator + "ffmpeg.exe")){
						location = check.getAbsolutePath();
					}
				}
				else if(check.isDirectory()){
					File[] dirEntries = check.listFiles();
					for(int j = 0; j < dirEntries.length; j++){
						File current = dirEntries[j];
						if(current.isFile()){
							if(check.getPath().endsWith(File.separator + "ffmpeg") || check.getPath().endsWith(File.separator + "ffmpeg.exe")){
								location = check.getAbsolutePath();
							}
						}
					}
				}
				else{
					//not file, not dir, what now?
				}
			}
			else{
				break;
			}
		}
		
		return location;
	}
	
	private static String getOSString(){
		String os = getOS();
		String arch = getArch();
		return os + arch;
	}
	
	private static String getOS(){
		String os;
		
		String osName = System.getProperty("os.name").toLowerCase();
		if(osName.indexOf("win") >= 0){
			os = "win";
		}
		else if(osName.indexOf("mac") >= 0){
			os = "osx";
		}
		else if(osName.indexOf("nix") >= 0){
			os = "lin";
		}
		else if(osName.indexOf("nux") >= 0){
			os = "lin";
		}
		else if(osName.indexOf("aix") >= 0){
			os = "lin";
		}
		else if(osName.indexOf("sunos") >= 0){
			os = "solaris";
		}
		else{
			os = "UNKNOWN";
		}
		
		return os;
	}
	
	private static String getArch(){
		String arch;
		
		String osArch = System.getProperty("os.arch");
		if(osArch.contains("64")){
			arch = "64";
		}
		else if(osArch.contains("32")){
			arch = "32";
		}
		else if(osArch.contains("86")){
			arch = "32";
		}
		else{
			arch = "UNKNOWN";
		}
		
		return arch;
	}
}
