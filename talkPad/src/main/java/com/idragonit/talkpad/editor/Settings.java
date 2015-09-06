package com.idragonit.talkpad.editor;

import com.idragonit.talkpad.utils.Constants;

public class Settings implements Constants{

    public static String FONT_NAME = DEFAULT_FONT_NAME;

    public static int COLOR = DEFAULT_COLOR;

    public static boolean IS_BOLD = false;
    public static boolean IS_ITALIC = false;
    public static boolean IS_UNDERLINE = false;
    
    public static boolean IS_BULLET = false;
    public static boolean IS_NUMBERING = false;
    
    public static int ALIGNMENT = ALIGNMENT_LEFT;
    public static int TEXT_SIZE = DEFAULT_TEXT_SIZE;
    
    public static int SELECTION_START = 0;
    public static int SELECTION_END = 0;
    
    public static String TEMP_NOTES = "";
    public static String TEMP_TEXT = "";
    public static String BEFORE_NOTES = "";
    
    public static String OVERWRITE_FILE = "";
    
    public static boolean IS_LINE_START = false; 
    
    public static SpeechCommand COMMAND = new SpeechCommand();
    
    public static void init(){
    	FONT_NAME = DEFAULT_FONT_NAME;
    	COLOR = DEFAULT_COLOR;
    	IS_BOLD = false;
    	IS_ITALIC = false;
    	IS_UNDERLINE = false;
    	IS_BULLET = false;
    	IS_NUMBERING = false;
    	ALIGNMENT = ALIGNMENT_LEFT;
    	SELECTION_START = 0;
    	SELECTION_END = 0;
    	
    	TEMP_NOTES = "";
    	TEMP_TEXT = "";
    	BEFORE_NOTES = "";
    		
    	OVERWRITE_FILE = "";
    	
    	IS_LINE_START = false;
    }
}
