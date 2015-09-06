package com.idragonit.talkpad.editor.style;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.view.View;

public class TLineDrawSpan extends DynamicDrawableSpan {

    protected int mColor = 0;
    protected int mWidth = 0;
    protected Bitmap mBitmap = null;
    public PaintDrawable mDrawable = null;

    public TLineDrawSpan(int color, int width) {

        mColor = color;
        mWidth = width;
        mBitmap = null;
        mDrawable = new PaintDrawable();
        mDrawable.setBounds(0, 0, mWidth, 20);
    }

    public static Spanned getLineDrawSpannable(View view) {

        int width = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight();
        SpannableStringBuilder ssb = new SpannableStringBuilder();

        ssb.append("\uFFFC");
        ssb.setSpan(new TLineDrawSpan(0xff888888, width), 0, 1, Spanned.SPAN_POINT_MARK);

        return ssb;
    }

    public final int getColor() {
        return mColor;
    }

    public final void setWidth(int width) {

        mWidth = width;

        mDrawable = new PaintDrawable();
        mDrawable.setBounds(0, 0, mWidth, 20);
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        Paint painter = new Paint();

        painter.setColor(mColor);
        painter.setStrokeWidth(0.0F);

        canvas.drawLine(x, top + (bottom - top) / 2, x + (float) mWidth, top + (bottom - top) / 2, painter);
    }

    public Drawable getDrawable() {
        return mDrawable;
    }
}
