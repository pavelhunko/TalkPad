package com.idragonit.talkpad.ui;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.fragment.MainFragment.KeyboardPopupListener;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class ResponsiveScrollView extends ScrollView{

    KeyboardPopupListener key_listener = null;
    boolean isKeyboardShown;
	
	public ResponsiveScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// TODO Auto-generated constructor stub
        isKeyboardShown = false;
	}

	public void setKeyboardPopupListener(KeyboardPopupListener key_listener) {
		// TODO Auto-generated method stub
		this.key_listener = key_listener;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		Log.i("onLayout", changed+"="+left+","+top+","+right+","+bottom);
		if (changed) {
			if (AppConstants.TEXTAREA_HEIGHT==0) {
				AppConstants.TEXTAREA_HEIGHT = bottom;
			}		
			
			if (AppConstants.TEXTAREA_HEIGHT<=bottom) {
				if (isKeyboardShown) {
					isKeyboardShown = false;
					if (key_listener!=null)
						key_listener.onHide();
				}
			} else {
				if (!isKeyboardShown){
					isKeyboardShown = true;
					if (key_listener!=null)
						key_listener.onShow();
				}
			}
		}
		
		super.onLayout(changed, left, top, right, bottom);
	}
}
