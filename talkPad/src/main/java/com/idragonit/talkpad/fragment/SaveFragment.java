package com.idragonit.talkpad.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.adapter.DriveAdapter;
import com.idragonit.talkpad.adapter.MenuAdapter;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.editor.data.TNoteDataUtil;
import com.idragonit.talkpad.utils.CommonMethods;
import com.idragonit.talkpad.utils.Constants;
import com.idragonit.talkpad.utils.Stack;

import fr.xgouchet.androidlib.ui.activity.AbstractBrowsingActivity;

/**
 *
 * @author pandadaro.senior 2/12/2015
 *
 */
public class SaveFragment extends AbstractBrowsingActivity implements
		OnClickListener, Constants, DialogInterface.OnClickListener {

	ListView driveList;
	
	/** the edit text input */
	protected EditText mFileName;
	protected Drawable mWriteable;
	protected Drawable mLocked;

	public View onCreateView(LayoutInflater layoutinflater, ViewGroup viewgroup, Bundle bundle) {
		final View view = layoutinflater.inflate(R.layout.fragment_saveas, viewgroup, false);
		
		driveList = (ListView)view.findViewById(R.id.list_drive);
		
		if (AppConstants.FILE_MANAGER__LOCATION.length()==0){
			DriveAdapter adapter = new DriveAdapter(getActivity());
			String[] menu = getResources().getStringArray(R.array.drive_menu);
			for (int i = 0; i < menu.length; i++)
				adapter.add(menu[i]);

			driveList.setAdapter(adapter);
//			driveList.setSelector(R.drawable.drive_selector);
			
			view.findViewById(android.R.id.list).setVisibility(View.GONE);
			mCurrentFolder = null;
			
			driveList.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
					driveList.setVisibility(View.GONE);
					view.findViewById(android.R.id.list).setVisibility(View.VISIBLE);
					
					switch (position){
					case 0:
						fillFolderView(new File(TNoteDataUtil.mTalkPadPath));
						break;
					case 1:
						fillFolderView(new File(TNoteDataUtil.mStoragePath));
						break;
					case 2:
						fillFolderView(new File("/"));
						break;
					}
				}
			});
		} else {
			driveList.setVisibility(View.GONE);
	        mCurrentFolder = new File(AppConstants.FILE_MANAGER__LOCATION);
		}
		
		initview(view);
		return view;
	}

	private void initview(View view) {
		// buttons
		view.findViewById(R.id.buttonCancel).setOnClickListener(this);
		view.findViewById(R.id.buttonOk).setOnClickListener(this);
		((Button) view.findViewById(R.id.buttonOk)).setText(R.string.ui_save);

		// widgets
		mFileName = (EditText) view.findViewById(R.id.editFileName);

		// drawables
		mWriteable = getResources().getDrawable(R.drawable.folder_rw);
		mLocked = getResources().getDrawable(R.drawable.folder_r);
	}

	@Override
	public void onClick(View v) {
		if (mFileName != null) {
			CommonMethods.hideKeyboard(getActivity(), mFileName);
		}

		switch (v.getId()) {
		case R.id.buttonCancel:
			AppConstants.APP_EXIT = false;
			AppConstants.IS_SAVED = false;
//			if (AppConstants.APP_EXIT) {
//				AppConstants.init();
//				Settings.init();
//				getActivity().finish();
//			} else {
				AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
//			}
			
			break;
		case R.id.buttonOk:
			if (setSaveResult()) {
				saveFile();
				
				Settings.BEFORE_NOTES = Settings.TEMP_NOTES;
			}
			
			if (AppConstants.IS_SAVED) {
				AppConstants.init();
				Settings.init();
				Stack.clear();
				
				AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
			} else if (AppConstants.APP_EXIT){
				AppConstants.init();
				Settings.init();
				Stack.clear();
				AppConstants.FILE_MANAGER__LOCATION = "";
				
				getActivity().finish();
			}
			break;
		}
	}

	protected void onFileClick(File file) {
		if (file.canWrite())
			mFileName.setText(file.getName());
	}

	protected boolean onFolderClick(File folder) {
		return true;
	}

	protected void onFolderViewFilled() {
	}

	public void showConfirmDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Are you sure to overwrite this file?")
				.setNegativeButton("No", this).setPositiveButton("Yes", this)
				.show();
	}

	private void saveFile() {
		if (CommonMethods
				.writeFile(AppConstants.FILE_PATH, Settings.TEMP_NOTES)) {
			AppConstants.SAVE_MODE = SAVED_MODE;
			AppConstants.EDIT_MODE = OPEN_MODE;

			Toast.makeText(getActivity(), R.string.toast_save_success,
					Toast.LENGTH_SHORT).show();

			try {
				SharedPreferences pref = getActivity().getApplicationContext()
						.getSharedPreferences(APP_NAME,
								getActivity().MODE_PRIVATE);
				Editor editor = pref.edit();

				int recent = pref.getInt(PREFERENCE_RECENT_COUNT, 0);
				int k = 0;

				do {
					k = 0;
					for (int i = 1; i <= recent; i++) {
						String path = pref.getString(
								PREFERENCE_RECENT_FILE + i, "");
						if (path.length() == 0
								|| path.equals(AppConstants.FILE_PATH)) {
							k = i;
							break;
						}
					}

					if (k != 0) {
						for (int i = k; i < recent; i++) {
							int v = i + 1;
							String path = pref.getString(PREFERENCE_RECENT_FILE
									+ v, "");
							editor.putString(PREFERENCE_RECENT_FILE + i, path);
						}

						editor.commit();
						recent--;
					}
				} while (k != 0);

				if (recent == RECENT_FILE_COUNT) {
					for (int i = 1; i < recent; i++) {
						int v = i + 1;
						String path = pref.getString(
								PREFERENCE_RECENT_FILE + v, "");
						editor.putString(PREFERENCE_RECENT_FILE + i, path);
					}
				} else {
					recent++;
				}

				editor.putInt(PREFERENCE_RECENT_COUNT, recent);
				editor.putString(PREFERENCE_RECENT_FILE + recent,
						AppConstants.FILE_PATH);
				editor.commit();
			} catch (Exception e) {
			}

		} else {
			Toast.makeText(getActivity(), R.string.toast_save_error,
					Toast.LENGTH_SHORT).show();
			AppConstants.FILE_PATH = "";
		}

		if (AppConstants.APP_EXIT) {
			
		} else {
			AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_NEW_PAD, ACTION_DELAY_TIME);
		}
	}

	/**
	 * Sets the result data when the user presses save
	 *
	 * @return if the result is OK (if not, it means the user must change its
	 *         selection / input)
	 */
	protected boolean setSaveResult() {
		String fileName;

		if (driveList!=null && driveList.getVisibility()==View.VISIBLE){
			Toast.makeText(getActivity(), "Please select folder to save!", Toast.LENGTH_SHORT).show();
			return false;
		}
		
		if ((mCurrentFolder == null) || (!mCurrentFolder.exists())) {
			Toast.makeText(getActivity(), R.string.toast_folder_doesnt_exist, Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!mCurrentFolder.canWrite()) {
			Toast.makeText(getActivity(), R.string.toast_folder_cant_write, Toast.LENGTH_SHORT).show();
			return false;
		}

		fileName = mFileName.getText().toString();
		if (fileName.length() == 0) {
			Toast.makeText(getActivity(), R.string.toast_filename_empty, Toast.LENGTH_SHORT).show();
			return false;
		}

		String temp = mCurrentFolder.getAbsolutePath() + File.separator + fileName;
		if (!fileName.endsWith(FILE_EXTENTION))
			temp += FILE_EXTENTION;

		File file = new File(temp);
		if (file.exists()) {
			Settings.OVERWRITE_FILE = temp;
			showConfirmDialog();
			return false;
		} else {
			AppConstants.FILE_PATH = temp;
		}

        AppConstants.FILE_MANAGER__LOCATION = mCurrentFolder.getAbsolutePath(); 
		
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			// Yes button clicked
			AppConstants.FILE_PATH = Settings.OVERWRITE_FILE;
			saveFile();
			break;

		case DialogInterface.BUTTON_NEGATIVE:
			// No button clicked
			break;
		}
	}
}
