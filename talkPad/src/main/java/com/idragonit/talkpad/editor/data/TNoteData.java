package com.idragonit.talkpad.editor.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;

import com.idragonit.talkpad.editor.TNoteContentBuilder;
import com.idragonit.talkpad.editor.TNoteEditText;
import com.idragonit.talkpad.editor.TNoteMetaContentHandler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class TNoteData implements Parcelable {

    public static int a = 1000;
    public long mCreateTime; //h
    public long mUpdateTime; //i
    public String mUUID; //e
    public String mTitle; //g
    public String mBody; //f
    public String mHtmlNote; //n
    public boolean mTemp;
    
    public int mStart;
    public int mEnd;

    public TNoteData() {

        mCreateTime = -1;
        mUpdateTime = -1;
        mUUID = "";
        mBody = "";
        mTitle = "";
        mHtmlNote = "";
        mTemp = false;
        
        mStart = 0;
        mEnd = 0;
    }

    public static TNoteData newNote(String uuid) { //a

        TNoteData note = new TNoteData();

        if (uuid != null && UUID.fromString(uuid) == null)
            uuid = TNoteDataUtil.genRandomUUID();

        if (uuid == null)
            uuid = TNoteDataUtil.genRandomUUID();

        note.mUUID = uuid;
        note.mCreateTime = System.currentTimeMillis();
        note.mUpdateTime = System.currentTimeMillis();

        TNoteDataUtil.makeDir(TNoteDataUtil.getNotePath(note.mUUID));

        return note;
    }

    public boolean saveToFile(String filename) { //d

        FileOutputStream fileoutputstream = null;

        try {
            fileoutputstream = new FileOutputStream(filename);
            fileoutputstream.write(mHtmlNote.getBytes("utf-8"));
            fileoutputstream.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public final boolean loadFromFile(String filename) { //c

        mHtmlNote = TNoteDataUtil.getFileContent(filename);

        if (mHtmlNote == null)
            return false;

        HashMap hashmap = TNoteMetaContentHandler.getMetaContentMap(mHtmlNote);

        mBody = ((String) hashmap.get("body")).replace("\uFFFC", "").replace("\n", " ");
        mTitle = (String) hashmap.get("title");

        String strCreateTime = (String) hashmap.get("create-time");
        if (strCreateTime != null)
            mCreateTime = Long.parseLong(strCreateTime);

        String strUpdateTime = (String) hashmap.get("update-time");
        if (strUpdateTime != null)
            mUpdateTime = Long.parseLong(strUpdateTime);

        String strUUID = (String) hashmap.get("uuid");
        if (strUUID != null)
            mUUID = strUUID;
        else
            mUUID = TNoteDataUtil.genRandomUUID();

        String strStart = (String) hashmap.get("cursor-start");
        if (strStart != null)
            mStart = Integer.parseInt(strStart);
        
        String strEnd = (String) hashmap.get("cursor-end");
        if (strEnd != null)
            mEnd = Integer.parseInt(strEnd);
        
        return true;
    }

    public final void saveToFile(TNoteEditText editText, String filename) {
        mTemp = true;
        mHtmlNote = editText.getHtmlString(mTitle, mCreateTime, mUpdateTime, mUUID);
        saveToFile(filename);
    }

    public final void loadFromFile(TNoteEditText editText, String filename) {
        loadFromFile(filename);

        SpannableStringBuilder ssb = (SpannableStringBuilder) TNoteContentBuilder.getSpannableFromContent(mHtmlNote);

        ssb = editText.getNoteEditContentFrom(getNotePath(), ssb);
        editText.setText(ssb);
    }

    public final void loadTempFile(TNoteEditText editText) {
        if (mTemp) {
            SpannableStringBuilder ssb = (SpannableStringBuilder) TNoteContentBuilder.getSpannableFromContent(mHtmlNote);
            ssb = editText.getNoteEditContentFrom(getNotePath(), ssb);
            editText.setText(ssb);
        }

        mTemp = false;
    }

    public final String getNotePath() { //b
        return TNoteDataUtil.getNotePath(mUUID);
    }

    public final String getHtmlNote() { //c
        return mHtmlNote;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i1) {
    }

	public void loadFromHtml(TNoteEditText editText, String tEMP_NOTES) {
		// TODO Auto-generated method stub
		mHtmlNote = tEMP_NOTES;
		
        if (mHtmlNote == null)
            return;

        HashMap hashmap = TNoteMetaContentHandler.getMetaContentMap(mHtmlNote);

        mBody = ((String) hashmap.get("body")).replace("\uFFFC", "").replace("\n", " ");
        mTitle = (String) hashmap.get("title");

        String strCreateTime = (String) hashmap.get("create-time");
        if (strCreateTime != null)
            mCreateTime = Long.parseLong(strCreateTime);

        String strUpdateTime = (String) hashmap.get("update-time");
        if (strUpdateTime != null)
            mUpdateTime = Long.parseLong(strUpdateTime);

        String strUUID = (String) hashmap.get("uuid");
        if (strUUID != null)
            mUUID = strUUID;
        else
            mUUID = TNoteDataUtil.genRandomUUID();

        String strStart = (String) hashmap.get("cursor-start");
        if (strStart != null)
            mStart = Integer.parseInt(strStart);
        
        String strEnd = (String) hashmap.get("cursor-end");
        if (strEnd != null)
            mEnd = Integer.parseInt(strEnd);
        
        SpannableStringBuilder ssb = (SpannableStringBuilder) TNoteContentBuilder.getSpannableFromContent(mHtmlNote);

        ssb = editText.getNoteEditContentFrom(getNotePath(), ssb);
        editText.setText(ssb);
        
        try{
        	editText.setSelection(mStart, mEnd);
        }catch (Exception e){
        	e.printStackTrace();
        }
	}

	public String getHtmlNote(TNoteEditText mNoteEdit) {
		// TODO Auto-generated method stub
		mHtmlNote = mNoteEdit.getHtmlString(mTitle, mCreateTime, mUpdateTime, mUUID);
		return mHtmlNote;
	}
}
