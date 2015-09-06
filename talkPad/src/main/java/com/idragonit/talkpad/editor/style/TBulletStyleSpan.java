package com.idragonit.talkpad.editor.style;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.*;
import android.text.Layout.Alignment;
import android.text.style.DynamicDrawableSpan;
import android.util.Log;
import android.widget.EditText;

import java.util.*;

public class TBulletStyleSpan implements android.text.style.LeadingMarginSpan.LeadingMarginSpan2 {

    private final int MAX_LEADING_COUNT = 0x7fffffff;
    protected int mBulletMargin = 50;
    protected int mLevel = 1;
    protected int c = 5;
    public boolean mLevelTail = false;
    public int mRow = 1;
    public int mLevelUp = 1;
    public int mLevelDown = 1;
    public TBulletType mBulletType = TBulletType.SHAPE;
    public TBulletSortType mSortType = TBulletSortType.SORT_1;
    public TBulletShapeType mShapeType = TBulletShapeType.SHAPE_DISC;
    public Bitmap mBitmap = null;
    public boolean l = false;
    public Layout.Alignment mAlign;

    private TBulletStyleSpan(TBulletType bulletType, TBulletSortType sortType, TBulletShapeType shapeType, Bitmap bitmap) {
        mBulletType = bulletType;
        mSortType = sortType;
        mShapeType = shapeType;
        mBitmap = bitmap;
        mAlign = Alignment.ALIGN_NORMAL;
    }
    
    public void setAlignment(Layout.Alignment align){
    	this.mAlign = align;
    }

    public static CharSequence clearBulletSpans(CharSequence editable) {

        if (editable instanceof Spannable) {

            TBulletStyleSpan[] spans = ((Spannable)editable).getSpans(0, editable.length(), TBulletStyleSpan.class);
            for (int j = 0; j < spans.length; ++ j) {
                ((Spannable)editable).removeSpan(spans[j]);
            }

            TBaseDrawSpan[] drawSpans = ((Spannable)editable).getSpans(0, editable.length(), TBaseDrawSpan.class);
            for (int i = 0; i < drawSpans.length; ++i) {
                ((SpannableStringBuilder)editable).delete(((Spannable)editable).getSpanStart(drawSpans[i]), ((Spannable)editable).getSpanEnd(drawSpans[i]));
            }
        }

        return editable;
    }

    private static void calcBulletLevel(final Editable editable) {

        TBulletStyleSpan[] spans = editable.getSpans(0, editable.length(), TBulletStyleSpan.class);

        if (spans.length <= 0) return;

        Arrays.sort(spans, new Comparator<TBulletStyleSpan>() {
            @Override
            public int compare(TBulletStyleSpan lhs, TBulletStyleSpan rhs) {
                return editable.getSpanStart(lhs) - editable.getSpanStart(rhs);
            }
        });

        Stack st = new Stack();

        int n=0;
        for (int i = 0; i < spans.length; i++) {

            spans[i].mLevelUp = 0;

            //First item
            if (st.size() == 0) {
                st.push(spans[i]);
                spans[i].mLevelTail = false;
                spans[i].mRow = 1;
                spans[i].mLevelUp = spans[i].mLevel;
//                n = i;
            }
            else {
            	
                //Get Top Level Bullet span
                TBulletStyleSpan topSpan = (TBulletStyleSpan) st.peek();

                //If bullet's start pos is not equals with previous bullet's end pos, in a word,
                //  bullet is a root level bullet
                if ((editable.getSpanStart(spans[i]) - 1) != editable.getSpanEnd(spans[i - 1])) {

                    //Remove all spans from stack
                    while (st.size() > 0) {
                        TBulletStyleSpan span = (TBulletStyleSpan) st.pop();
                        editable.setSpan(span, editable.getSpanStart(span), editable.getSpanEnd(span), Spanned.SPAN_MARK_POINT);
                    }

                    spans[i - 1].mLevelTail = true;
                    spans[i - 1].mLevelDown = spans[i - 1].mLevel;

                    st.push(spans[i]);

                    spans[i].mLevelTail = false;
                    spans[i].mRow = 1;
                    spans[i].mLevelUp = spans[i].mLevel;
                    
//                    n = i;
                }

                //bullet is lower level from top span
                else if (spans[i].mLevel < topSpan.mLevel) {

                    //pop spans from stack until reach the stack's top span's level to bullet's level
                    while (st.size() > 0 && ((TBulletStyleSpan) st.peek()).mLevel > spans[i].mLevel) {
                        st.pop();
                    }

                    topSpan.mLevelTail = true;

                    if (st.size() > 0) {
                        if (compareBullet(spans[i], (TBulletStyleSpan) st.peek())) {
                            topSpan.mLevelDown = topSpan.mLevel - spans[i].mLevel;
                        }
//                        else {
                            while (st.size() > 0) {
                                TBulletStyleSpan span = (TBulletStyleSpan) st.pop();
                                editable.setSpan(span, editable.getSpanStart(span), editable.getSpanEnd(span), Spanned.SPAN_MARK_POINT);
                            }
//                        }
                    }

                    topSpan.mLevelDown = topSpan.mLevel;
//                    n = i - 1;
                    i--;
                }

                //bullet is a continuous bullet
                else if (spans[i].mLevel == topSpan.mLevel) {
                    TBulletStyleSpan span = (TBulletStyleSpan) st.pop();

                    spans[i].mLevelTail = false;

                    //bullet is a same style with top stack bullet
                    if (compareBullet(spans[i], topSpan)) {
                        spans[i].mRow = span.mRow + 1;
                    }

                    //is not same
                    else {
                        spans[i].mRow = 1;
                        spans[i].mLevelUp = spans[i].mLevel;
                        span.mLevelTail = true;
                        span.mLevelDown = span.mLevel;
                        st.clear();
                    }

                    st.push(spans[i]);

//                    n = i;
                }

                //bullet is a higher level bullet of top stack's bullet
                else if (spans[i].mLevel > topSpan.mLevel) {
                    spans[i].mRow = 1;
                    spans[i].mLevelTail = false;
                    spans[i].mLevelUp = spans[i].mLevel - topSpan.mLevel;
                    st.push(spans[i]);
                    
//                    n = i;
                }	
            }

            //Reach last bullet span
            if (i == spans.length - 1) {
                if (st.size() > 0) {
                    TBulletStyleSpan span = (TBulletStyleSpan) st.peek();

                    span.mLevelTail = true;
                    span.mLevelDown = span.mLevel;
                }

                while (st.size() > 0) {
                    TBulletStyleSpan span = (TBulletStyleSpan) st.pop();
                    editable.setSpan(span, editable.getSpanStart(span), editable.getSpanEnd(span), Spanned.SPAN_MARK_POINT);
                }
            }
        }
    }

    public static void toggleBullet(Editable editable, int start, int end, TBulletType bulletType, TBulletSortType sortType, TBulletShapeType shapeType) {

        int[] paraRange = getBoundParagraphRange(editable, start, end);
        int paraStart = paraRange[0];
        int paraEnd = paraRange[1];
        boolean fSame = false;
        TBulletStyleSpan[] bulletSpans = editable.getSpans(paraStart, paraEnd, TBulletStyleSpan.class);

        if (bulletSpans.length > 0) {

            fSame = true;
            for (int i = 0; i < bulletSpans.length; i ++) {

                if (bulletSpans[i].mBulletType != bulletType || bulletSpans[i].mSortType != sortType || bulletSpans[i].mShapeType != shapeType) {
                    fSame = false;
                    break;
                }
            }
        }

        //If the bullet spans are all same, then remove all bullet spans, otherwise, set all them to same
        if (!fSame) {

            updateBulletSpans(editable, getParagraphRangesIn(editable, paraStart, paraEnd), bulletType, sortType, shapeType, null, 1);
            calcBulletLevel(editable);
        }
        else {

            //Remove all existing bullet spans
            for (int i = 0; i < bulletSpans.length; ++ i) {
                editable.removeSpan(bulletSpans[i]);
            }

            //Remove all BaseDraw spans
            TBaseDrawSpan[] drawSpans = editable.getSpans(paraStart, paraEnd, TBaseDrawSpan.class);
            for (int i = 0; i < drawSpans.length; ++i) {
                editable.delete(editable.getSpanStart(drawSpans[i]), editable.getSpanEnd(drawSpans[i]));
            }

            //Recalculate bullet span's level and row values
            calcBulletLevel(editable);
        }
    }

    public static void toggleIndent(Editable editable, int var1, int var2, boolean fRight) {

        int[] paraBoundRange = getBoundParagraphRange(editable, var1, var2);
        Iterator<int[]> it = getParagraphRangesIn(editable, paraBoundRange[0], paraBoundRange[1]).iterator();

        while (it.hasNext()) {

            int[] paraRange = it.next();
            TBulletStyleSpan[] spans = editable.getSpans(paraRange[0], paraRange[1], TBulletStyleSpan.class);

            if (spans.length > 0) {

                //Indent right
                if (fRight) {

                    //Max Indent level is 5
                    if (spans[0].mLevel == 5) {
                        continue;
                    }

                    spans[0].mLevel ++;
                }

                //Indent left
                else {

                    //Min Indent level is 1
                    //Reach to first then remove bullet span style
                    if (spans[0].mLevel == 1) {

                        editable.removeSpan(spans[0]);

                        TBaseDrawSpan[] drawSpans = editable.getSpans(paraRange[0], paraRange[1], TBaseDrawSpan.class);
                        if (drawSpans.length > 0) {
                            editable.delete(editable.getSpanStart(drawSpans[0]), editable.getSpanEnd(drawSpans[0]));
                        }
                        continue;
                    }

                    spans[0].mLevel --;
                }

                editable.setSpan(spans[0], paraRange[0], paraRange[1], Spanned.SPAN_MARK_POINT);

            }

            //There is no any bullet span, so add new bullet span
            else if (fRight) {

                editable.setSpan(new TBulletStyleSpan(TBulletType.NONE, TBulletSortType.NONE, TBulletShapeType.NONE, null), paraRange[0], paraRange[1], Spanned.SPAN_MARK_POINT);
                if (editable.getSpans(paraRange[0], paraRange[1], TBaseDrawSpan.class).length == 0) {
                    SpannableStringBuilder ssb = new SpannableStringBuilder();

                    ssb.append(' ');
                    ssb.setSpan(new TBaseDrawSpan(), 0, 1, Spanned.SPAN_POINT_MARK);
                    editable.insert(paraRange[0], ssb);
                }
            }
        }

        calcBulletLevel(editable);
    }

    private static void updateBulletSpans(Editable editable, ArrayList<int[]> rangeList, TBulletType bulletType, TBulletSortType sortType, TBulletShapeType shapeType, Bitmap bitmap, int level) {

        Iterator<int[]> it = rangeList.iterator();

        while (it.hasNext()) {

            int[] paraRange = it.next();
            TBulletStyleSpan[] bulletSpans = editable.getSpans(paraRange[0], paraRange[1], TBulletStyleSpan.class);

            //If exists bullet span already, update only
            if (bulletSpans.length > 0) {

                bulletSpans[0].mBulletType = bulletType;
                bulletSpans[0].mSortType = sortType;
                bulletSpans[0].mShapeType = shapeType;
                editable.setSpan(bulletSpans[0], editable.getSpanStart(bulletSpans[0]), editable.getSpanEnd(bulletSpans[0]), Spanned.SPAN_MARK_POINT);

            }

            //Create new bullet span and set
            else {

                TBulletStyleSpan span = new TBulletStyleSpan(bulletType, sortType, shapeType, bitmap);

                span.mLevel = level;
                editable.setSpan(span, paraRange[0], paraRange[1], Spanned.SPAN_MARK_POINT);

                if (editable.getSpans(paraRange[0], paraRange[0], TBaseDrawSpan.class).length == 0) {

                    SpannableStringBuilder ssb = new SpannableStringBuilder();

                    ssb.append(' ');
                    ssb.setSpan(new TBaseDrawSpan(), 0, 1, Spanned.SPAN_POINT_MARK);
                    editable.insert(paraRange[0], ssb);
                }
            }
        }
    }

    public static void a(SpannableStringBuilder ssb, int start, int end, int k1, int l1) {

        TBulletStyleSpan newSpan;
        SpannableStringBuilder newSsb;

        if (l1 == 1)
            newSpan = new TBulletStyleSpan(TBulletType.SHAPE, TBulletSortType.SORT_1, TBulletShapeType.SHAPE_DISC, null);
        else if (l1 == 2)
            newSpan = new TBulletStyleSpan(TBulletType.SORT, TBulletSortType.SORT_1, TBulletShapeType.SHAPE_DISC, null);
        else
            newSpan = new TBulletStyleSpan(TBulletType.NONE, TBulletSortType.SORT_1, TBulletShapeType.SHAPE_DISC, null);

        newSpan.mLevel = k1;
        ssb.setSpan(newSpan, start, end, Spanned.SPAN_MARK_MARK);

        newSsb = new SpannableStringBuilder();
        newSsb.append(' ');
        newSsb.setSpan(new TBaseDrawSpan(), 0, 1, Spanned.SPAN_POINT_MARK);
        ssb.insert(start, newSsb);
    }

    public static void a(EditText editText) {

        Editable editable = editText.getText();
        TBulletStyleSpan[] spans = editable.getSpans(0, editable.length(), TBulletStyleSpan.class);

        for (int i = 0; i < spans.length; ++ i) {

            int start = editable.getSpanStart(spans[i]);
            int end = editable.getSpanEnd(spans[i]);
            TBaseDrawSpan[] drawSpans = editable.getSpans(start, start, TBaseDrawSpan.class);

            if (drawSpans.length != 0 && editable.getSpanStart(drawSpans[0]) == start) {

                if (editable.getSpans(start, end, TLineDrawSpan.class).length > 0) {

                    editable.removeSpan(spans[i]);
                    editable.delete(editable.getSpanStart(drawSpans[0]), editable.getSpanEnd(drawSpans[0]));
                } else {

                    int[] range = getBoundParagraphRange(editable, start, end);

                    if (range[1] != end) {

                        editable.setSpan(spans[i], start, range[1], Spanned.SPAN_MARK_POINT);
                    }
                    else if (end - start == 2 && editable.charAt(start + 1) == 10) {

                        editable.removeSpan(spans[i]);
                        editable.delete(start, start + 1);
                    }
                    else {

                        ArrayList<int[]> rangeList = getParagraphRangesIn(editable, start, end);
                        if (rangeList.size() > 1 || rangeList.size() == 0) {

                            editable.removeSpan(spans[i]);
                            if (rangeList.size() > 1) {
                                updateBulletSpans(editable, rangeList, spans[i].mBulletType, spans[i].mSortType, spans[i].mShapeType, spans[i].mBitmap, spans[i].mLevel);
                            }
                        }
                    }
                }
            } else {
                editable.removeSpan(spans[i]);
            }
        }

        calcBulletLevel(editText.getText());
    }

    public static void a(EditText editText, int start, int end) {

        TBaseDrawSpan[] drawSpans = editText.getText().getSpans(start, start, TBaseDrawSpan.class);
        int selStart = start;
        int selEnd = end;

        if (drawSpans.length > 0 && editText.getText().getSpanStart(drawSpans[0]) == start) {
            selStart = start + 1;
        }

        if (end != start) {

            TBaseDrawSpan[] drawSpans1 = editText.getText().getSpans(end, end, TBaseDrawSpan.class);
            if (drawSpans1.length > 0 && editText.getText().getSpanStart(drawSpans1[0]) == end) {
                selEnd = end + 1;
            }
        }

        if (selStart != start || selEnd != end) {
            editText.setSelection(selStart, selEnd);
        }
    }

    public static boolean a(EditText editText, int[] range) {

        if (range[0] == range[1]) {

            TBulletStyleSpan[] spans = editText.getText().getSpans(range[0], range[0], TBulletStyleSpan.class);
            if (spans.length > 0 && editText.getText().getSpanStart(spans[0]) == (range[0] - 1)) {

                if (spans[0].mLevel == 1) {
                    editText.getText().removeSpan(spans[0]);
                    return false;
                }

                spans[0].mLevel--;
                editText.getText().setSpan(spans[0], editText.getText().getSpanStart(spans[0]), editText.getText().getSpanEnd(spans[0]), Spanned.SPAN_MARK_POINT);
                calcBulletLevel(editText.getText());

                return true;
            }
        }

        return false;
    }

    private static boolean compareBullet(TBulletStyleSpan c1, TBulletStyleSpan c2) {
        return c1.mBulletType == c2.mBulletType && (c1.mBulletType != TBulletType.SORT || c1.mSortType == c2.mSortType) && (c1.mBulletType != TBulletType.SHAPE || c1.mShapeType == c2.mShapeType);
    }

    public static int[] getBoundParagraphRange(Editable editable, int start, int end) {

        int length = editable.length();
        int rangeStart;
        int rangeEnd = end;

        if (rangeEnd > length) {
            rangeEnd = length;
        }

        //Reverse Find first line break from pos[rangeStart]
        for (rangeStart = start - 1; rangeStart >= 0; rangeStart --) {
            if (editable.charAt(rangeStart) == 10) {
                rangeStart ++;
                break;
            }
        }

        if (rangeStart < 0) {
            rangeStart = 0;
        }

        //Find first line break from pos[rangeEnd]
        while (rangeEnd < length && editable.charAt(rangeEnd) != 10) {
            ++ rangeEnd;
        }

        return new int[]{rangeStart, rangeEnd};
    }

    public static ArrayList<int[]> getParagraphRangesIn(Editable editable, int start, int end) {

        ArrayList<int[]> rangeList = new ArrayList();
        int[] range = new int[]{-1, -1};

        for (int i = start; i <= end; ++ i) {

            if (i == start) {
                range[0] = start;
            }

            if (i == end || editable.charAt(i) == 10) {

                range[1] = i;

                rangeList.add(0, new int[]{range[0], range[1]});

                range[0] = i + 1;
            }
        }

        return rangeList;
    }

    public final String getBuilletTagString(boolean bTagClose) {

        StringBuilder sb = new StringBuilder();
        String tagStart = "<ul>";
        String tagEnd = "</ul>";

        if (mBulletType == TBulletType.SORT) {

            tagStart = "<ol>";
            tagEnd = "</ol>";
        }
        else if (mBulletType == TBulletType.NONE) {

            tagStart = "<ul  style=\"list-style-type:none;\">";
            tagEnd = "</ul>";
        }

        if (!bTagClose) {
            for (int i = 0; i < this.mLevelUp; i ++) {
                sb.append(tagStart);
            }
        } else {
            for (int i = 0; i < this.mLevelDown; i ++) {
                sb.append(tagEnd);
            }
        }

        return sb.toString();
    }

    public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {

        Rect rect = new Rect(x, top, -2 + this.mBulletMargin * this.mLevel, baseline);
//    	Log.e("Bullet", rect.toString()+"--"+mAlign.toString());
        
        Paint paint1 = new Paint();
        paint1.setAntiAlias(true);
        paint1.setColor(paint.getColor());

        
        if (!first) return;

        switch (mBulletType) {
            case NONE:
                paint1.setTextSize(12.0F);
                paint1.setTextAlign(Paint.Align.RIGHT);
                paint1.setColor(0xffcccccc);
                break;

            case SORT:
                String num = "";
                switch (mSortType) {
                    case SORT_a:
                        num = (char) ('a' + (this.mRow - 1) % 26) + ".";
                        break;
                    case SORT_A:
                        num = (char) ('A' + (this.mRow - 1) % 26) + ".";
                        break;
                    case SORT_1:
                        num = this.mRow + ".";
                }

                paint1.setTextSize(26.0F);
                paint1.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(num, (float) (rect.right - 10), (float) rect.bottom, paint1);
                break;

            case SHAPE:
                int bx1 = rect.right - 15;
                int by1 = rect.centerY() + 1;
                TBulletShapeType shapeType = TBulletShapeType.NONE;

                if (mShapeType != TBulletShapeType.NONE) {
                    switch ((this.mLevel - 1) % 3) {
                        case 0:
                            shapeType = TBulletShapeType.SHAPE_DISC;
                            break;
                        case 1:
                            shapeType = TBulletShapeType.SHAPE_CIRCLE;
                            break;
                        case 2:
                            shapeType = TBulletShapeType.SHAPE_SQUARE;
                            break;
                    }
                }

                paint1.setColor(0xff000000);

                switch (shapeType) {
                    case SHAPE_DISC:
                    case SHAPE_CIRCLE:
                        if (shapeType == TBulletShapeType.SHAPE_DISC) {
                            paint1.setStyle(Paint.Style.FILL);
                        } else {
                            paint1.setStyle(Paint.Style.STROKE);
                        }

                        canvas.drawCircle((float) bx1, (float) by1, (float) this.c, paint1);
                        break;

                    case SHAPE_SQUARE:
                        canvas.drawRect((float) (bx1 - this.c), (float) (by1 - this.c), (float) (bx1 + this.c), (float) (by1 + this.c), paint1);
                        break;
                }
                break;

            case BITMAP:
                int bx = rect.centerX() + 10 * (this.mLevel - 1);
                int by = rect.centerY() + 1;
                if (mBitmap != null) {
                    canvas.drawBitmap(mBitmap, (Rect) null, new Rect(bx - 10, by - 10, bx + 10, by + 10), paint);
                }
                break;
        }
    }

    public int getLeadingMargin(boolean flag) {
        return mBulletMargin * mLevel;
    }

    public int getLeadingMarginLineCount() {
        return MAX_LEADING_COUNT;
    }

    public static enum TBulletShapeType {
        NONE,
        SHAPE_DISC,
        SHAPE_CIRCLE,
        SHAPE_SQUARE
    }

    public static enum TBulletSortType {
        NONE,
        SORT_a,
        SORT_A,
        SORT_1,
        SORT_LROMA,
        SORT_UROMA
    }

    public static enum TBulletType {
        NONE,
        SORT,
        SHAPE,
        BITMAP,
        CHECKBOX
    }

    public static class TBaseDrawSpan extends DynamicDrawableSpan {

        Drawable mDrawable = null;

        public TBaseDrawSpan() {

            mDrawable = new PaintDrawable(0);
            mDrawable.setBounds(0, 0, 1, 20);
        }

        public Drawable getDrawable() {
            return mDrawable;
        }
    }
}
