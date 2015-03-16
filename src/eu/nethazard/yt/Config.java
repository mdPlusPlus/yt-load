/**
 * @author Mathias Dickenscheid
 * @since 2014-09-07
 * 
 * 2015-03-16 1.0.3 added missing itags
 * 2015-03-16 1.0.2 changed how the file name is cleaned
 * 2015-03-16 1.0.1 fix: single 'Representation' entries don't throw an exception anymore
 * 2014-09-07 1.0.0 initial release
 * 2014-08-31 x.x.x initial build
 */
package eu.nethazard.yt;

public class Config {
	
	public static boolean OVERWRITE_EXISTING_FILES = true;
	
	public static boolean REMOVE_TRACKS_AFTER_CONVERTING = true;
	public static boolean REMOVE_TRACKS_AFTER_MUXING = true;
	
	private final static String guiVersion = "1.0.0";
	private final static String libraryVersion = "1.0.3";
	
	public static String getGUIVersion(){
		return guiVersion;
	}
	
	public static String getLibraryVersion(){
		return libraryVersion;
	}
	
}
