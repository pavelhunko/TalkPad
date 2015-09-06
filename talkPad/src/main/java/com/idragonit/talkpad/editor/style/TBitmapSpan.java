package com.idragonit.talkpad.editor.style;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.media.ExifInterface;
import android.text.*;
import android.text.style.DynamicDrawableSpan;
import android.view.WindowManager;
import android.widget.EditText;

public class TBitmapSpan extends DynamicDrawableSpan {

    public static int MAX_REAL_HEIGHT = 2000;
    public static int MAX_THUMB_HEIGHT = 80;
    public static int padding = 5;
    public int mMaxWidth = 480;
    public int mWidth = 0;
    public boolean mThumb = false;
    public String mFilePath = "";
    protected Rect mRect = null;
    protected Bitmap mBitmap = null;
    public PaintDrawable mDrawable = null;

    private Bitmap getCachedBitmap(Editable editable) {

        TBitmapSpan[] spans = editable.getSpans(0, editable.length(), TBitmapSpan.class);

        for (int i = 0; i < spans.length; ++ i) {

            if (spans[i].mFilePath.equals(this.mFilePath) && spans[i].mBitmap != null && !spans[i].mBitmap.isRecycled()) {
                return spans[i].mBitmap;
            }
        }

        return null;
    }

    private Rect calcDrawRect(int width, int height) {

        Rect rect = new Rect(0, 0, width, height);
        int maxHeight;
        int w1;
        int h1;

        maxHeight = mThumb == false ? MAX_REAL_HEIGHT : MAX_THUMB_HEIGHT;
        w1 = mMaxWidth - 2 * padding;
        h1 = maxHeight - 2 * padding;

        if (width > w1 || height > h1) {

            float rateW = (float) w1 / (float) width;
            float rateH = (float) h1 / (float) height;

            if (rateH >= rateW)
                rateH = rateW;

            rect.set(0, 0, (int) (rateH * (float) width), (int) (rateH * (float) height));
        }

        return rect;
    }

    public static Spanned getBitmapSpanned(String filePath, EditText text) {

        if (filePath == null)
            return null;

        TBitmapSpan span = new TBitmapSpan();
        Point size = new Point();

        ((WindowManager) text.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);

        span.mFilePath = filePath;
        span.mMaxWidth = (int)(size.x*0.8);

        if (!span.initBitmapSpan(text.getText(), null, text)) {
            return new SpannableStringBuilder();
        } else {

            SpannableStringBuilder ssb = new SpannableStringBuilder();

            ssb.append("\uFFFC");
            ssb.setSpan(span, 0, 1, Spanned.SPAN_POINT_MARK);

            return ssb;
        }
    }

    private int getOrientation() {
        int rotate = 0;

        try {
            ExifInterface exif = new ExifInterface(mFilePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rotate;
    }

    private boolean loadBitmap() {

        android.graphics.BitmapFactory.Options bmpOpt = new android.graphics.BitmapFactory.Options();

        try {

            bmpOpt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mFilePath, bmpOpt);
        } catch (Exception e) {

            mBitmap = null;
            return false;
        } catch (OutOfMemoryError e) {

            mBitmap = null;
            return false;
        }

        if (bmpOpt.outWidth != 0 && bmpOpt.outHeight != 0) {

            int rotate = getOrientation();
            int width, height;

            if (rotate == 0 || rotate == 180) {
                width = bmpOpt.outWidth;
                height = bmpOpt.outHeight;
            }
            else {
                width = bmpOpt.outHeight;
                height = bmpOpt.outWidth;
            }

            mRect = calcDrawRect(width, height);

            bmpOpt.inSampleSize = (int) (1.0F / ((float) mRect.width() / (float) width));
            bmpOpt.inJustDecodeBounds = false;

            try {

                Bitmap orgBmp = BitmapFactory.decodeFile(mFilePath, bmpOpt);
                Matrix matrix = new Matrix();

                matrix.postRotate(rotate);
                mBitmap = Bitmap.createBitmap(orgBmp, 0, 0, orgBmp.getWidth(), orgBmp.getHeight(), matrix, true);
//                mBitmap = Bitmap.createScaledBitmap(temp, (int)(orgBmp.getWidth()/5.0*4), (int)(orgBmp.getHeight()/5.0*4), false);
                mRect = calcDrawRect(mBitmap.getWidth(), mBitmap.getHeight());

            }
            catch (Exception e) {
                mBitmap = null;
            }
            catch (OutOfMemoryError e) {
                mBitmap = null;
            }

            if (mBitmap != null)
                return true;
        }

        return false;
    }

    public final String getFilePath() {
        return mFilePath;
    }

    public final void onDelete(Editable editable, boolean flag) {

        if (!flag) {
            TBitmapSpan[] spans = editable.getSpans(0, editable.length(), TBitmapSpan.class);
            boolean exist = true;

            for (int i = 0; i < spans.length; i ++) {

                if (spans[i].mFilePath.equals(mFilePath)) {
                    exist = false;

                    if (this != spans[i]) {
                        break;
                    }
                }
            }

            if (!exist) {
                return;
            }
        }

        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public final void setThumbState(boolean thumb) {

        if (thumb != mThumb) {

            mThumb = thumb;
            mRect = calcDrawRect(mBitmap.getWidth(), mBitmap.getHeight());
            mDrawable.setBounds(new Rect(0, 0, mRect.width() + 2 * padding, mRect.height() + 2 * padding));
        }
    }

    public final boolean initBitmapSpan(Editable editable, SpannableStringBuilder ssb, EditText editText) {

    	Point size = new Point();

        ((WindowManager) editText.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(size);

        mMaxWidth = (int)(size.x*0.8);

        if (mBitmap == null || mBitmap.isRecycled()) {

            mBitmap = getCachedBitmap(editable);
            if (mBitmap == null && ssb != null)
                mBitmap = getCachedBitmap(ssb);

            if (mBitmap == null || mBitmap.isRecycled()) {

                boolean fThumb = mThumb;

                mThumb = false;
                if (!loadBitmap()) {

                    mThumb = fThumb;
                    return false;
                }

                mThumb = fThumb;
            }
        }

        mDrawable = new PaintDrawable();
        if (mBitmap != null && !mBitmap.isRecycled()) {
        	mRect = calcDrawRect(mBitmap.getWidth(), mBitmap.getHeight());
        }
        else {
        	mRect = new Rect(0, 0, 20, 20);
        }
        
        mDrawable.setBounds(new Rect(0, 0, mRect.width() + 2 * padding, mRect.height() + 2 * padding));

        return true;
    }

    public final TBitmapSpan duplicate(Editable editable, SpannableStringBuilder ssb) {

        TBitmapSpan newSpan = new TBitmapSpan();

        newSpan.mFilePath = mFilePath;
        newSpan.mWidth = mWidth;
        newSpan.mThumb = mThumb;
        newSpan.mBitmap = null;
        newSpan.mDrawable = mDrawable;
        newSpan.mRect = mRect;

        newSpan.initBitmapSpan(editable, ssb, null);

        return newSpan;
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {

        if (mBitmap == null || mBitmap.isRecycled())
            return;

        Drawable drawable = getDrawable();
        Paint paint1 = new Paint();
        int paddingBottom = bottom - drawable.getBounds().bottom;

        canvas.save();

        if (mVerticalAlignment == 1)
            paddingBottom -= paint.getFontMetricsInt().descent;

        canvas.translate(x, paddingBottom);
        paint1.setColor(0xffffffff);

        int cx = (drawable.getBounds().right - mRect.width()) / 2;
        int cy = (drawable.getBounds().bottom - mRect.height()) / 2;

        canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()), new Rect(cx, cy, cx + mRect.right, cy + mRect.bottom), paint1);
        canvas.restore();
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public int getVerticalAlignment() {
        return super.getVerticalAlignment();
    }
}
