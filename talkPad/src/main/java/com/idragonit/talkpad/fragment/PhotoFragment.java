package com.idragonit.talkpad.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;

/**
 *
 * @author pandadaro.senior 2/12/2015
 *
 */

public class PhotoFragment extends Fragment implements View.OnClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = null;
		view = inflater.inflate(R.layout.fragment_photo, container, false);
		
		/* setup view */
		initview(view);
		return view;
	}

	private void initview(View view) {
		view.findViewById(R.id.btn_addphoto).setOnClickListener(this);
		view.findViewById(R.id.btn_takepicture).setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_addphoto:
			onAddPhotoClick();
			break;
			
		case R.id.btn_takepicture:
			onTakePictureClick();
			break;
		default:
			return;
		}
	}

	private void onAddPhotoClick() {
		AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_addphoto, 10);
	}

	private void onTakePictureClick() {
		AppConstants.COMMAND_HANDLER.sendEmptyMessageDelayed(R.id.btn_takepicture, 10);
	}

}
