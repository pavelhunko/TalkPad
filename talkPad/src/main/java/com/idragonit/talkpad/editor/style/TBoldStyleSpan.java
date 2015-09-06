package com.idragonit.talkpad.editor.style;

import android.text.TextPaint;
import android.text.style.StyleSpan;

public class TBoldStyleSpan extends StyleSpan {

    public TBoldStyleSpan() {
        super(1);
    }

    public void updateDrawState(TextPaint textpaint) {
        super.updateDrawState(textpaint);
    }
}
