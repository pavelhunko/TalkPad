package com.idragonit.talkpad.utils;

import java.util.ArrayList;

public class Stack implements Constants{
	private static String DEFAULT = "";
	private static int current = -1;
	private static ArrayList<String> list = new ArrayList<String>();
	
	public static void add(String content){
		if (current!=-1 && current+1!=list.size()){
			for (int i=current+1; i<list.size(); i++)
				list.remove(i);
		}
		
//		if (list.size()==STACK_MAX_SIZE){
//			list.remove(0);
//			current--;
//		}
		
		if (current<list.size()){
			current++;
			list.add(current, content);		
		}
	}
	
	public static void clear(){
		list.clear();
		current = -1;
	}
	
	public static String undo(){
		if (current==-1)
			return DEFAULT;
		
		current--;
		if (current<0 || current>=list.size()){
			current++;
			return DEFAULT;
		}
		
		String result = list.get(current);
		
		return result;
	}
	
	public static String redo(){
		if (current==-1)
			return DEFAULT;
		
		current++;
		if (current<0 || current>=list.size()){
			current--;
			return DEFAULT;
		}
		
		String result = list.get(current);
		return result;
	}
	
	public static void setEmptyString(String str){
		DEFAULT = str;
	}
	
}
