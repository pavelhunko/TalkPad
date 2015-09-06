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
public class LanguagePickerDialog extends DialogFragment{

	private final String[] LANGUAGE = { 
		"Afrikaans", "Basque",	"Bulgarian",	"Catalan",	"Czech", 	"Dutch",	"English (Australia)", "English (Canada)", "English (New Zealand)", "English (South Africa)", "English (UK)", "English (US)", "Finnish",
		
		"French", "German", "Hebrew", "Hungarian", "Icelandic", "Italian", "Indonesian", "Japanese", "Korean", "Latin", "Mandarin Chinese", "Traditional Taiwan", "Simplified China", "Norwegian", "Polish", "Portuguese", "Romanian",
		
		"Russian", "Serbian", "Slovak", "Swedish", "Turkish", "Zulu", "Spanish (Argentina)", "Spanish (Bolivia)", "Spanish (Dominican Republic)", "Spanish (Ecuador)", "Spanish (El Salvador)", "Spanish (Guatemala)", "Spanish (Honduras)", "Spanish (Mexico)", "Spanish (Nicaragua)", "Spanish (Panama)", "Spanish (Paraguay)", "Spanish (Peru)", "Spanish (Puerto Rico)", "Spanish (Spain)", "Spanish (US)", "Spanish (Uruguay)", "Spanish (Venezuela)",
		
		"Arabic (Egypt)", "Arabic (Jordan)", "Arabic (Kuwait)", "Arabic (Lebanon)", "Arabic (Qatar)", "Arabic (UAE)", "Arabic (Morocco)", "Arabic (Iraq)", "Arabic (Algeria)", "Arabic (Bahrain)", "Arabic (Lybia)", "Arabic (Oman)", "Arabic (Saudi Arabia)", "Arabic (Tunisia)", "Arabic (Yemen)",
		
		"Galician"
	};
	
	private final String[] LANG_CODE = {
		"AF",		 "EU",		"BG",			"CA",		"CS", 		"nl-NL",	"en-AU",				"en-CA",			"en-NZ", 				"en-ZA", 					"en-GB",		"en-US",		"fi",
		
		"fr-FR",  "de-DE", 	"he", 		"hu", 		"is", 		"it-IT",	"id", 			"ja", 		"ko",	"la",		"zh-CN",			"zh-TW",				"zh-CN", 		"no-NO",	"pl", 		"pt-PT",	"ro-RO",
		
		"ru", 		"sr-SP", 	"sk",	"sv-SE",	"tr",		"zu",	"es-AR",				"es-BO", 			"es-DO",						"es-EC",				"es-SV",				"es-GT", 				"es-HN",			"es-MX", 			"es-NI",				"es-PA",			"es-PY", 			"es-PE",  			"es-PR", 					"es-ES",		"es-US",		"es-UY",			"es-VE",
		
		"ar-EG", 				"ar-JO",			"ar-KW",		"ar-LB",			"ar-QA",		"ar-AE",			"ar-MA",		"ar-IQ",			"ar-DZ",			"ar-BH",			"ar-LY",		"ar-OM",			"ar-SA",				"ar-TN",			"ar-YE",
		
		"gl"
	};
	
	private int curItem=0;

    @Override
    public View onCreateView(LayoutInflater inflate, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mListener = (LanguagePickerListener) getArguments().getSerializable("languageDialogInterface");
        getDialog().setTitle(getString(R.string.choose_a_speechlanguage));
        View view = inflate.inflate(R.layout.dialog_language_picker, null);
        
        NumberPicker np = (NumberPicker) view.findViewById(R.id.number_picker);
        np.setMinValue(0);
        np.setMaxValue(LANGUAGE.length-1);
        np.setDisplayedValues(LANGUAGE);
        np.setValue(curItem);
        
        np.setFocusable(false);
        np.setFocusableInTouchMode(false);
        np.setClickable(false);
        np.setWrapSelectorWheel(true);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener(){
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
            	curItem = newVal;
//                picker.setValue((newVal < oldVal)?oldVal-2:oldVal+2);
            }
        });
        view.findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLanguagePickerDoneClick(LanguagePickerDialog.this);
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

    public interface LanguagePickerListener extends Serializable {
        public void onLanguagePickerDoneClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    LanguagePickerListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static LanguagePickerDialog getInstance(LanguagePickerListener dialogInterface) {
        LanguagePickerDialog fontSizePickerDialog = new LanguagePickerDialog();
        Bundle args = new Bundle();
        args.putSerializable("languageDialogInterface", dialogInterface);
        fontSizePickerDialog.setArguments(args);

        return fontSizePickerDialog;
    }

    public String getLanguage(){
        return LANG_CODE[curItem];
    }

    public void setLanguage(String lang){
         curItem = getIndex(lang);
    }
    
    private int getIndex(String language){
    	for (int i=0; i<LANG_CODE.length; i++){
    		if (LANG_CODE[i].equals(language))
    			return i;
    	}
    	
    	return 0;
    }
}
