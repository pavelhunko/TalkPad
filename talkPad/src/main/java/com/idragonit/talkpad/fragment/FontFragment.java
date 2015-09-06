package com.idragonit.talkpad.fragment;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.editor.Settings;
import com.idragonit.talkpad.ui.ColorPickerDialog;
import com.idragonit.talkpad.ui.FontPickerDialog;
import com.idragonit.talkpad.ui.FontSizePickerDialog;
import com.idragonit.talkpad.utils.Constants;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 *
 * @author pandadaro.senior
 * 2/12/2015
 *
 */
public class FontFragment extends Fragment implements View.OnClickListener , ColorPickerDialog.ColorPickerListener, FontSizePickerDialog.FontSizePickerListener, FontPickerDialog.FontPickerDialogListener, Constants{

    private ImageButton btn_bold, btn_italic, btn_underline;
    
    ColorPickerDialog colorPickerDialog;
    FontSizePickerDialog fontSizePickerDialog;
    FontPickerDialog fontPickerDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = null;
        view = inflater.inflate(R.layout.fragment_font, container, false);

        /*setup view*/
        initview(view);
        return view;
    }

    private void initview(View view){
        btn_bold = (ImageButton) view.findViewById(R.id.btn_bold);
        btn_italic = (ImageButton) view.findViewById(R.id.btn_italic);
        btn_underline = (ImageButton) view.findViewById(R.id.btn_underline);
        btn_bold.setOnClickListener(this);
        btn_italic.setOnClickListener(this);
        btn_underline.setOnClickListener(this);

        view.findViewById(R.id.btn_italic).setOnClickListener(this);
        view.findViewById(R.id.btn_adjustsize).setOnClickListener(this);
        view.findViewById(R.id.btn_changecolor).setOnClickListener(this);
        view.findViewById(R.id.btn_changecase).setOnClickListener(this);

        setState();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_bold:
                onBoldClick(!Settings.IS_BOLD);
                break;
            case R.id.btn_underline:
                onUnderlineClick(!Settings.IS_UNDERLINE);
                break;
            case R.id.btn_italic:
                onItalicClick(!Settings.IS_ITALIC);
                break;
                
            case R.id.btn_adjustsize:
            	AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(ACTION_FONTSIZE, ACTION_DELAY_TIME);
//                onAdjustFontSizeClick();
                break;
            case R.id.btn_changecolor:
            	AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(ACTION_FONTCOLOR, ACTION_DELAY_TIME);
//                onChangeColorClick();
                break;
            case R.id.btn_changecase:
            	AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(ACTION_FONTNAME, ACTION_DELAY_TIME);
//                onChangecaseClick();
                break;
            default:
                return;
        }
    }
    
    public void setState(){
    	drawBoldButton(Settings.IS_BOLD);
    	drawUnderlineButton(Settings.IS_UNDERLINE);
    	drawItalicButton(Settings.IS_ITALIC);
    }

    private void drawBoldButton(boolean bold){
    	if (btn_bold!=null) {
            if(bold)
                btn_bold.setImageResource(R.drawable.bold_s);
            else
                btn_bold.setImageResource(R.drawable.bold);
    	}
    }
    private void onBoldClick(boolean bold){
        drawBoldButton(bold);
        Settings.IS_BOLD = bold;
        AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_bold, 10);
    }

    private void drawUnderlineButton(boolean underline){
    	if (btn_underline!=null) {
            if(underline)
                btn_underline.setImageResource(R.drawable.underline_s);
            else
                btn_underline.setImageResource(R.drawable.underline);
    	}
    }
    private void onUnderlineClick(boolean underline){
    	drawUnderlineButton(underline);
        Settings.IS_UNDERLINE = underline;
        AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_underline, 10);
    }

    private void drawItalicButton(boolean italic){
    	if (btn_italic!=null){
            if(italic) {
                btn_italic.setImageResource(R.drawable.italic_s);
            }else
                btn_italic.setImageResource(R.drawable.italic);
    	}
    }
    
    private void onItalicClick(boolean italic){
    	drawItalicButton(italic);
        Settings.IS_ITALIC = italic;
        AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_italic, 10);
    }

    public void onAdjustFontSizeClick(){
        fontSizePickerDialog = FontSizePickerDialog.getInstance(FontFragment.this);
        fontSizePickerDialog.setFontSize(Settings.TEXT_SIZE);
        fontSizePickerDialog.show(getActivity().getSupportFragmentManager(), "fontsizePicker");
    }

    public void onChangeColorClick(){
        colorPickerDialog = ColorPickerDialog.getInstance(FontFragment.this);
        colorPickerDialog.setColor(Settings.COLOR);
        colorPickerDialog.show(getActivity().getSupportFragmentManager(), "ColorPicker");
    }

    public void onChangecaseClick(){
        fontPickerDialog = FontPickerDialog.getInstance(FontFragment.this);
        fontPickerDialog.setSelectedFont(Settings.FONT_NAME);
        fontPickerDialog.show(getActivity().getSupportFragmentManager(), "fontPicker");
    }

    @Override
    public void onColorPickerDoneClick(DialogFragment dialog) {
        if (colorPickerDialog != null){
            Settings.COLOR = colorPickerDialog.getColor();
            AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_changecolor, ACTION_DELAY_TIME);
            colorPickerDialog.dismiss();
        }
    }

    @Override
    public void onFontSizePickerDoneClick(DialogFragment dialog) {
        if (fontSizePickerDialog != null){
            Settings.TEXT_SIZE = fontSizePickerDialog.getFontSize();
            AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_adjustsize, ACTION_DELAY_TIME);
            fontSizePickerDialog.dismiss();
        }
    }

    @Override
    public void onFontSelected(DialogFragment dialog) {
        if (fontPickerDialog != null){
            Settings.FONT_NAME = fontPickerDialog.getSelectedFont();
//            mainFragment.addStyleSpan_FontName();
            AppConstants.COMMAND_HANDLER.sendEmptyMessageAtTime(R.id.btn_changecase, ACTION_DELAY_TIME);
            fontPickerDialog.dismiss();
        }
    }
}
