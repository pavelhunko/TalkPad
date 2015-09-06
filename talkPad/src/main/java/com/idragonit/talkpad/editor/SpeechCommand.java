package com.idragonit.talkpad.editor;

import java.util.ArrayList;

import android.graphics.Color;

import com.idragonit.talkpad.R;
import com.idragonit.talkpad.utils.Constants;

public class SpeechCommand implements Constants{
	
	private final String[] COLOR_NAME = { "black", "orange", "red", "green", "blue", "aqua", "magenta", "yellow"};
	private final int[] COLORS = { Color.BLACK, Color.rgb(255, 165, 0), Color.RED, Color.GREEN, Color.BLUE, Color.rgb(0,255,255), Color.MAGENTA, Color.YELLOW };
	private final int[] FONT_SIZE = { 8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36, 48, 72 };
	private final String[] FONT_NAME = { "default", "times new roman", "arial", "georgia", "courier new", "verdana", "tahoma" };	
	private final String[] FONT_PATH = { "", "times.ttf", "arial.ttf", "georgia.ttf", "courier.ttf", "verdana.ttf", "tahoma.ttf" };	

	public ArrayList<Item> list = new ArrayList<SpeechCommand.Item>();
	
	public SpeechCommand() {
		// TODO Auto-generated constructor stub
		init();
	}
	
	private void init() {
		// TODO Auto-generated method stub
		list.add(new Item(COMMAND__SELECT_MENU, SAVE_FRAGMENT, "save"));
		list.add(new Item(COMMAND__SELECT_MENU, SAVE_AS_FRAGMENT, "save as"));
		list.add(new Item(COMMAND__SELECT_MENU, OPEN_FRAGMENT, "open"));
		list.add(new Item(COMMAND__SELECT_MENU, NEW_FRAGMENT, "new"));
		list.add(new Item(COMMAND__SELECT_MENU, EXIT_FRAGMENT, "exit"));
		list.add(new Item(COMMAND__SELECT_MENU, EXIT_FRAGMENT, "quit"));
		
		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_bold, "bold"));
		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_italic, "italic"));
		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_underline, "underline"));

		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_addphoto, "gallery"));
		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_takepicture, "camera"));

		list.add(new Item(COMMAND__SELECT_BUTTON__COMBINE, R.id.btn_alignleft, new String[]{ "left to right", "ltr" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMBINE, R.id.btn_alignright, new String[]{ "right to left", "rtl" } ));
		
		list.add(new Item(COMMAND__SELECT_BUTTON, R.id.btn_bullet, "bullet" ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMBINE, R.id.btn_numbering, new String[]{ "numeric", "numbering" } ));
		
		list.add(new Item(COMMAND__SELECT_BUTTON, ACTION_TEXT_COPY, "copy"));
		list.add(new Item(COMMAND__SELECT_BUTTON, ACTION_TEXT_CUT, "cut"));
		list.add(new Item(COMMAND__SELECT_BUTTON, ACTION_TEXT_PASTE, "paste"));
		list.add(new Item(COMMAND__SELECT_BUTTON, ACTION_TEXT_DELETE, "delete"));
		
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_changecase, new String[]{ "font", "name" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_adjustsize, new String[]{ "font", "size" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_changecolor, new String[]{ "font", "color" } ));

		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_changecase, new String[]{ "font", "change" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_adjustsize, new String[]{ "size", "change" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_changecolor, new String[]{ "color", "change" } ));
		
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_alignleft, new String[]{ "align", "left" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_alignright, new String[]{ "align", "right" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_aligncenter, new String[]{ "align", "center" } ));
		
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_indentdecrease, new String[]{ "indent", "decrease" } ));
		list.add(new Item(COMMAND__SELECT_BUTTON__COMPLEX, R.id.btn_indentincrease, new String[]{ "indent", "increase" } ));
		
	}
	
	public class Item {
		public int type;
		public int action;
		public int color;
		public int size;
		public int line1;
		public int line2;
		public String font;
		public boolean isStart;
		public boolean isEnd;
		
		public String[] command = null;
		public String[] command2 = null;
		
		public Item() {
			// TODO Auto-generated constructor stub
			init();
		}
		
		public Item(int type, int action, String command) {
			init();
			this.type = type;
			this.action = action;
			
			this.command = new String[1];
			this.command[0] = command;
		}
		
		public Item(int type, int action, String[] command) {
			init();
			this.type = type;
			this.action = action;
			
			this.command = new String[command.length];
			for (int i=0; i<command.length; i++){
				this.command[i] = command[i];
			}
		}
		
		public Item(int type, int action, String[] command, String[] command2) {
			init();
			this.type = type;
			this.action = action;
			
			this.command = new String[command.length];
			for (int i=0; i<command.length; i++){
				this.command[i] = command[i];
			}
			
			this.command2 = new String[command2.length];
			for (int i=0; i<command2.length; i++){
				this.command2[i] = command2[i];
			}
		}
		
		public void init(){
			this.color = -1;
			this.size = -1;
			this.line1 = -1;
			this.line2 = -1;
			this.isStart = false;
			this.isEnd = false;
			this.font = null;
		}
		
		private int isNumber(String speech){
			int result = -1;
			try{
				result = Integer.parseInt(speech);
			}catch (Exception e){
				result = -1;
			}
			
			return result;
		}
		
		private int recognizeNumber(String speech) {
			int result = -1;
			String[] words = speech.split(" ");
			
			for (int i=0; i<words.length; i++){
				int number = isNumber(words[i]);
				if (number>0){
					result = number;
					break;
				}
			}
			
			return result;
		}
		
		private String recognizeFont(String speech){
			for (int i=0; i<FONT_NAME.length; i++) {
				if (speech.indexOf(FONT_NAME[i])>-1) {
					return FONT_PATH[i];
				}
			}
			
			return null;
		}
		
		private boolean isValidSize(){
			for (int v : FONT_SIZE){
				if (v==size)
					return true;
			}
			
			return false;
		}
		
		public boolean check(String speech) {
			speech = speech.toLowerCase();
			boolean result = false;
			
			if (type == COMMAND__SELECT_BUTTON || type==COMMAND__SELECT_MENU || type==COMMAND__FUNCTION) {
				if (speech.indexOf(command[0])>-1) {
					result = true;
				}
			}
			
			if (type == COMMAND__SELECT_BUTTON__COMBINE || type==COMMAND__SELECT_MENU__COMBINE || type==COMMAND__FUNCTION__COMBINE) {
				for (int i=0; i<command.length; i++){
					if  (speech.indexOf(command[i])>-1){
						result = true;
						break;
					}
				}
			}
			
			if (type==COMMAND__SELECT_BUTTON__COMPLEX || type==COMMAND__SELECT_MENU__COMPLEX || type==COMMAND__FUNCTION__COMPLEX) {
				boolean find=true;
				for (int i=0; i<command.length; i++){
					if  (speech.indexOf(command[i])==-1){
						find = false;
						break;
					}
				}
				
				result = find;
			}
			
			if (result && action==R.id.btn_changecolor){
				for (int i=0; i<COLOR_NAME.length; i++){
					if (speech.indexOf(COLOR_NAME[i])>-1){
						color = COLORS[i];
						break;
					}
				}
			}
			
			if (result && action==R.id.btn_adjustsize){
				int s = speech.indexOf("size");
				int e = speech.indexOf("line");
				
				if (s!=-1 && e!=-1){
					String t = speech.substring(s, e);
					size = recognizeNumber(t);
					size = isValidSize() ? size : -1;
				}
			}
			
			if (result && action==R.id.btn_changecase){
				font = recognizeFont(speech);
			}
			
			if (result && (action!=R.id.btn_addphoto && action!=R.id.btn_takepicture) ) {
				int from_to_start = speech.indexOf("line");
				int from_to_end = speech.indexOf("through");
				
				if (from_to_start!=-1 && from_to_end!=-1) {
					String before = speech.substring(from_to_start, from_to_end);
					String after = speech.substring(from_to_end);
					
//					if (before.indexOf("start")>-1 || after.indexOf("start")>-1) {
//						isStart = true;
//					}
//						
//					if (before.indexOf("end")>-1 || after.indexOf("end")>-1) {
//						isEnd = true;
//					}
					
					line1 = recognizeNumber(before);
					line2 = recognizeNumber(after);
				} else if (from_to_start!=-1) {
					String before = speech.substring(from_to_start);
					line1 = recognizeNumber(before);
					line2 = line1;
				} else if (from_to_end!=-1) {
					String after = speech.substring(from_to_end);
					line2 = recognizeNumber(after);
					line1 = line2;
				}
				
				if (line1>line2){
					int w = line1;
					line1 = line2;
					line2 = w;
				}
				
				if (line1!=-1 && line2==-1){
					line2 = line1;
				}
			}
			
			return result;
		}
	}
}
