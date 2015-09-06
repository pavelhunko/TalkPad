package com.idragonit.talkpad.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.Serializable;

import com.idragonit.talkpad.R;

/**
 * Created by pandadaro.senior on 1/21/15.
 */
public class ColorPickerDialog extends DialogFragment{

	LinearLayout p1, p2;
    private int color=0;

    @Override
    public View onCreateView(LayoutInflater inflate, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListener = (ColorPickerListener) getArguments().getSerializable("dialogInterface");
        getDialog().setTitle(getString(R.string.choose_a_fontcolor));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_color_picker, null);
        
        p1 = (LinearLayout) view.findViewById(R.id.parent_color_1);
        p2 = (LinearLayout) view.findViewById(R.id.parent_color_2);
        
        init();
        
        for (int i=0; i<p1.getChildCount(); i++){
        	final Button btn = (Button)p1.getChildAt(i);
        	btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					color = btn.getCurrentTextColor();
					init();
				}
			});
        }
        for (int i=0; i<p2.getChildCount(); i++){
        	final Button btn = (Button)p2.getChildAt(i);
        	btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					color = btn.getCurrentTextColor();
					init();
				}
			});
        }
        
        view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	mListener.onColorPickerDoneClick(ColorPickerDialog.this);
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
    
    public void init(){
    	if (p1!=null){
            for (int i=0; i<p1.getChildCount(); i++){
            	final Button btn = (Button)p1.getChildAt(i);
            	if (btn.getCurrentTextColor()==color) {
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(btn.getCurrentTextColor()); // Changes this drawbale to use a single color instead of a gradient
                    gd.setCornerRadius(1);
                    gd.setStroke(8, Color.WHITE);
                    btn.setBackgroundDrawable(gd);
            	} else {
                    btn.setBackgroundDrawable(new ColorDrawable(btn.getCurrentTextColor()));
            	}
            }
    	}
    	
    	if (p2!=null){
            for (int i=0; i<p2.getChildCount(); i++){
            	final Button btn = (Button)p2.getChildAt(i);
            	if (btn.getCurrentTextColor()==color) {
                    GradientDrawable gd = new GradientDrawable();
                    gd.setColor(btn.getCurrentTextColor()); // Changes this drawbale to use a single color instead of a gradient
                    gd.setCornerRadius(1);
                    gd.setStroke(8, Color.WHITE);
                    btn.setBackgroundDrawable(gd);
            	} else {
                    btn.setBackgroundDrawable(new ColorDrawable(btn.getCurrentTextColor()));
            	}
            }
    	}
    }

    public interface ColorPickerListener extends Serializable {
        public void onColorPickerDoneClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    ColorPickerListener mListener;

    public static ColorPickerDialog getInstance(ColorPickerListener dialogInterface) {
        ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        Bundle args = new Bundle();
        args.putSerializable("dialogInterface", dialogInterface);
        colorPickerDialog.setArguments(args);

        return colorPickerDialog;
    }

    public int getColor() {
//        color = Color.BLACK;// Color.argb(sbAlpha.getProgress(), sbRed.getProgress(), sbGreen.getProgress(), sbBlue.getProgress());
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
