package com.idragonit.talkpad.utils;

import android.graphics.Color;

public interface Constants {
	public final static String APP_NAME = "TALK PAD";
	
	public final static int ACTIVITY_RESULT__GALLERY = 1;
	public final static int ACTIVITY_RESULT__CAMERA = 2;
	public final static int ACTIVITY_RESULT__VOICE_SPEECH = 3;
	public final static int ACTIVITY_RESULT__VOICE_COMMAND = 4;
	
    public final static int BLANK_FRAGMENT = 0;
    public final static int NEW_FRAGMENT = 1;
    public final static int OPEN_FRAGMENT = 2;
    public final static int SAVE_FRAGMENT = 3;
    public final static int SAVE_AS_FRAGMENT = 4;
    public final static int RECENT_FRAGMENT = 5;
    public final static int EXIT_FRAGMENT = 6;
	public final static int ABOUT_FRAGMENT = 7;
	
	public final static int NEW_MODE = 0;
	public final static int OPEN_MODE = 1;
	
	public final static int NOT_SAVE_MODE = 0;
	public final static int SAVED_MODE = 1;
	
	public final static int ACTION_MENU = 1;
	public final static int ACTION_NEW_PAD = 2;
	public final static int ACTION_OPEN_PAD = 3;
	public final static int ACTION_GALLERY = 4;
	public final static int ACTION_CAMERA = 5;
	public final static int ACTION_SPEECH = 100;
	public final static int ACTION_COMMAND = 101;
	public final static int ACTION_FONTNAME = 6;
	public final static int ACTION_FONTSIZE = 7;
	public final static int ACTION_FONTCOLOR = 8;
	public final static int ACTION_TEXT_COPY = 9;
	public final static int ACTION_TEXT_CUT = 10;
	public final static int ACTION_TEXT_PASTE = 11;
	public final static int ACTION_TEXT_DELETE = 12;
	
	public final static int ACTION_DELAY_TIME = 50;
	
	public static final String FILE_EXTENTION = ".talkpad";

	public static final String PREFERENCE_BACKUP_FILE = "talkpad_backup";
	public static final String PREFERENCE_RECENT_FILE = "recent_talkpad_";
	public static final String PREFERENCE_RECENT_COUNT = "recent_talkpad_count";
	public static final String PREFERENCE_LINENUMBER = "talkpad_linenumber";
	public static final String PREFERENCE_SPEECHLANGUAGE = "talkpad_speechlanguage";
	
	public static final int TYPEFACE_BOLD = 1001;
	public static final int TYPEFACE_ITALIC = 1002;
	public static final int TYPEFACE_UNDERLINE = 1003;
	
	public static final int DEFAULT_TEXT_SIZE = 24;
	public static final String DEFAULT_FONT_NAME = "";
	public static final int DEFAULT_COLOR = Color.rgb(0, 0, 0);
	
	public static final String DEFAULT_SPEECHLANGUAGE = "en-US";
	public static final boolean DEFAULT_LINENUMBER = true;
	
	public static final int ALIGNMENT_LEFT = 0;
	public static final int ALIGNMENT_CENTER = 1;
	public static final int ALIGNMENT_RIGHT = 2;
	
	public static final int RECENT_FILE_COUNT = 20;
	public static final int STACK_MAX_SIZE = 30;
	
	public static final String VOICE_DATA = "voice";
	
	public static final int COMMAND__SELECT_BUTTON = 1;
	public static final int COMMAND__SELECT_BUTTON__COMBINE = 2;
	public static final int COMMAND__SELECT_BUTTON__COMPLEX = 3;
	
	public static final int COMMAND__SELECT_MENU = 100;
	public static final int COMMAND__SELECT_MENU__COMBINE = 101;
	public static final int COMMAND__SELECT_MENU__COMPLEX = 102;
	
	public static final int COMMAND__FUNCTION = 200;
	public static final int COMMAND__FUNCTION__COMBINE = 201;
	public static final int COMMAND__FUNCTION__COMPLEX = 202;
	
	public static final int COMMAND__SYMBOL = 1000;
	
}
