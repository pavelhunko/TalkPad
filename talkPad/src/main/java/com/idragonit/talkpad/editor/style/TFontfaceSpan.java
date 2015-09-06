package com.idragonit.talkpad.editor.style;

import com.idragonit.talkpad.AppConstants;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

/**
 * Created by Michale on 2/26/2015.
 */
public class TFontfaceSpan extends TypefaceSpan {
    private Typeface mTypeface = null;
    private String mFontUri = "";

    public TFontfaceSpan(String fontUri) {
        super("");

        mTypeface = Typeface.createFromAsset(AppConstants.ASSET_MANAGER, fontUri);
        mFontUri = fontUri;
    }

    public String getFontUri() {
        return mFontUri;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setTypeface(mTypeface);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.setTypeface(mTypeface);
    }
}
