package com.idragonit.talkpad.adapter;

import com.idragonit.talkpad.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DriveAdapter extends ArrayAdapter<String> {

	public DriveAdapter(Context context) {
		super(context, 0);
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drivemenu, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.row_title);
		title.setText(getItem(position));

		return convertView;
	}
}
