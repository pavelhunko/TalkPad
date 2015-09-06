package com.idragonit.talkpad.editor;

import android.app.Application;
import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.Parcelable;
import android.text.*;
import android.text.style.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.EditText;

import com.idragonit.talkpad.AppConstants;
import com.idragonit.talkpad.R;
import com.idragonit.talkpad.editor.style.TBitmapSpan;
import com.idragonit.talkpad.editor.style.TBoldStyleSpan;
import com.idragonit.talkpad.editor.style.TBulletStyleSpan;
import com.idragonit.talkpad.editor.style.TFontfaceSpan;
import com.idragonit.talkpad.editor.style.TItalicStyleSpan;
import com.idragonit.talkpad.editor.style.TLineDrawSpan;
import com.idragonit.talkpad.editor.style.TLinkClickSpan;
import com.idragonit.talkpad.editor.style.TSubscriptSpan;
import com.idragonit.talkpad.editor.style.TSuperscriptSpan;

import java.util.*;

public class TNoteEditText extends EditText {

    private static Object mSpan = new Object();
    public Handler mHandler;
    protected TNoteHashMap mSpanMap;
    private Paint mPainter;
    private Path mPath;
    private Object mCurSpan;
    private CharSequence mClipboardText;
    private TNoteParagraphStyle mCurParagraphStyle;
    
    public TNoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHandler = null;
        mClipboardText = null;
        mPath = null;
        mCurSpan = null;
        mPainter = new Paint();
        mSpanMap = new TNoteHashMap();
        mCurParagraphStyle = new TNoteParagraphStyle();
        
        setBackgroundColor(0xFFFFFFFF);
//        setHighlightColor(0);
        setGravity(Gravity.TOP | Gravity.LEFT);
        setTextSize(0, 24.0F);
        setLineSpacing(3F, 1.0F);
        addTextChangedListener(new TNoteEditWatcher(this));
    }

    public static int getSpanStartPos(TNoteEditText edit, Object obj) {
        return edit.getSpanStartPos(obj);
    }

    public static int getSpanEndPos(TNoteEditText edit, Object obj) {
        return edit.getSpanEndPos(obj);
    }

    private int getSpanStartPos(Object obj) {
        return getText().getSpanStart(obj);
    }

    private int getSpanEndPos(Object obj) {
        return getText().getSpanEnd(obj);
    }

    private static int getCharacterStyleSpanValue(CharacterStyle style) {
        Class spanClass = style.getClass();

        if (spanClass == ForegroundColorSpan.class) {
            return ((ForegroundColorSpan) style).getForegroundColor();
        }
        if (spanClass == BackgroundColorSpan.class) {
            return ((BackgroundColorSpan) style).getBackgroundColor();
        }
        if (spanClass == AbsoluteSizeSpan.class) {
            return ((AbsoluteSizeSpan) style).getSize();
        }
        if (spanClass == TFontfaceSpan.class) {
            return 0;
        }
        if (spanClass == TBoldStyleSpan.class) {
            return 0;
        }
        if (spanClass == TItalicStyleSpan.class) {
            return 0;
        }
        if (spanClass == UnderlineSpan.class) {
            return 0;
        }
        if (spanClass == StrikethroughSpan.class) {
            return 0;
        }
        if (spanClass == TSuperscriptSpan.class) {
            return 0;
        }
        if (spanClass == TSubscriptSpan.class) {
            return 0;
        }

        return 0;
    }

    public static void refreshEditText(TNoteEditText edit, int start) {
        edit.refreshEditText(start, 0);
    }

    private void refreshEditText(int start, int count) {
        if (getText().getSpans(start, start, TNoteHashMap.class).length == 0) {
            return;
        }

        Object[] spanClasses = mSpanMap.map.keySet().toArray();
        if (count != 0) {
            for (int i = 0; i < spanClasses.length; ++ i) {
                Object[] spans = getText().getSpans(start, start, (Class) spanClasses[i]);
                for (int j = 0; j < spans.length; ++ j) {

                    if (getSpanEndPos(spans[j]) == start) {
                        getText().setSpan(spans[j], getSpanStartPos(spans[j]), getSpanEndPos(spans[j]), Spanned.SPAN_POINT_POINT);
                    }
                }

                Object span = mSpanMap.map.get(spanClasses[i]);
                if (span != mSpan) {
                    getText().setSpan(span, start, start + count, Spanned.SPAN_POINT_POINT);
                }
            }

            j();
        } else {
            for (int i = 0; i < spanClasses.length; ++ i) {
                Object[] spans = getText().getSpans(start, start, (Class) spanClasses[i]);
                for (int j = 0; j < spans.length; ++ j) {
                    if (getSpanEndPos(spans[j]) == start) {
                        getText().setSpan(spans[j], getSpanStartPos(spans[j]), getSpanEndPos(spans[j]), Spanned.SPAN_POINT_MARK);
                    }
                }
            }
        }
    }

    private void resetSpanList(TNoteSpanList spanList) {
        if (spanList.spans.length > 0) {
            for (int i = 0; i < spanList.spans.length; ++ i) {
                getText().setSpan(spanList.spans[i], spanList.start[i], spanList.end[i], spanList.flags[i]);
            }
        }
    }

    private void clearSpan(Class spanClass, int start, int end) {
        clearSpan(spanClass, start, end, false);
    }

    private void clearSpan(Class spanClass, int start, int end, boolean flag) {

        Object[] spans = getText().getSpans(start, end, spanClass);

        for (int i = 0; i < spans.length; ++ i) {

            if (!(spans[i] instanceof ReplacementSpan)) {

                int spanStart = getText().getSpanStart(spans[i]);
                int spanEnd = getText().getSpanEnd(spans[i]);

                if (spanStart >= start && spanEnd <= end) {

                    getText().removeSpan(spans[i]);
                    continue;
                }

                Object span;
                int spanFlag;

                if (spanStart < start && spanEnd > end) {
                    spanFlag = getText().getSpanFlags(spans[i]);
                    getText().setSpan(spans[i], spanStart, start, flag ? Spanned.SPAN_POINT_MARK : spanFlag);
                    Class aClass = flag ? spans[i].getClass() : spanClass;

                    if (aClass == ForegroundColorSpan.class) {
                        span = new ForegroundColorSpan(((ForegroundColorSpan)spans[i]).getForegroundColor());
                    } else if (aClass == BackgroundColorSpan.class) {
                        span = new BackgroundColorSpan(((BackgroundColorSpan)spans[i]).getBackgroundColor());
                    } else if (aClass == AbsoluteSizeSpan.class) {
                        span = new AbsoluteSizeSpan(((AbsoluteSizeSpan)spans[i]).getSize());
                    } else if (aClass == TFontfaceSpan.class) {
                        span = new TFontfaceSpan(((TFontfaceSpan)spans[i]).getFontUri());
                    } else if (aClass == TBoldStyleSpan.class) {
                        span = new TBoldStyleSpan();
                    } else if (aClass == TItalicStyleSpan.class) {
                        span = new TItalicStyleSpan();
                    } else if (aClass == UnderlineSpan.class) {
                        span = new UnderlineSpan();
                    } else if (aClass == StrikethroughSpan.class) {
                        span = new StrikethroughSpan();
                    } else if (aClass == TSuperscriptSpan.class) {
                        span = new TSuperscriptSpan();
                    } else if (aClass == TSubscriptSpan.class) {
                        span = new TSubscriptSpan();
                    } else {
                        span = null;
                    }

                    spanStart = end;
                } else {
                    spanFlag = getText().getSpanFlags(spans[i]);
                    span = spans[i];

                    if (spanStart >= start) {
                        if (end == spanStart) {
                            continue;
                        }

                        spanStart = end;
                    } else {
                        if (spanEnd > end || start == spanEnd && !flag) {
                            continue;
                        }

                        spanEnd = start;
                        if (flag) {
                            spanFlag = Spanned.SPAN_POINT_MARK;
                        }
                    }
                }

                getText().setSpan(span, spanStart, spanEnd, spanFlag);
            }
        }
    }

    private void setHashMapSpan(Class spanClass, Object span, int start) {
        if (getText().getSpans(start, start, TNoteHashMap.class).length == 0)
            getText().setSpan(mSpanMap, start, start, Spanned.SPAN_MARK_MARK);

        mSpanMap.add(spanClass, span);
    }

    private boolean getToggleSpanValue(Class spanClass) {
        updateParagraphStyle();

        if (spanClass == TBoldStyleSpan.class)
            return mCurParagraphStyle.bold;
        if (spanClass == TItalicStyleSpan.class)
            return mCurParagraphStyle.italic;
        if (spanClass == UnderlineSpan.class)
            return mCurParagraphStyle.underline;
        if (spanClass == TSuperscriptSpan.class)
            return mCurParagraphStyle.superScript;
        if (spanClass == TSubscriptSpan.class)
            return mCurParagraphStyle.subScript;
        if (spanClass == StrikethroughSpan.class)
            return mCurParagraphStyle.strikeThrough;

        return false;
    }

    private CharacterStyle[] replaceStyleSpan(CharacterStyle[] spanList, CharacterStyle span) {

        for (int i = 0; i < spanList.length; ++ i) {

            if (spanList[i] != null && getCharacterStyleSpanValue(spanList[i]) == getCharacterStyleSpanValue(span)) {

                int start1 = getSpanStartPos(spanList[i]);
                int start2 = getSpanStartPos(span);
                int end1 = getSpanEndPos(spanList[i]);
                int end2 = getSpanEndPos(span);

                if (start1 >= start2 && start1 <= end2 || end1 >= start2 && end1 <= end2) {

                    getText().setSpan(span, Math.min(start1, start2), Math.max(end1, end2), getText().getSpanFlags(span));
                    getText().removeSpan(spanList[i]);

                    spanList[i] = null;
                    i = 0;
                }
            }
        }

        boolean fReplaced = false;
        for (int i = 0; i < spanList.length; i ++) {
            if (spanList[i] == null) {
                spanList[i] = span;
                fReplaced = true;
                break;
            }
        }

        if (!fReplaced) {
            CharacterStyle[] newList = new CharacterStyle[2 * spanList.length];
            System.arraycopy(spanList, 0, newList, 0, spanList.length);
            spanList = newList;
        }

        return spanList;
    }

    public void resetSpan(int start, int end) {
        ReplacementSpan[] replaceSpans = getText().getSpans(start, end, ReplacementSpan.class);

        for (int i = 0; i < replaceSpans.length; ++ i) {
            int spanStart = getSpanStartPos(replaceSpans[i]);
            int spanEnd = getSpanEndPos(replaceSpans[i]);
            int spanFlag = getText().getSpanFlags(replaceSpans[i]);

            getText().removeSpan(replaceSpans[i]);
            getText().setSpan(replaceSpans[i], spanStart, spanEnd, spanFlag);
        }

        TBulletStyleSpan[] alignSpans = getText().getSpans(start, end, TBulletStyleSpan.class);
        for (int i = 0; i < alignSpans.length; ++ i) {
            int spanStart = getSpanStartPos(alignSpans[i]);
            int spanEnd = getSpanEndPos(alignSpans[i]);
            int spanFlag = getText().getSpanFlags(alignSpans[i]);

            getText().removeSpan(alignSpans[i]);
            getText().setSpan(alignSpans[i], spanStart, spanEnd, spanFlag);
        }
    }

    private void resetSpan(int start) {
        TNoteSpanList spanList = clearSpanWatcher();
        CharacterStyle[] styleSpans = getText().getSpans(start, start, CharacterStyle.class);

        for (int i = 0; i < styleSpans.length; ++ i) {

            if (!(styleSpans[i] instanceof ReplacementSpan) && !(styleSpans[i] instanceof TLinkClickSpan)) {
                getText().setSpan(styleSpans[i], getSpanStartPos(styleSpans[i]), getSpanEndPos(styleSpans[i]), Spanned.SPAN_POINT_POINT);
            }
        }

        resetSpanList(spanList);
    }

    private void sendRefreshStyleCommandMsg() {
        if (mHandler != null) {
            mHandler.sendMessage(mHandler.obtainMessage(2000));
        }
    }

    private void j() {
        Object[] spans = getText().getSpans(0, getText().length(), TNoteHashMap.class);

        if (spans.length > 0) {
            getText().getSpanStart(spans[0]);
            getText().getSpanEnd(spans[0]);
            resetSpan();
            getText().removeSpan(spans[0]);
        }

        if (mSpanMap != null) {
            mSpanMap.clear();
        }
        
        sendRefreshStyleCommandMsg();
    }

    private TNoteSpanList clearSpanWatcher() {

        TNoteSpanList spanList = new TNoteSpanList();

        spanList.spans = getText().getSpans(0, this.getText().length(), SpanWatcher.class);

        if (spanList.spans.length > 0) {

            spanList.start = new int[spanList.spans.length];
            spanList.end   = new int[spanList.spans.length];
            spanList.flags = new int[spanList.spans.length];

            for (int i = 0; i < spanList.spans.length; ++ i) {
                spanList.start[i] = getSpanStartPos(spanList.spans[i]);
                spanList.end[i]   = getSpanEndPos(spanList.spans[i]);
                spanList.flags[i] = getText().getSpanFlags(spanList.spans[i]);

                getText().removeSpan(spanList.spans[i]);
            }
        }

        return spanList;
    }

    public void resetSpan() {
        TNoteSpanList spanList = clearSpanWatcher();
        HashMap hashMap = new HashMap();
        CharacterStyle[] styleSpans = getText().getSpans(0, getText().length(), CharacterStyle.class);

        for (int i = 0; i < styleSpans.length; ++ i) {
            if (!(styleSpans[i] instanceof ReplacementSpan)) {

                CharacterStyle[] spans = (CharacterStyle[]) hashMap.get(styleSpans[i].getClass());

                if (spans == null) {
                    hashMap.put(styleSpans[i].getClass(), new CharacterStyle[]{styleSpans[i], null});
                } else {
                    hashMap.put(styleSpans[i].getClass(), replaceStyleSpan(spans, styleSpans[i]));
                }
            }
        }

        resetSpanList(spanList);
    }

    private int[] getSelection() {
        int[] sel = new int[] {getSelectionStart(), getSelectionEnd()};

        if (sel[0] > sel[1]) {
            int temp = sel[0];

            sel[0] = sel[1];
            sel[1] = temp;
        }

        return sel;
    }

    public final SpannableStringBuilder getNoteEditContentFrom(String rootPath, SpannableStringBuilder editable) {

        int width = getWidth() - getPaddingLeft() - getPaddingRight();

        TLineDrawSpan[] lineSpans = editable.getSpans(0, editable.length(), TLineDrawSpan.class);
        for (int i = 0; i < lineSpans.length; ++ i) {
            lineSpans[i].setWidth(width);
        }

        TBitmapSpan[] bitmapSpans = editable.getSpans(0, editable.length(), TBitmapSpan.class);
        for (int i = 0; i < bitmapSpans.length; ++ i) {

            width = Math.min(getWidth(), 480);

//            bitmapSpans[i].mWidth = width - this.getPaddingLeft() - this.getPaddingRight();
            bitmapSpans[i].mFilePath = rootPath + bitmapSpans[i].mFilePath;
            bitmapSpans[i].initBitmapSpan(getText(), editable, this);
        }

        return editable;
    }

    public final String getHtmlString(String title, long ctime, long utime, String uuid) {

        StringBuilder sb = new StringBuilder();
        String body = TNoteContentBuilder.getContentFromSpannable((Spanned) getText().subSequence(0, getText().length()));

        sb.append("<html>");
        sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset= UTF-8\" />");
        sb.append("<meta create-time=\"" + ctime + "\" />");
        sb.append("<meta update-time=\"" + utime + "\" />");
        sb.append("<meta uuid=\"" + uuid + "\" />");
        sb.append("<meta cursor-start=\"" + getSelectionStart() + "\" />");
        sb.append("<meta cursor-end=\"" + getSelectionEnd() + "\" />");
        sb.append("<head>");
        sb.append("<title>");
        sb.append(title);
        sb.append("</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append(body);
        sb.append("</body>");
        sb.append("</html>");

        return sb.toString();
    }

    public final void recycleResources() {
        TBitmapSpan[] bmpSpans = getText().getSpans(0, this.getText().length(), TBitmapSpan.class);

        for (int i = 0;i < bmpSpans.length; ++ i) {
            bmpSpans[i].onDelete(getText(), true);
        }
    }

    public final void commandSetTextColor(int color) {
        int[] sel = getSelection();

        clearSpan(ForegroundColorSpan.class, sel[0], sel[1]);

        if (sel[0] != sel[1]) {
            getText().setSpan(new ForegroundColorSpan(color), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
        } else {
            setHashMapSpan(ForegroundColorSpan.class, new ForegroundColorSpan(color), sel[0]);
        }
        
        sendRefreshStyleCommandMsg();
    }

    public final void commandToggleAlignment(Layout.Alignment align) {

        int[] sel = getSelection();
        int[] paraBoundRange = TBulletStyleSpan.getBoundParagraphRange(getText(), sel[0], sel[1]);
        Iterator<int[]> it = TBulletStyleSpan.getParagraphRangesIn(getText(), paraBoundRange[0], paraBoundRange[1]).iterator();

        //Reset all align spans in paragraph
        while (it.hasNext()) {

            int[] paraRange = it.next();
            AlignmentSpan[] alignSpans = getText().getSpans(paraRange[0], paraRange[1], AlignmentSpan.class);

            if (alignSpans.length > 0) {
                getText().removeSpan(alignSpans[0]);
            }

            getText().setSpan(new AlignmentSpan.Standard(align), paraRange[0], paraRange[1], Spanned.SPAN_MARK_POINT);
            
//            TBulletStyleSpan[] bulletSpan = getText().getSpans(paraRange[0], paraRange[1], TBulletStyleSpan.class);
//            if (bulletSpan.length > 0) {
//            	Log.e("commandToogleAlignment", paraRange[0]+"~"+paraRange[1]+":"+align.toString());
//            	
//                getText().removeSpan(bulletSpan[0]);
//                bulletSpan[0].setAlignment(align);
//                getText().setSpan(bulletSpan[0], paraRange[0], paraRange[1], Spanned.SPAN_MARK_POINT);
//            }
        }

        //Paragraph is not a first paragraph, reset all align spans before selection
        if (paraBoundRange[0] != 0) {
            AlignmentSpan[] spans = getText().getSpans(0, paraBoundRange[0] - 1, AlignmentSpan.class);

            if(spans.length > 0) {
                Arrays.sort(spans, new Comparator<AlignmentSpan>() {
                    @Override
                    public int compare(AlignmentSpan lhs, AlignmentSpan rhs) {
                        return getSpanStartPos(lhs) - getSpanStartPos(rhs);
                    }
                });

                for (int i = spans.length - 1; i >= 0; -- i) {
                    int start = getSpanStartPos(spans[i]);
                    int end = getSpanEndPos(spans[i]);

                    getText().removeSpan(spans[i]);
                    getText().setSpan(spans[i], start, end, Spanned.SPAN_MARK_POINT);
                }
            }
        }
        
        sendRefreshStyleCommandMsg();
    }

    public final void commandToggleBullet(TBulletStyleSpan.TBulletType bulletType, TBulletStyleSpan.TBulletSortType sortType, TBulletStyleSpan.TBulletShapeType shapeType) {
        int sel[] = getSelection();
        TBulletStyleSpan.toggleBullet(getText(), sel[0], sel[1], bulletType, sortType, shapeType);
        
        sendRefreshStyleCommandMsg();
    }

    public final void insertLinkItem(String s, String s1) {
        int sel[] = getSelection();
        Spanned spanned = TBitmapSpan.getBitmapSpanned(s, this);
        getText().replace(sel[0], sel[1], TLinkClickSpan.a(spanned, s1));
    }

    public final void commandIndent(boolean fRight) {

        int sel[] = getSelection();
        TBulletStyleSpan.toggleIndent(getText(), sel[0], sel[1], fRight);
        RelativeSizeSpan relativesizespan = new RelativeSizeSpan(0.999F);

//        if (sel[1] >= getText().length()) sel[1] = getText().length();

        sel = getSelection();

        getText().setSpan(relativesizespan, sel[0], sel[1], Spanned.SPAN_POINT_POINT);
        getText().removeSpan(relativesizespan);
        
        sendRefreshStyleCommandMsg();
    }

    public final boolean insertBitmap(String filePath) {

        int sel[] = getSelection();
        getText().replace(sel[0], sel[1], TBitmapSpan.getBitmapSpanned(filePath, this));
        
        return true;
    }

    public final TNoteParagraphStyle updateParagraphStyle() {

        int[] sel = getSelection();

        TNoteParagraphStyle paragraphStyle = mCurParagraphStyle;

        paragraphStyle.bold = false;
        paragraphStyle.italic = false;
        paragraphStyle.underline = false;
        paragraphStyle.foregroundColor = 0xff000000;
        paragraphStyle.backgroundColor = 0xffffffff;
        paragraphStyle.textSize = (int) getTextSize();
        paragraphStyle.strikeThrough = false;
        paragraphStyle.superScript = false;
        paragraphStyle.subScript = false;
        paragraphStyle.textAlign = Layout.Alignment.ALIGN_NORMAL;
        paragraphStyle.mBulletType = 0;
        paragraphStyle.fontFamilyUri = "";

        //No Selection
        if (sel[0] == sel[1]) {

            //Get current paragraph spans
            ParagraphStyle[] paragraphSpans = getText().getSpans(sel[0], sel[0], ParagraphStyle.class);
            for (int i = 0; i < paragraphSpans.length; ++i) {

                if (paragraphSpans[i] instanceof TBulletStyleSpan) {

                    if (((TBulletStyleSpan) paragraphSpans[i]).mBulletType == TBulletStyleSpan.TBulletType.SORT) {
                        mCurParagraphStyle.mBulletType = 1;
                    }
                    else if(((TBulletStyleSpan) paragraphSpans[i]).mBulletType == TBulletStyleSpan.TBulletType.SHAPE) {
                        mCurParagraphStyle.mBulletType = 2;
                    }
                }
                else if(paragraphSpans[i] instanceof AlignmentSpan) {
                    mCurParagraphStyle.textAlign = ((AlignmentSpan) paragraphSpans[i]).getAlignment();
                }
            }
        }

        //Get character style spans
        CharacterStyle[] characterSpans = getText().getSpans(sel[0], sel[1], CharacterStyle.class);
        for (int i = 0; i < characterSpans.length; ++i) {

            //Character style contains cursor
            if (getSpanStartPos(characterSpans[i]) != sel[1]) {

                if (TBoldStyleSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.bold = true;
                }
                else if (TItalicStyleSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.italic = true;
                }
                else if (UnderlineSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.underline = true;
                }
                else if (ForegroundColorSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.foregroundColor = ((ForegroundColorSpan)characterSpans[i]).getForegroundColor();
                }
                else if (BackgroundColorSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.backgroundColor = ((BackgroundColorSpan)characterSpans[i]).getBackgroundColor();
                }
                else if (AbsoluteSizeSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.textSize = ((AbsoluteSizeSpan)characterSpans[i]).getSize();
                }
                else if (TFontfaceSpan.class == characterSpans[i].getClass()) {
                    mCurParagraphStyle.fontFamilyUri = ((TFontfaceSpan)characterSpans[i]).getFontUri();
                }
                else if (characterSpans[i].getClass() == TSuperscriptSpan.class) {
                    mCurParagraphStyle.superScript = true;
                }
                else if (characterSpans[i].getClass() == TSubscriptSpan.class) {
                    mCurParagraphStyle.subScript = true;
                }
                else if (characterSpans[i].getClass() == StrikethroughSpan.class) {
                    mCurParagraphStyle.strikeThrough = true;
                }
            }
        }

        //Update map style
        if (sel[0] == sel[1] && (getText().getSpans(sel[0], sel[1], TNoteHashMap.class)).length > 0) {

            Object span;

            span = this.mSpanMap.map.get(TBoldStyleSpan.class);
            if (span != null) {
                mCurParagraphStyle.bold = span != mSpan;
            }

            span = this.mSpanMap.map.get(TItalicStyleSpan.class);
            if (span != null) {
                mCurParagraphStyle.italic = span != mSpan;
            }

            span = this.mSpanMap.map.get(UnderlineSpan.class);
            if (span != null) {
                mCurParagraphStyle.underline = span != mSpan;
            }

            span = this.mSpanMap.map.get(StrikethroughSpan.class);
            if (span != null) {
                mCurParagraphStyle.strikeThrough = span != mSpan;
            }

            span = this.mSpanMap.map.get(TSuperscriptSpan.class);
            if (span != null) {
                mCurParagraphStyle.superScript = span != mSpan;
            }

            span = this.mSpanMap.map.get(TSubscriptSpan.class);
            if (span != null) {
                mCurParagraphStyle.subScript = span != mSpan;
            }

            span = this.mSpanMap.map.get(ForegroundColorSpan.class);
            if (span != null) {
                mCurParagraphStyle.foregroundColor = ((ForegroundColorSpan)span).getForegroundColor();
            }

            span = this.mSpanMap.map.get(BackgroundColorSpan.class);
            if (span != null) {
                mCurParagraphStyle.backgroundColor = ((BackgroundColorSpan)span).getBackgroundColor();
            }

            span = this.mSpanMap.map.get(AbsoluteSizeSpan.class);
            if (span != null) {
                mCurParagraphStyle.backgroundColor = ((AbsoluteSizeSpan)span).getSize();
            }

            span = this.mSpanMap.map.get(TFontfaceSpan.class);
            if (span != null) {
                mCurParagraphStyle.fontFamilyUri= ((TFontfaceSpan)span).getFontUri();
            }
        }

        return mCurParagraphStyle;
    }

    public final void commandSetBackColor(int color) {

        int[] sel = getSelection();

        clearSpan(BackgroundColorSpan.class, sel[0], sel[1]);

        if (color != -1) {

            if (sel[0] == sel[1]) {
                setHashMapSpan(BackgroundColorSpan.class, new BackgroundColorSpan(color), sel[0]);
            }
            else {
                getText().setSpan(new BackgroundColorSpan(color), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
            }
        }
    }

    public final void b(boolean flag) {

        TBitmapSpan[] var2 = getText().getSpans(0, getText().length(), TBitmapSpan.class);

        for (int i = 0; i < var2.length; ++ i) {

            var2[i].setThumbState(flag);

            int spanStart = getSpanStartPos(var2[i]);
            int spanEnd = getSpanEndPos(var2[i]);

            getText().removeSpan(var2[i]);
            getText().setSpan(var2[i], spanStart, spanEnd, Spanned.SPAN_POINT_MARK);
        }

        setCursorVisible(!flag);
        setFocusableInTouchMode(!flag);
        setFocusable(!flag);
        setBackgroundColor(flag ? getContext().getResources().getColor(R.color.gray_backcolor) : 0xffffffff);
    }

    public final void commandClearStyle() {

        int[] sel = getSelection();

        //Remove all Character style spans
        CharacterStyle[] characterSpans = getText().getSpans(sel[0], sel[1], CharacterStyle.class);
        for (int i = 0; i < characterSpans.length; ++ i) {

            if (!(characterSpans[i] instanceof ReplacementSpan)) {
                getText().removeSpan(characterSpans[i]);
            }
        }

        //Remove all Paragraph style spans
        ParagraphStyle[] paragraphSpans = getText().getSpans(sel[0], sel[1], ParagraphStyle.class);
        for (int i = 0; i < paragraphSpans.length; ++ i) {

            if (paragraphSpans[i] instanceof TBulletStyleSpan) {
                getText().delete(getSpanStartPos(paragraphSpans[i]), getSpanStartPos(paragraphSpans[i]) + 1);
            }

            getText().removeSpan(paragraphSpans[i]);
        }
    }

    public final void commandSetFontface(String fontUri) {
        int[] sel = getSelection();

        clearSpan(TFontfaceSpan.class, sel[0], sel[1]);
        if (sel[0] != sel[1]) {
            getText().setSpan(new TFontfaceSpan(fontUri), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
            resetSpan(sel[0], sel[1]);
        }
        else {
            setHashMapSpan(TFontfaceSpan.class, new TFontfaceSpan(fontUri), sel[0]);
        }
        
        sendRefreshStyleCommandMsg();
    }

    public final void commandSetTextSize(int size) {

        int[] sel = getSelection();

        clearSpan(AbsoluteSizeSpan.class, sel[0], sel[1]);

        if (sel[0] != sel[1]) {
            getText().setSpan(new AbsoluteSizeSpan(size), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
            resetSpan(sel[0], sel[1]);
        } else {
            setHashMapSpan(AbsoluteSizeSpan.class, new AbsoluteSizeSpan(size), sel[0]);
        }
        
        sendRefreshStyleCommandMsg();
    }

    public final void commandToggleBold() {

        int[] sel = getSelection();
        boolean fToggled = getToggleSpanValue(TBoldStyleSpan.class);

        //If Current Bold style is toggled and selection is not none, then set selection's style to bold
        if (fToggled) {

            clearSpan(TBoldStyleSpan.class, sel[0], sel[1]);
            if (sel[0] != sel[1]) {
                sendRefreshStyleCommandMsg();
                return;
            }
        }

        //Set Bold Style
        if (sel[0] != sel[1]) {
            getText().setSpan(new TBoldStyleSpan(), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
        } else {
            setHashMapSpan(TBoldStyleSpan.class, fToggled ? mSpan : new TBoldStyleSpan(), sel[0]);
        }

        sendRefreshStyleCommandMsg();
    }

    public final void commandToggleItalic() {

        int[] sel = getSelection();
        boolean fToggled = getToggleSpanValue(TItalicStyleSpan.class);

        //If Current Bold style is toggled and selection is not none, then set selection's style to bold
        if (fToggled) {

            clearSpan(TItalicStyleSpan.class, sel[0], sel[1]);
            if(sel[0] != sel[1]) {
                sendRefreshStyleCommandMsg();
                return;
            }
        }

        if (sel[0] != sel[1]) {
            getText().setSpan(new TItalicStyleSpan(), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
        } else {
            setHashMapSpan(TItalicStyleSpan.class, fToggled ? mSpan : new TItalicStyleSpan(), sel[0]);
        }

        sendRefreshStyleCommandMsg();
    }

    public final void commandToggleUnderline() {

        int[] sel = getSelection();
        boolean fToggled = getToggleSpanValue(UnderlineSpan.class);

        //If Current Bold style is toggled and selection is not none, then set selection's style to bold
        if (fToggled) {

            clearSpan(UnderlineSpan.class, sel[0], sel[1]);
            if (sel[0] != sel[1]) {
                sendRefreshStyleCommandMsg();
                return;
            }
        }

        if (sel[0] != sel[1]) {
            getText().setSpan(new UnderlineSpan(), sel[0], sel[1], Spanned.SPAN_POINT_POINT);
        } else {
            setHashMapSpan(UnderlineSpan.class, fToggled ? mSpan : new UnderlineSpan(), sel[0]);
        }

        sendRefreshStyleCommandMsg();
    }

    public final void commandInsertLine() {

        int selEnd = getSelection()[1];
        boolean fFind = false;

        //Find first Enter key from cursor
        for (int i = selEnd; i < getText().length(); i ++) {

            if (getText().charAt(i) == 10) {
                selEnd = i;
                fFind = true;
                break;
            }

            selEnd = i;
        }

        //There is no Linebreak, then add Linebreak to the end
        if (!fFind) {

            if (selEnd > 0 && getText().charAt(selEnd - 1) != 10) {
                getText().append('\n');
            }

            selEnd = getText().length();
            getText().append('\n');
        }

        //There is a Linebreak,
        else if (selEnd != 0 && getText().charAt(selEnd - 1) != 10) {

            selEnd ++;
            if (selEnd == getText().length() || (selEnd < getText().length() && getText().charAt(selEnd) != 10)) {
                getText().insert(selEnd, "\n");
            }
        }

        getText().insert(selEnd, TLineDrawSpan.getLineDrawSpannable(this));
        
        sendRefreshStyleCommandMsg();
    }

    public final String h() {
        return getText().toString().replace("\uFFFC", "").replace("\n", " ");
    }

    protected void onCreateContextMenu(ContextMenu contextmenu) {
        if (isFocusable())
            super.onCreateContextMenu(contextmenu);
    }

    @SuppressWarnings("deprecation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isFocusable()) {
            Paint paint = new Paint();
            int sel[] = getSelection();
            Path path = new Path();

            canvas.getClipBounds();
            getLayout().getSelectionPath(sel[0], sel[1], path);

//            if (sel[0] != sel[1]) {
//                canvas.save();
//                canvas.translate(getTotalPaddingLeft(), getTotalPaddingTop());
////                paint.setXfermode(new PixelXorXfermode(0xffffff));
//                canvas.drawPath(path, paint);
//                canvas.restore();
//            }
        } else if (mPath != null) {
            mPainter.setStyle(android.graphics.Paint.Style.FILL);
            mPainter.setColor(getResources().getColor(R.color.gray_backcolor));
            mPainter.setAlpha(136);
            canvas.drawPath(mPath, mPainter);
        }
        
        if (AppConstants.LINE_NUMBER) {
    		Paint mPaint = new Paint();
    		mPaint.setTypeface(Typeface.SERIF);
    		mPaint.setAntiAlias(true);
    		mPaint.setTextSize(24.0f);
    		mPaint.setColor(Color.GRAY);
        	
        	Rect drawingRect = new Rect();
			Rect textBounds = new Rect();
            getDrawingRect(drawingRect);
            
            int margin = 5;
            int right = getPaddingLeft() - margin*2;
            Rect mLineBounds = new Rect();
        	int count = getLineCount();
    		int min = 1;
    		int max = count;
    		
			int bottom = getLineBounds(max-1, mLineBounds) + getPaddingTop()*2;
    		mPaint.getTextBounds("4", 0, 1, textBounds);
    		
    		int[] lines = getStartPositionOfLines();
    		for (int i=0; i<lines.length; i++) {
    			int lll = getLine(lines[i]);
    			String t = ""+(i+1);
    			int offset = 0;
    			int baseline = getLineBounds(lll, mLineBounds);
    			canvas.drawText(t, right-margin*2-textBounds.width()*t.length(), baseline-offset, mPaint);
    		}
    		
    		// Draw Line 
//			canvas.drawLine(right, drawingRect.top, right, bottom, mPaint);
        }
    }
    
    

    public boolean onKeyDown(int keycode, KeyEvent keyevent) {
        if (keycode == KeyEvent.KEYCODE_DEL) {
            int sel[] = getSelection();
            if (TBulletStyleSpan.a(this, sel))
                return true;

            if (sel[0] == sel[1]) {
                AlignmentSpan[] alignSpans = getText().getSpans(sel[0], sel[0], AlignmentSpan.class);
                if (alignSpans.length > 0 && getText().getSpanStart(alignSpans[0]) == sel[0] && alignSpans[0].getAlignment() != Layout.Alignment.ALIGN_NORMAL) {
                    getText().removeSpan(alignSpans[0]);
                    sendRefreshStyleCommandMsg();
                    return true;
                }
            }
        }

        return super.onKeyDown(keycode, keyevent);
    }

    public Parcelable onSaveInstanceState() {
//        j();
        return super.onSaveInstanceState();
    }

    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        j();
        TBulletStyleSpan.a(this, selStart, selEnd);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw) {

            int width = getWidth() - getPaddingLeft() - getPaddingRight();
            TLineDrawSpan[] var7 = getText().getSpans(0, getText().length(), TLineDrawSpan.class);

            for (int i = 0; i < var7.length; ++i) {
                var7[i].setWidth(width);
            }
        }
        sendRefreshStyleCommandMsg();
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {

        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        CharacterStyle[] characterSpans = getText().getSpans(0, this.getText().length(), CharacterStyle.class);

        for (int i = 0; i < characterSpans.length; ++i) {

            if (getText().getSpanStart(characterSpans[i]) == getText().getSpanEnd(characterSpans[i])) {
                getText().removeSpan(characterSpans[i]);
            }
        }

        refreshEditText(start, lengthAfter);
        TBulletStyleSpan.a(this);
        resetSpan();

        int end = start + lengthAfter;
        int[] range1 = TBulletStyleSpan.getBoundParagraphRange(getText(), start, end);
        ArrayList var9 = TBulletStyleSpan.getParagraphRangesIn(getText(), range1[0], range1[1]);

        for (int i = 0; i < var9.size(); ++ i) {
            int[] range2 = (int[])var9.get(i);
            AlignmentSpan[] alignSpans = getText().getSpans(range2[0], range2[1], AlignmentSpan.class);

            if (alignSpans.length == 0) {
                getText().setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL), range2[0], range2[1], Spanned.SPAN_MARK_POINT);
                
//                TBulletStyleSpan[] bulletSpan = getText().getSpans(range2[0], range2[1], TBulletStyleSpan.class);
//                if (bulletSpan.length>0){
//                	Log.e("NoteText TextChange", range2.toString() + "-- Left Alignment");
//                    getText().removeSpan(bulletSpan[0]);
//                	bulletSpan[0].setAlignment(Layout.Alignment.ALIGN_NORMAL);
//                    getText().setSpan(bulletSpan[0], range2[0], range2[1], Spanned.SPAN_MARK_POINT);
//                }
            } else {
                int spanStart = getText().getSpanStart(alignSpans[0]);
                int spanEnd = getText().getSpanEnd(alignSpans[0]);
                if (spanStart < range2[0] && spanEnd >= range2[1]) {
                    Layout.Alignment var15 = alignSpans[0].getAlignment();
                    getText().removeSpan(alignSpans[0]);

                    for (int j = i; j < var9.size(); ++ j) {

                        int[] range3 = (int[])var9.get(j);

                        if (range3[1] < spanStart) {
                            break;
                        }

                        getText().setSpan(new AlignmentSpan.Standard(var15), range3[0], range3[1], Spanned.SPAN_MARK_POINT);
                        
//                        TBulletStyleSpan[] bulletSpan = getText().getSpans(range3[0], range3[1], TBulletStyleSpan.class);
//                        if (bulletSpan.length>0){
//                        	Log.e("NoteText TextChange-2", range3.toString() + "-- " + var15.toString());
//                            getText().removeSpan(bulletSpan[0]);
//                        	bulletSpan[0].setAlignment(var15);
//                            getText().setSpan(bulletSpan[0], range3[0], range3[1], Spanned.SPAN_MARK_POINT);
//                        }
                    }
                } else {
                    getText().setSpan(alignSpans[0], range2[0], range2[1], Spanned.SPAN_MARK_POINT);
                    
//                    TBulletStyleSpan[] bulletSpan = getText().getSpans(range2[0], range2[1], TBulletStyleSpan.class);
//                    if (bulletSpan.length>0){
//                    	Log.e("NoteText TextChange-3", range2.toString() + "-- " + alignSpans[0].getAlignment().toString());
//                        getText().removeSpan(bulletSpan[0]);
//                    	bulletSpan[0].setAlignment(alignSpans[0].getAlignment());
//                        getText().setSpan(bulletSpan[0], range2[0], range2[1], Spanned.SPAN_MARK_POINT);
//                    }
                }
            }
        }

        sendRefreshStyleCommandMsg();
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        int[] sel = getSelection();
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Application.CLIPBOARD_SERVICE);

        switch (id) {
            case android.R.id.cut:
                if (sel[0] == sel[1]) {
                    sel[0] = 0;
                    sel[1] = getText().length();
                }

                clearComposingText();
                this.mClipboardText = TBulletStyleSpan.clearBulletSpans(getText().subSequence(sel[0], sel[1]));
                clipboard.setText(this.mClipboardText.toString());
                getText().delete(sel[0], sel[1]);
                break;

            case android.R.id.copy:
                if (sel[0] == sel[1]) {
                    sel[0] = 0;
                    sel[1] = getText().length();
                }

                clearComposingText();
                this.mClipboardText = TBulletStyleSpan.clearBulletSpans(getText().subSequence(sel[0], sel[1]));
                clipboard.setText(this.mClipboardText.toString());
                
                Log.e("COCOCOCOCOCOCOCCCCCCCCCCCCC", this.mClipboardText.toString());
                break;

            case android.R.id.paste:
                clearComposingText();
                CharSequence clipboardText = clipboard.getText();

                if (clipboardText != null) {

                    TNoteSpanList spanList = clearSpanWatcher();

                    clearSpan(CharacterStyle.class, sel[0], sel[1], true);
                    resetSpanList(spanList);
                    setSelection(sel[1]);

                    if (this.mClipboardText != null && clipboardText.toString().equals(this.mClipboardText.toString())) {

                        if (this.mClipboardText instanceof SpannableStringBuilder) {

                            CharacterStyle[] characterSpans = ((SpannableStringBuilder)this.mClipboardText).getSpans(0, this.mClipboardText.length(), CharacterStyle.class);

                            for (int i = 0; i < characterSpans.length; ++ i) {

                                Class spanClass = characterSpans[i].getClass();
                                Object span;

                                if (spanClass == ForegroundColorSpan.class) {
                                    span = new ForegroundColorSpan(((ForegroundColorSpan)characterSpans[i]).getForegroundColor());
                                } else if (spanClass == BackgroundColorSpan.class) {
                                    span = new BackgroundColorSpan(((BackgroundColorSpan)characterSpans[i]).getBackgroundColor());
                                } else if (spanClass == AbsoluteSizeSpan.class) {
                                    span = new AbsoluteSizeSpan(((AbsoluteSizeSpan)characterSpans[i]).getSize());
                                } else if (spanClass == TFontfaceSpan.class) {
                                    span = new TFontfaceSpan(((TFontfaceSpan)characterSpans[i]).getFontUri());
                                } else if (spanClass == TBoldStyleSpan.class) {
                                    span = new TBoldStyleSpan();
                                } else if (spanClass == TItalicStyleSpan.class) {
                                    span = new TItalicStyleSpan();
                                } else if (spanClass == UnderlineSpan.class) {
                                    span = new UnderlineSpan();
                                } else if (spanClass == StrikethroughSpan.class) {
                                    span = new StrikethroughSpan();
                                } else if (spanClass == TSuperscriptSpan.class) {
                                    span = new TSuperscriptSpan();
                                } else if (spanClass == TSubscriptSpan.class) {
                                    span = new TSubscriptSpan();
                                } else if (spanClass == TLineDrawSpan.class) {
                                    span = new TLineDrawSpan(((TLineDrawSpan)characterSpans[i]).getColor(), getWidth() - getPaddingLeft() - getPaddingRight());
                                } else if (spanClass == TBitmapSpan.class) {
                                    span = ((TBitmapSpan)characterSpans[i]).duplicate(this.getText(), (SpannableStringBuilder) this.mClipboardText);
                                } else {
                                    span = characterSpans[i];
                                }

                                int spanStart = ((SpannableStringBuilder)this.mClipboardText).getSpanStart(characterSpans[i]);
                                int spanEnd = ((SpannableStringBuilder)this.mClipboardText).getSpanEnd(characterSpans[i]);
                                int spanFlag = ((SpannableStringBuilder)this.mClipboardText).getSpanFlags(characterSpans[i]);

                                ((SpannableStringBuilder)this.mClipboardText).removeSpan(characterSpans[i]);
                                if (span != null) {
                                	
                                	if (spanStart>spanEnd) {
                                		int w = spanStart;
                                		spanStart = spanEnd;
                                		spanEnd = w;
                                	}
                                	
                                    ((SpannableStringBuilder)this.mClipboardText).setSpan(span, spanStart, spanEnd, spanFlag);
                                }
                            }
                        }

                        Log.e("PPPPPPPPPPP", "="+this.mClipboardText.toString()+"=");
                        if (isFirstPositionOfLine(sel[0])){
                        	getText().replace(sel[0], sel[1], this.mClipboardText);
                        	resetSpan(sel[0], sel[0] + this.mClipboardText.length());
                        } else {
                        	getText().replace(sel[0], sel[1], " " + this.mClipboardText);
                        	resetSpan(sel[0], sel[0] + 1 + this.mClipboardText.length());
                        }
                    } else {
                    	if (isFirstPositionOfLine(sel[0])){
                    		getText().replace(sel[0], sel[1], clipboardText);
                    	} else {
                    		getText().replace(sel[0], sel[1], " " + clipboardText);
                    	}
                    }

                    resetSpan(sel[0]);
                }
                break;
            default:
                return super.onTextContextMenuItem(id);
        }

        sendRefreshStyleCommandMsg();
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (!this.isFocusable()) {

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                TLinkClickSpan[] var10 = getText().getSpans(0, getText().length(), TLinkClickSpan.class);

                for (int i = 0; i < var10.length; i ++) {

                    Path var14 = new Path();

                    getLayout().getSelectionPath(getSpanStartPos(var10[i]), getSpanEndPos(var10[i]), var14);
                    var14.offset((float) getTotalPaddingLeft(), (float) getTotalPaddingTop());

                    if (!var14.isEmpty()) {

                        RectF var15 = new RectF();

                        var14.computeBounds(var15, true);
                        if (var15.contains(event.getX(), event.getY())) {

                            this.mPath = var14;
                            this.mCurSpan = var10[i];
                            invalidate();
                            break;
                        }
                    }
                }

                TBitmapSpan[] var16 = getText().getSpans(0, getText().length(), TBitmapSpan.class);

                for (int j = 0; j < var16.length; ++ j) {

                    Path var19 = new Path();

                    getLayout().getSelectionPath(getSpanStartPos(var16[j]), getSpanEndPos(var16[j]), var19);
                    var19.offset((float)this.getTotalPaddingLeft(), (float)this.getTotalPaddingTop());

                    if (!var19.isEmpty()) {

                        RectF var20 = new RectF();

                        var19.computeBounds(var20, true);
                        if (var20.contains(event.getX(), event.getY())) {

                            this.mPath = var19;
                            this.mCurSpan = var16[j];
                            invalidate();
                            return super.onTouchEvent(event);
                        }
                    }
                }

                if (this.mPath != null) {

                    this.mCurSpan = null;
                    this.mPath = null;
                    invalidate();
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                if (this.mPath != null) {

                    RectF var3 = new RectF();

                    this.mPath.computeBounds(var3, true);
                    if (var3.contains(event.getX(), event.getY())) {

                        if (this.mCurSpan instanceof TBitmapSpan) {

                            if (mHandler != null) {
                                mHandler.sendMessage(mHandler.obtainMessage(2001, ((TBitmapSpan) this.mCurSpan).getFilePath()));
                            }
                        } else if (this.mCurSpan instanceof TLinkClickSpan) {

                            if (mHandler != null) {
                                mHandler.sendMessage(mHandler.obtainMessage(2001, ((TLinkClickSpan) this.mCurSpan).getUrl()));
                            }
                        }

                        this.mCurSpan = null;
                        this.mPath = null;
                        invalidate();
                    }
                }

            } else if(event.getAction() == MotionEvent.ACTION_CANCEL && this.mPath != null) {
                this.mCurSpan = null;
                this.mPath = null;
            }
        }

        return super.onTouchEvent(event);
    }
    
    public void addText(String clipboardText){
        int[] sel = getSelection();
//        clearComposingText();

        if (clipboardText != null) {

//            TNoteSpanList spanList = clearSpanWatcher();

//            clearSpan(CharacterStyle.class, sel[0], sel[1], true);
//            resetSpanList(spanList);
//            setSelection(sel[1]);

            if (clipboardText.length()>0) {
                getText().replace(sel[0], sel[1], clipboardText);
                resetSpan(sel[0], sel[0] + clipboardText.length());
            } else {
                getText().replace(sel[0], sel[1], clipboardText);
            }

//            resetSpan(sel[0]);
        }
        
        sendRefreshStyleCommandMsg();
    }

    public void scrollTo(int x, int y) {

        if (x <= 0)
            super.scrollTo(x, y);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public final class TNoteSpanList {

        public SpanWatcher spans[];
        public int start[];
        public int end[];
        public int flags[];
    }

    public final class TNoteParagraphStyle {

        public boolean bold = false;
        public boolean italic = false;
        public boolean underline = false;
        public boolean strikeThrough = false;
        public boolean superScript = false;
        public boolean subScript = false;
        public int foregroundColor = 0xff000000;
        public int backgroundColor = 0xffffffff;
        public int textSize = 0;
        public Layout.Alignment textAlign = Layout.Alignment.ALIGN_NORMAL;
        public int mBulletType = 0;
        public String fontFamilyUri = "";
    }

    public class TNoteHashMap {

        public HashMap<Class, Object> map = new HashMap<Class, Object>();

        public final void clear() {
            map.clear();
        }

        public final void add(Class class1, Object obj) {
            map.put(class1, obj);
        }
    }

	public int getFirstIndexOfLine(int line) {
		// TODO Auto-generated method stub
		if (line<=0)
			return -1;
		
		String t = getText().toString();
		String[] lines = t.split("\n");
		if (line>lines.length)
			return -1;

		line--;
		int result = 0;
		for (int i=0; i<lines.length; i++){
			if (i==line){
				break;
			}
			
			result += lines[i].length()+1;
		}
		
		if (result>=t.length())
			result = t.length()-1;
		
		return result;
	}

	public int getLastIndexOfLine(int line) {
		// TODO Auto-generated method stub
		if (line<=0)
			return -1;
		
		String t = getText().toString()+"A";
		String[] lines = t.split("\n");
		if (line>lines.length)
			return -1;

		line--;
		int result = 0;
		for (int i=0; i<lines.length; i++){
			result += lines[i].length()+1;
			
			if (i==lines.length-1)
				result--;
			
			if (i==line){
				break;
			}
		}
		
		if (result>=t.length()-1)
			result = t.length()-2;
		
		return result;
	}
	
	public boolean isFirstPositionOfLine(int position) {
		String t = getText().toString()+"A";
		if (position>=t.length())
			return false;
		
		String[] lines = t.split("\n");
		int p = 0;
		for (int i=0; i<lines.length; i++){
			if (p==position)
				return true;
			
			if (p>position)
				break;
			
			p += lines[i].length();
			if (i!=lines.length-1)
				p++;
			else
				p--;
		}
		
		return false;
	}
	
	public int getRealLines(){
		String t = getText().toString();
		String[] lines = t.split("\n");
		return lines.length<1 ? 1 : lines.length;
	}

	public int[] getStartPositionOfLines(){
		String t = getText().toString()+"A";
		String[] lines = t.split("\n");
		int l = lines.length<1 ? 1 : lines.length;
		
//		if (t.length()>0 && t.substring(t.length()-1).equals("\n")){
//			l++;
//		}
		
		int p = 0;
		int[] result = new int[l];
		for (int i=0; i<l; i++) {
			result[i] = p;
			
			if (i<lines.length)
				p += lines[i].length();
			
			if (i!=lines.length-1)
				p++;
			else
				p--;
		}
		
		return result;
	}
	
	public int getLine(int position)
	{    
	    Layout layout = getLayout();
        return layout.getLineForOffset(position);
	}	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
//		final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
//		final int actualHeight = getHeight();
//		
//		Log.i("Measure Height", proposedheight + ", " + actualHeight);
//		if (proposedheight == actualHeight) {
//			if (isKeyboardShown) {
//				Log.e("Keyboard", "Hidden");
//				isKeyboardShown = false;
//			}
//		}
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
}
