package com.idragonit.talkpad.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
/**
 * Created by pandadaro.senior on 1/21/15.
 */
public class FontPickerDialog extends DialogFragment {

    // Keeps the font file paths and names in separate arrays
    private List<String> mFontPaths; // list of file paths for the available fonts
    private List<String> mFontNames; // font names of the available fonts. These indices match up with mFontPaths
    private Context mContext; // The calling activities context.
    private String mSelectedFont; // The font that was selected
    private int pos = 0;

    public interface FontPickerDialogListener extends Serializable {
        public void onFontSelected(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FontPickerDialogListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mListener = (FontPickerDialogListener) getArguments().getSerializable("fontDialogInterface");
        // get context
        mContext = getActivity();
        getDialog().setTitle(getString(R.string.choose_a_font));
        // Let FontManager find available fonts
//        HashMap<String, String> fonts = FontManager.enumerateFonts();
        mFontPaths = new ArrayList<String>();
        mFontNames = new ArrayList<String>();

    	mFontPaths.add("");
    	mFontNames.add("Default");
        // add fonts to List
//        for (String path : fonts.keySet()) {
//            mFontPaths.add(path);
//            mFontNames.add(fonts.get(path));
//        }
    	mFontPaths.add("times.ttf");
    	mFontNames.add("Times New Roman");

    	mFontPaths.add("arial.ttf");
    	mFontNames.add("Arial");

    	mFontPaths.add("georgia.ttf");
    	mFontNames.add("Georgia");

    	mFontPaths.add("courier.ttf");
    	mFontNames.add("Courier New");

    	mFontPaths.add("verdana.ttf");
    	mFontNames.add("Verdana");

    	mFontPaths.add("tahoma.ttf");
    	mFontNames.add("Tahoma");
    	
        pos = getPosition();
        
        View view = inflater.inflate(R.layout.font_picker_dialog, null);
        final ListView list_fontChoice = (ListView) view.findViewById(R.id.list_fontChoice);
        final FontAdapter adapter = new FontAdapter(mContext);
        list_fontChoice.setAdapter(adapter);
//        list_fontChoice.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        list_fontChoice.setSelection(pos);
        
        list_fontChoice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int magicNumber = position;
                mSelectedFont = mFontPaths.get(magicNumber);
                view.setSelected(true);
//                if (firstTimeStartup){
//                	currentSelectedView = list_fontChoice.getChildAt(pos);
//                }
//                
//                firstTimeStartup = false;
                if (currentSelectedView != null && currentSelectedView != view) {
                    unhighlightCurrentRow(currentSelectedView);
                }
                
                currentSelectedView = view;
                highlightCurrentRow(currentSelectedView);
            }
        });
        
        view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFontSelected(FontPickerDialog.this);
            }
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    public static FontPickerDialog getInstance(FontPickerDialogListener dialogInterface) {
        FontPickerDialog fontPickerDialog = new FontPickerDialog();
        Bundle args = new Bundle();
        args.putSerializable("fontDialogInterface", dialogInterface);
        fontPickerDialog.setArguments(args);

        return fontPickerDialog;
    }

    /** Callback method that that is called once a font has been
     * selected and the fontpickerdialog closes.
     * @return The pathname of the font that was selected
     */
    public String getSelectedFont(){
        return mSelectedFont;
    }
    
    public void setSelectedFont(String font){
    	mSelectedFont = font;
    }

    /** Create an adapter to show the fonts in the dialog.
     * Each font will be a text view with the text being the
     * font name, written in the style of the font
     */
    private class FontAdapter extends BaseAdapter {
        private Context mContext;

        public FontAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return mFontNames.size();
        }

        @Override
        public Object getItem(int position) {
            return mFontNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            // We use the position as ID
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;

            // This function may be called in two cases: a new view needs to be
            // created,
            // or an existing view needs to be reused
            if (view == null) {
                view = new TextView(mContext);
                view.setHeight(50);
                view.setGravity(Gravity.CENTER_VERTICAL);
                view.setPadding(10, 0, 0, 0);

            } else {
                view = (TextView) convertView;
            }
            
            if (firstTimeStartup && position == pos) {
                highlightCurrentRow(view);
                currentSelectedView = view;
            } else {
                unhighlightCurrentRow(view);
            }
            
            // Set text to be font name and written in font style
            String font_name = mFontPaths.get(position);
            if (font_name.length()>0) {
                Typeface tface = Typeface.createFromAsset(AppConstants.ASSET_MANAGER, font_name);
                view.setTextSize(10.0f);
                view.setTypeface(tface);
            }
            
            view.setText(mFontNames.get(position));
            return view;
        }
    }

    private int getPosition(){
    	for (int i=0; i<mFontPaths.size(); i++){
    		String font = mFontPaths.get(i);
    		if (font!=null && font.equals(mSelectedFont)){
    			return i;
    		}
    	}
    	
    	return 0;
    }
    
    private void unhighlightCurrentRow(View rowView) {
        if(rowView != null)
            rowView.setBackgroundColor(Color.TRANSPARENT);
    }
    
    private void highlightCurrentRow(View rowView) {
        if(rowView != null)
            rowView.setBackgroundColor(getResources().getColor(R.color.pressed_color));
    }

    private View currentSelectedView = null;
    private boolean firstTimeStartup = true;
}