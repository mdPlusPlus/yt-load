/**
 * @author Mathias Dickenscheid
 * @since 2014-08-31
 */
package eu.nethazard.yt;

import java.net.MalformedURLException;
import java.net.URL;

public class YTMediaUtil {
	
	public static String cleanTitle(String title){
		//TODO regex
		//TODO out-source to config
		
		String cleanTitle = title;
		
		StringBuilder allowedCharsBuilder = new StringBuilder();
		allowedCharsBuilder.append("0123456789");
		allowedCharsBuilder.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		allowedCharsBuilder.append("abcdefghijklmnopqrstuvwxyz");
		allowedCharsBuilder.append("ÄÖÜ");
		allowedCharsBuilder.append("äöü");
		allowedCharsBuilder.append("ß");
		String allowedChars = allowedCharsBuilder.toString();
		
		for(int i = 0; i < cleanTitle.length(); i++){
			char currentChar = cleanTitle.charAt(i);
			if(!allowedChars.contains(String.valueOf(currentChar))){
				cleanTitle = cleanTitle.replace(currentChar, '_');
			}
		}
		
		return cleanTitle;
	}
	
	public static boolean isYouTubeURL(String str){
		boolean result = false;
		
		boolean isValidURL = true;
		try {
			new URL(str);
		}
		catch (MalformedURLException e) {
			isValidURL = false;
		}
		
		if(isValidURL){
			if(	   str.startsWith("https://www.youtube.com/watch?v=")
				|| str.startsWith("http://www.youtube.com/watch?v=") 
				|| str.startsWith("www.youtube.com/watch?v=")
				|| str.startsWith("youtube.com/watch?v=")){
				result = true;
			}
		}
		
		return result;
	}
	
	public static String convertSeconds(int secs){
		int remaining = secs;
		
		int hours = remaining / 3600;
		remaining = remaining % 3600;
		int minutes = remaining / 60;
		remaining = remaining % 60;
		int seconds =  remaining;
		
		String hh = String.valueOf(hours);
		if(hh.length() == 1){
			hh = "0" +  hh;
		}
		
		String mm = String.valueOf(minutes);
		if(mm.length() == 1){
			mm = "0" +  mm;
		}
		
		String ss = String.valueOf(seconds);
		if(ss.length() == 1){
			ss = "0" +  ss;
		}
		
		String formatted = hh + ":" + mm + ":" + ss;
		
		return formatted;
	}
}
