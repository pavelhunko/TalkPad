package com.idragonit.talkpad.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.utils.CommonMethods;
import com.idragonit.talkpad.utils.Constants;
import com.idragonit.talkpad.utils.Stack;

import fr.xgouchet.androidlib.ui.activity.AbstractBrowsingActivity;

/**
 *
 * @author pandadaro.senior
 * 2/12/2015
 *
 */
public class OpenFragment extends AbstractBrowsingActivity implements OnClickListener , Constants {

    public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle)
	{
		View view = layoutinflater.inflate(R.layout.fragment_open, viewgroup, false);
        view.findViewById(R.id.buttonCancel).setOnClickListener(this);

        mCurrentFolder = new File(AppConstants.FILE_MANAGER__LOCATION);
                
		return view;
	}

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            // navigate to parent folder
            File parent = mCurrentFolder.getParentFile();
            if ((parent != null) && (parent.exists())) {
                fillFolderView(parent);
                return true;
            }
        }
        return getActivity().onKeyUp(keyCode, event);
    }


    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        if (v.getId() == R.id.buttonCancel) {
        	AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
        }
    }

    protected boolean onFolderClick(File folder) {
        return true;
    }

    protected void onFolderViewFilled() {
    }

    protected void onFileClick(File file) {
        setOpenResult(file);
    }

    /**
     * Set the result of this activity to open a file
     *
     * @param file
     *            the file to return
     * @return if the result was set correctly
     */
    protected boolean setOpenResult(File file) {
    	if (!file.getAbsolutePath().endsWith(FILE_EXTENTION)){
            Toast.makeText(getActivity(), R.string.toast_intent_illegal, Toast.LENGTH_SHORT).show();
            return false;
    	}
    	
        if (!file.canRead()) {
            Toast.makeText(getActivity(), R.string.toast_file_cant_read, Toast.LENGTH_SHORT).show();
            return false;
        }

        AppConstants.EDIT_MODE = OPEN_MODE;
        AppConstants.SAVE_MODE = SAVED_MODE;
        AppConstants.FILE_PATH = file.getAbsolutePath();
        
        try{
        	SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(APP_NAME, getActivity().MODE_PRIVATE);
        	Editor editor = pref.edit();
        	
        	int recent = pref.getInt(PREFERENCE_RECENT_COUNT, 0);
        	int k=0;
        	
        	do {
        		k=0;
        		for (int i=1; i<=recent; i++){
        			String path = pref.getString(PREFERENCE_RECENT_FILE+i, "");
        			if (path.length()==0 || path.equals(AppConstants.FILE_PATH)){
        				k=i;
        				break;
        			}
        		}
        		
        		if (k!=0){
            		for (int i=k; i<recent; i++){
            			int v=i+1;
            			String path = pref.getString(PREFERENCE_RECENT_FILE+v, "");
            			editor.putString(PREFERENCE_RECENT_FILE+i, path);
            		}
            		
                	editor.commit();        		
            		recent--;
        		}
        	} while (k!=0);
        	
        	if (recent==RECENT_FILE_COUNT){
        		for (int i=1; i<recent; i++){
        			int v=i+1;
        			String path = pref.getString(PREFERENCE_RECENT_FILE+v, "");
        			editor.putString(PREFERENCE_RECENT_FILE+i, path);
        		}
        	} else {
        		recent++;
        	}
        	
        	editor.putInt(PREFERENCE_RECENT_COUNT, recent);
        	editor.putString(PREFERENCE_RECENT_FILE+recent, AppConstants.FILE_PATH);
        	editor.commit();
        }catch (Exception e){  }
        
        Settings.TEMP_NOTES = CommonMethods.readFile(AppConstants.FILE_PATH);
        Stack.clear();
        Stack.add(Settings.TEMP_NOTES);
        AppConstants.IS_OPENED = true;
        AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
        AppConstants.FILE_MANAGER__LOCATION = mCurrentFolder.getAbsolutePath(); 
        
        return true;
    }
    
}