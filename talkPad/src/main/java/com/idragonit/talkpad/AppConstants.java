package com.idragonit.talkpad;

import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.Handler;

public class AppConstants {
	public static Handler APP_HANDLER;
	public static Handler COMMAND_HANDLER;
	
	public static int APP_STATUS = 0;
	public static boolean APP_EXIT = false;
	
	public static int EDIT_MODE = 0;
	public static int SAVE_MODE = 0;
	
	public static String FILE_PATH = "";
	
	public static String FILE_MANAGER__LOCATION = "";
	
	public static int PICTURE_MODE = 0;
	public static String PICTURE_NAME = "";
	
	public static boolean IS_CREATED = false;
	public static boolean IS_OPENED = false;
	public static boolean IS_SAVED = false;
	
	public static AssetManager ASSET_MANAGER = null ;
	
	public static Point WINDOW = new Point(0, 0);
	public static int WINDOW_MARGIN = 50;
	
	public static int TEXTAREA_HEIGHT = 0;

	public static boolean LINE_NUMBER = true;
	public static String SPEECH_LANGUAGE = "en";
	
	public static void init(){
		APP_EXIT = false;
		
		APP_STATUS = 0;
		
		EDIT_MODE = 0;
		SAVE_MODE = 0;
		
		FILE_PATH = "";
		
		PICTURE_MODE = 0;
		PICTURE_NAME = "";
		
		IS_CREATED = false;
		IS_OPENED = false;
		IS_SAVED = false;
	}
}
