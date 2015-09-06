package com.idragonit.talkpad.fragment;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.utils.Constants;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class BlankFragment extends Fragment implements View.OnTouchListener , Constants {
	View btnMenu; 

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle args) {
		View view = inflater.inflate(R.layout.fragment_blank, container, false);
		btnMenu = view.findViewById(R.id.btn_left);
		btnMenu.setOnTouchListener(this);
		
		return view;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		
		switch (v.getId()){
		case R.id.btn_left:
			AppConstants.APP_HANDLER.sendEmptyMessageDelayed(ACTION_MENU, ACTION_DELAY_TIME);
			break;
		}
		
		return false;
	}
}