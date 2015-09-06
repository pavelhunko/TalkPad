package com.idragonit.talkpad.editor.style;

import android.text.TextPaint;
import android.text.style.SuperscriptSpan;

public class TSuperscriptSpan extends SuperscriptSpan {

    public TSuperscriptSpan() {
    }

    public void updateDrawState(TextPaint textpaint) {

        textpaint.setTextSize(10F);
        textpaint.baselineShift = textpaint.baselineShift + (int) textpaint.ascent();
    }

    public void updateMeasureState(TextPaint textpaint) {

        textpaint.setTextSize(10F);
        textpaint.baselineShift = textpaint.baselineShift + (int) textpaint.ascent();
    }
}
