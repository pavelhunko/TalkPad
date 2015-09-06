package com.idragonit.talkpad.adapter;

public class FileInfo {
	String filename;
	String time;
	
	public FileInfo() {
		// TODO Auto-generated constructor stub
	}
	
	public FileInfo(String name, String t){
		filename = name;
		time = t;
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	
}
