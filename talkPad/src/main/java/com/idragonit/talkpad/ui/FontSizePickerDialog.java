package com.idragonit.talkpad.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import java.io.Serializable;

import com.idragonit.talkpad.R;

/**
 * Created by pandadaro.senior on 1/21/15.
 */
public class FontSizePickerDialog extends DialogFragment{

    private int fontsize = 12;
	private final String[] FONT = { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72" };

    @Override
    public View onCreateView(LayoutInflater inflate, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListener = (FontSizePickerListener) getArguments().getSerializable("fontDialogInterface");
        getDialog().setTitle(getString(R.string.choose_a_fontsize));
        View view = inflate.inflate(R.layout.dialog_fontsize_picker, null);
        
        NumberPicker np = (NumberPicker) view.findViewById(R.id.number_picker);
        np.setMinValue(0);
        np.setMaxValue(FONT.length-1);
        np.setDisplayedValues(FONT);
        np.setValue(fontsize);
        
        np.setWrapSelectorWheel(true);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                fontsize = newVal;
//                picker.setValue((newVal < oldVal)?oldVal-2:oldVal+2);
            }
        });
        view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFontSizePickerDoneClick(FontSizePickerDialog.this);
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

    public interface FontSizePickerListener extends Serializable {
        public void onFontSizePickerDoneClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    FontSizePickerListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static FontSizePickerDialog getInstance(FontSizePickerListener dialogInterface) {
        FontSizePickerDialog fontSizePickerDialog = new FontSizePickerDialog();
        Bundle args = new Bundle();
        args.putSerializable("fontDialogInterface", dialogInterface);
        fontSizePickerDialog.setArguments(args);

        return fontSizePickerDialog;
    }

    public int getFontSize(){
        return Integer.parseInt(FONT[fontsize]);
    }

    public void setFontSize(int size){
        this.fontsize = getIndex(size);
    }
    
    private int getIndex(int font){
    	for (int i=0; i<FONT.length; i++){
    		if (FONT[i].equals(String.valueOf(font)))
    			return i;
    	}
    	return 0;
    }
}
