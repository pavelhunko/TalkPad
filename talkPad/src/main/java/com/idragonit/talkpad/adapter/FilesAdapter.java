package com.idragonit.talkpad.adapter;

import com.idragonit.talkpad.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FilesAdapter extends ArrayAdapter<FileInfo> {

//	ArrayAdapter<FileInfo> list;
	
	public FilesAdapter(Context context) {
		super(context, 0);
	}

	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_recentfile, null);
		}

		TextView title = (TextView) convertView.findViewById(R.id.row_title);
		TextView time = (TextView) convertView.findViewById(R.id.row_time);
		
		FileInfo file = getItem(position);
		title.setText(file.getFilename());
		time.setText(file.getTime());

		return convertView;
	}

}
