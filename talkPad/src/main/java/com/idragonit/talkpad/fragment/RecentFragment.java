package com.idragonit.talkpad.fragment;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.adapter.FileInfo;
import com.idragonit.talkpad.adapter.FilesAdapter;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.utils.CommonMethods;
import com.idragonit.talkpad.utils.Constants;
import com.idragonit.talkpad.utils.Stack;

/**
 *
 * @author pandadaro.senior
 * 2/12/2015
 *
 */
public class RecentFragment extends Fragment implements OnClickListener , Constants {
	
	String[] FILES = null;

    public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle)
	{
		View view = layoutinflater.inflate(R.layout.fragment_recent, viewgroup, false);
        view.findViewById(R.id.buttonCancel).setOnClickListener(this);
        
        TextView txtComment = (TextView)view.findViewById(R.id.textComment);
        txtComment.setText(R.string.toast_recent);
        
		ListView list = (ListView) view.findViewById(R.id.fileList);

        try{
        	SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences(APP_NAME, getActivity().MODE_PRIVATE);
        	int recent = pref.getInt(PREFERENCE_RECENT_COUNT, 0);
        	if (recent>0) {
            	FILES = new String[recent];
            	
            	int j=0;
        		for (int i=recent; i>=1; i--){
        			String path = pref.getString(PREFERENCE_RECENT_FILE+i, "");
        			FILES[j++] = path;
        		}
        	}
        }catch (Exception e){  }
		
//		String[] menu = getResources().getStringArray(R.array.sidebar_menu);
        if (FILES==null || FILES.length==0) {
        	txtComment.setText(R.string.toast_no_recent);
        } else {
    		FilesAdapter adapter = new FilesAdapter(getActivity());
    		
    		for (int i=0; i<FILES.length; i++){
    			File file = new File(FILES[i]);
    			long time = file.lastModified();
    			
    			String filename = file.getName();
    			filename = filename.substring(0, filename.lastIndexOf(FILE_EXTENTION));
    			
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk-mm-ss");
    			adapter.add( new FileInfo(filename, sdf.format(new Date(time)) ) );
    		}
    		list.setAdapter(adapter);
    		
    		list.setOnItemClickListener(new OnItemClickListener() {
    			@Override
    			public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
    				setOpenResult(FILES[position]);
    			}
    		});
        }
        
		return view;
	}


    /**
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View v) {
        if (v.getId() == R.id.buttonCancel) {
        	AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
        }
    }

    private void onFileClick(String filename) {
        setOpenResult(filename);
    }

    /**
     * Set the result of this activity to open a file
     *
     * @param file
     *            the file to return
     * @return if the result was set correctly
     */
    protected boolean setOpenResult(String filename) {
    	File file = new File(filename);
    	
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
        AppConstants.FILE_PATH = filename;
        
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
        
        return true;
    }
    
}