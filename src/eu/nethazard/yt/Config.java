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

	public static boolean VERBOSE = true;
	
	public static boolean OVERWRITE_EXISTING_FILES = true;
	
	public static boolean REMOVE_TRACKS_AFTER_CONVERTING = true;
	public static boolean REMOVE_TRACKS_AFTER_MUXING = true;
	
	public final static String GUI_VERSION = "1.1.0";
	public final static String LIBRARY_VERSION = "1.0.4";

	/**
	 * ITAGS_MUXED
	 *
  	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
  	 *
 	 * '13': {'ext': '3gp'},									????
 	 *
 	 * '17': {'ext': '3gp',  'width': 176,  'height': 144},
 	 *
 	 * '36': {'ext': '3gp',  'width': 320,  'height': 240},
 	 *  '5': {'ext': 'flv',  'width': 400,  'height': 240},
 	 *
 	 *  '6': {'ext': 'flv',  'width': 450,  'height': 270},
 	 *
 	 * '34': {'ext': 'flv',  'width': 640,  'height': 360},
 	 * '43': {'ext': 'webm', 'width': 640,  'height': 360},
 	 * '18': {'ext': 'mp4',  'width': 640,  'height': 360},
 	 *
 	 * '35': {'ext': 'flv',  'width': 854,  'height': 480},
 	 * '44': {'ext': 'webm', 'width': 854,  'height': 480},
 	 * '59': {'ext': 'mp4',  'width': 854,  'height': 480},
 	 * '78': {'ext': 'mp4',  'width': 854,  'height': 480},
 	 *
 	 * '45': {'ext': 'webm', 'width': 1280, 'height': 720},
 	 * '22': {'ext': 'mp4',  'width': 1280, 'height': 720},
 	 *
 	 * '46': {'ext': 'webm', 'width': 1920, 'height': 1080},
 	 * '37': {'ext': 'mp4',  'width': 1920, 'height': 1080},
 	 *
 	 * '38': {'ext': 'mp4',  'width': 4096, 'height': 3072},
 	 */
	public static final int[] ITAGS_MUXED = new int[]{38, 37, 46, 22, 45, 78, 59, 44, 35, 18, 43, 34, 6, 5, 36, 17, 13};

	/**
	 * ITAGS_3D
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
     * '100': {'ext': 'webm', 'height': 360,  'format_note': '3D', 'preference': -20},
     *  '82': {'ext': 'mp4',  'height': 360,  'format_note': '3D', 'preference': -20},
     *
     * '101': {'ext': 'webm', 'height': 480,  'format_note': '3D', 'preference': -20},
     *  '83': {'ext': 'mp4',  'height': 480,  'format_note': '3D', 'preference': -20},
     *
     * '102': {'ext': 'webm', 'height': 720,  'format_note': '3D', 'preference': -20},
     *  '84': {'ext': 'mp4',  'height': 720,  'format_note': '3D', 'preference': -20},
     *
     *  '85': {'ext': 'mp4',  'height': 1080, 'format_note': '3D', 'preference': -20},
	 */
	public static final int[] ITAGS_3D = new int[]{85, 84, 102, 83, 101, 82, 100};

	/**
	 * ITAGS_AUDIO_MP4
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
	 * '139': {'ext': 'm4a', 'format_note': 'DASH audio', 'acodec': 'aac', 'vcodec': 'none', 'abr': 48,  'preference': -50, 'container': 'm4a_dash'},
	 * '140': {'ext': 'm4a', 'format_note': 'DASH audio', 'acodec': 'aac', 'vcodec': 'none', 'abr': 128, 'preference': -50, 'container': 'm4a_dash'},
	 * '141': {'ext': 'm4a', 'format_note': 'DASH audio', 'acodec': 'aac', 'vcodec': 'none', 'abr': 256, 'preference': -50, 'container': 'm4a_dash'},
	 */
	public static final int[] ITAGS_AUDIO_MP4 = new int[]{141, 140, 139};

	/**
	 * ITAGS_AUDIO_WEBM
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
	 * '171': {'ext': 'webm', 'vcodec': 'none', 'format_note': 'DASH audio', 'abr': 128, 'preference': -50},
	 * '172': {'ext': 'webm', 'vcodec': 'none', 'format_note': 'DASH audio', 'abr': 256, 'preference': -50},
	 */
	public static final int[] ITAGS_AUDIO_WEBM = new int[]{172, 171};

	/**
	 * ITAGS_AUDIO_WEBM_OPUS
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
	 * '249': {'ext': 'webm', 'vcodec': 'none', 'format_note': 'DASH audio', 'acodec': 'opus', 'abr': 50,  'preference': -50},
	 * '250': {'ext': 'webm', 'vcodec': 'none', 'format_note': 'DASH audio', 'acodec': 'opus', 'abr': 70,  'preference': -50},
	 * '251': {'ext': 'webm', 'vcodec': 'none', 'format_note': 'DASH audio', 'acodec': 'opus', 'abr': 160, 'preference': -50},
	 */
	public static final int[] ITAGS_AUDIO_WEBM_OPUS = new int[]{251, 250, 249};

	/**
	 * ITAGS_VIDEO_MP4
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
	 * '160': {'ext': 'mp4', 'height': 144,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '133': {'ext': 'mp4', 'height': 240,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '134': {'ext': 'mp4', 'height': 360,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '135': {'ext': 'mp4', 'height': 480,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 *
	 * '136': {'ext': 'mp4', 'height': 720,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '298': {'ext': 'mp4', 'height': 720,  'format_note': 'DASH video', 'acodec': 'none', 'preference': -40, 'fps': 60, 'vcodec': 'h264'},
	 *
	 * '137': {'ext': 'mp4', 'height': 1080, 'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '299': {'ext': 'mp4', 'height': 1080, 'format_note': 'DASH video', 'acodec': 'none', 'preference': -40, 'fps': 60, 'vcodec': 'h264'},
	 *
	 * '264': {'ext': 'mp4', 'height': 1440, 'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},
	 * '266': {'ext': 'mp4', 'height': 2160, 'format_note': 'DASH video', 'acodec': 'none', 'preference': -40,            'vcodec': 'h264'},
	 * '138': {'ext': 'mp4',                 'format_note': 'DASH video', 'acodec': 'none', 'preference': -40},  # Height can vary (https://github.com/rg3/youtube-dl/issues/4559)
	 */
	public static final int[] ITAGS_VIDEO_MP4 = new int[]{138, 266, 264, 299, 137, 298, 136, 135, 134, 133, 160};

	/**
	 * ITAGS_VIDEO_WEBM
	 *
	 * source 2015-10-13: https://github.com/rg3/youtube-dl/blob/master/youtube_dl/extractor/youtube.py
	 *
	 * '278': {'ext': 'webm', 'height': 144,                 'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp9', 'preference': -40},
	 *
	 * '242': {'ext': 'webm', 'height': 240,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 *
	 * '167': {'ext': 'webm', 'height': 360,  'width': 640,  'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '243': {'ext': 'webm', 'height': 360,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 *
	 * '168': {'ext': 'webm', 'height': 480,  'width': 854,  'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '218': {'ext': 'webm', 'height': 480,  'width': 854,  'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '219': {'ext': 'webm', 'height': 480,  'width': 854,  'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '244': {'ext': 'webm', 'height': 480,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '245': {'ext': 'webm', 'height': 480,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '246': {'ext': 'webm', 'height': 480,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},

	 * '169': {'ext': 'webm', 'height': 720,  'width': 1280, 'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '247': {'ext': 'webm', 'height': 720,                 'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '302': {'ext': 'webm', 'height': 720,                 'format_note': 'DASH video', 'acodec': 'none',                      'vcodec': 'vp9', 'preference': -40, 'fps': 60},
	 *
	 * '170': {'ext': 'webm', 'height': 1080, 'width': 1920, 'format_note': 'DASH video', 'acodec': 'none', 'container': 'webm', 'vcodec': 'vp8', 'preference': -40},
	 * '248': {'ext': 'webm', 'height': 1080,                'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '303': {'ext': 'webm', 'height': 1080,                'format_note': 'DASH video', 'acodec': 'none',                      'vcodec': 'vp9', 'preference': -40, 'fps': 60},
	 *
	 * '271': {'ext': 'webm', 'height': 1440,                'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '308': {'ext': 'webm', 'height': 1440,                'format_note': 'DASH video', 'acodec': 'none',                      'vcodec': 'vp9'  'preference': -40, 'fps': 60},
	 *
	 * '272': {'ext': 'webm', 'height': 2160,                'format_note': 'DASH video', 'acodec': 'none',                                       'preference': -40},
	 * '313': {'ext': 'webm', 'height': 2160,                'format_note': 'DASH video', 'acodec': 'none',                      'vcodec': 'vp9'  'preference': -40},
	 * '315': {'ext': 'webm', 'height': 2160,                'format_note': 'DASH video', 'acodec': 'none',                      'vcodec': 'vp9'  'preference': -40, 'fps': 60},
	 */
	public static final int[] ITAGS_VIDEO_WEBM = new int[]{315, 313, 272, 308, 271, 303, 248, 170, 302, 247, 169, 246, 246, 244, 219, 218, 168, 243, 167, 242, 278};
	
}
