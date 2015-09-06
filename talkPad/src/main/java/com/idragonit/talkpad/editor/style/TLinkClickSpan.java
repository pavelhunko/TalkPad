package com.idragonit.talkpad.editor.style;

import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import com.idragonit.talkpad.editor.data.TNoteDataUtil;

public class TLinkClickSpan extends ClickableSpan {

    public String mUrl = "";

    public TLinkClickSpan(String url) {

        mUrl = url;
    }

    public static SpannableStringBuilder a(CharSequence text, String path) {

        SpannableStringBuilder ssb = new SpannableStringBuilder();

        if (text != null)
            ssb.append(text);

        String filename = TNoteDataUtil.getFileNameFromPath(path);

        ssb.append(filename);
        ssb.setSpan(new TLinkClickSpan(filename), 0, ssb.length(), 33);

        return ssb;
    }

    public final String getUrl() {
        return mUrl;
    }

    public void onClick(View view) {
//        Toast.makeText(view.getContext(), mUrl, Toast.LENGTH_SHORT).show();
    }

    public void updateDrawState(TextPaint paint) {

        paint.baselineShift = (int) ((float) paint.baselineShift - paint.descent() / 2.0F);
        paint.setColor(0xff0000ff);
    }
}
