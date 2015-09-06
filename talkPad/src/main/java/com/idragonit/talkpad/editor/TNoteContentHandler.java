package com.idragonit.talkpad.editor;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import com.idragonit.talkpad.editor.data.TNoteDataUtil;
import com.idragonit.talkpad.editor.style.TBitmapSpan;
import com.idragonit.talkpad.editor.style.TBoldStyleSpan;
import com.idragonit.talkpad.editor.style.TBulletStyleSpan;
import com.idragonit.talkpad.editor.style.TFontfaceSpan;
import com.idragonit.talkpad.editor.style.TItalicStyleSpan;
import com.idragonit.talkpad.editor.style.TLineDrawSpan;
import com.idragonit.talkpad.editor.style.TLinkClickSpan;
import com.idragonit.talkpad.editor.style.TSubscriptSpan;
import com.idragonit.talkpad.editor.style.TSuperscriptSpan;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Stack;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public final class TNoteContentHandler implements ContentHandler {

    private static final float[] mZoomList;
    private static HashMap<String, Integer> mColorMap;
    private int mBulletLevel = 0;
    private Stack mBulletStack = new Stack();
    private String mXmlContent;
    private XMLReader mXmlReader;
    private SpannableStringBuilder mSpannableBuilder = new SpannableStringBuilder();
    private boolean mMetaData = false;

    public TNoteContentHandler(String content, Parser parser) {
        mXmlContent = content;
        mXmlReader = parser;
    }

    private static int colorFromString(String strColor) {

        if (strColor == null)
            return 0xffffffff;

        String strName;
        int len, nSign, pos;
        char ch;
        byte radix;
        Integer color = mColorMap.get(strColor.toLowerCase());

        if (color != null)
            return color.intValue();

        try {
            strName = strColor.toString();
            len = strName.length();
            radix = 10;

            if (strName.charAt(0) == '-') {
                nSign = -1;
                pos = 1;
            } else {
                nSign = 1;
                pos = 0;
            }

            if (strName.charAt(pos) == '0') {

                if (pos == len - 1) {
                    return 0;
                }

                ch = strName.charAt(pos + 1);
                if (ch == 'x' || ch == 'X') {
                    pos = pos + 2;
                    radix = 16;
                } else {
                    pos = pos + 1;
                    radix = 8;
                }
            }
            else {

                ch = strName.charAt(pos);
                if (ch == '#') {
                    pos = pos + 1;
                    radix = 16;
                }
            }

            return nSign * Integer.parseInt(strName.substring(pos), radix);
        } catch (NumberFormatException numberformatexception) {
            return 0xffffffff;
        }
    }

    private static Object getLastSpan(Spanned spanned, Class spanClass) {

        Object[] spans = spanned.getSpans(0, spanned.length(), spanClass);
        if (spans.length == 0)
            return null;
        else
            return spans[spans.length - 1];
    }

    private static void appendLineBreak(SpannableStringBuilder editable) {

        int length = editable.length();
        if (length > 0 && editable.charAt(length - 1) == 10) {

            if (length < 2 || editable.charAt(length - 2) != 10) {
                editable.append("\n");
            }
        } else if (length != 0) {

            editable.append("\n");
        }
    }

    private static void replaceLastSpan(SpannableStringBuilder editable, Class spanClass, Object span) {

        Object lastSpan = getLastSpan(editable, spanClass);
        int start = editable.getSpanStart(lastSpan);
        int end = editable.length();

        editable.removeSpan(lastSpan);
        if (start != end)
            editable.setSpan(span, start, end, Spanned.SPAN_POINT_MARK);
    }

    private static void setTailSpan(SpannableStringBuilder editable, Object span) {

        int start = editable.length();
        editable.setSpan(span, start, start, Spanned.SPAN_MARK_MARK);
    }

    private static void setTailAttrSpan(SpannableStringBuilder editable, Attributes attrs) {

        String textColor = attrs.getValue("", "color");
        String textFace = attrs.getValue("", "face");
        String textSize = attrs.getValue("", "size");
        String textStyle = attrs.getValue("style");
        int length = editable.length();
        TNoteTextAttrSpannable textStyleSpan = new TNoteTextAttrSpannable();

        if (textColor != null && textColor.length() > 0) {
            textStyleSpan.mForeColor = colorFromString(textColor);
            textStyleSpan.mState |= TNoteTextAttrSpannable.StateForeColor;
        }
        if (textSize != null && textSize.length() > 0) {
            textStyleSpan.mSize = Integer.parseInt(textSize);
            textStyleSpan.mState |= TNoteTextAttrSpannable.StateTextSize;
        }
        if (textStyle != null && textStyle.length() > 0) {

            int pos = textStyle.toLowerCase().indexOf("font-size:");
            if (pos >= 0 && pos < textStyle.length()) {

                String strSize = textStyle.substring(pos + 10).replace(" ", "");

                int i;
                for(i = 0; i < strSize.length() && Character.isDigit(strSize.charAt(i)); ++ i) {
                    ;
                }

                textStyleSpan.mSize = Integer.parseInt(strSize.substring(0, i));
                textStyleSpan.mState |= TNoteTextAttrSpannable.StateTextSize;
            }

            pos = textStyle.toLowerCase().indexOf("background-color:");
            if (pos >= 0) {
                textStyleSpan.mBackColor = colorFromString(textStyle.substring(pos + 17).replace(" ", ""));
                textStyleSpan.mState |= TNoteTextAttrSpannable.StateBackColor;
            }

            pos = textStyle.toLowerCase().indexOf("font-family:");
            if (pos >= 0) {
                textStyleSpan.mTextFace = textStyle.substring(pos + 12).replace(" ", "");
                textStyleSpan.mState |= TNoteTextAttrSpannable.StateTextFace;
            }
        }

        editable.setSpan(textStyleSpan, length, length, Spanned.SPAN_MARK_MARK);
    }

    private static boolean existsSpanStartWith(SpannableStringBuilder editable, int start, Class spanClass) {

        Object[] spans = editable.getSpans(start, start, spanClass);
        if (spans != null && spans.length > 0) {

            for (int i = 0; i < spans.length; ++i) {

                if (editable.getSpanStart(spans[i]) == start) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void b(SpannableStringBuilder editable) {

        TNoteSizeRelSpannable lastSpan = (TNoteSizeRelSpannable) getLastSpan(editable, TNoteSizeRelSpannable.class);
        int start = editable.getSpanStart(lastSpan);
        int length = editable.length();

        editable.removeSpan(lastSpan);

        while (length > start && editable.charAt(length - 1) == 10) {
            -- length;
        }

        if (start != length) {

            editable.setSpan(new RelativeSizeSpan(mZoomList[TNoteSizeRelSpannable.getSize(lastSpan)]), start, length, Spanned.SPAN_POINT_MARK);
            editable.setSpan(new StyleSpan(1), start, length, Spanned.SPAN_POINT_MARK);
        }
    }

    public final Spanned getSpannableFromXml() {

        mXmlReader.setContentHandler(this);

        try {
            mXmlReader.parse(new InputSource(new StringReader(mXmlContent)));
        } catch (IOException ioexception) {
            throw new RuntimeException(ioexception);
        } catch (SAXException saxexception) {
            throw new RuntimeException(saxexception);
        }

        return mSpannableBuilder;
    }

    public final void characters(char[] chars, int start, int length) {

        if (!this.mMetaData) {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; ++i) {

                char ch = chars[i + start];
                if (ch != 32 && ch != 10) {

                    if (ch == 160) {
                        sb.append(' ');
                    } else {
                        sb.append(ch);
                    }
                } else {

                    int curLength = sb.length();
                    if (curLength == 0) {
                        int len = mSpannableBuilder.length();
                        if (len == 0) {
                            ch = 10;
                        } else {
                            ch = mSpannableBuilder.charAt(len - 1);
                        }
                    } else {
                        ch = sb.charAt(curLength - 1);
                    }

                    if (ch != 32 && ch != 10) {
                        sb.append(' ');
                    }
                }
            }

            mSpannableBuilder.append(sb);
        }
    }

    public final void endDocument() {
    }


    public final void endElement(String var1, String tag, String var3) {

        if (tag.equalsIgnoreCase("ul")) {
            mBulletLevel--;
            mBulletStack.pop();
        } else if (tag.equalsIgnoreCase("ol")) {
            mBulletLevel--;
            mBulletStack.pop();
        } else if (tag.equalsIgnoreCase("li")) {

            Object liSpan = getLastSpan(mSpannableBuilder, TNoteLiSpannable.class);
            int start = mSpannableBuilder.getSpanStart(liSpan);
            int length = mSpannableBuilder.length();

            mSpannableBuilder.removeSpan(liSpan);

            if (start == length) {
                mSpannableBuilder.append('\n');
            } else if (mSpannableBuilder.charAt(length - 1) != 10) {
                mSpannableBuilder.append('\n');
            }

            TBulletStyleSpan.a(mSpannableBuilder, start, length, mBulletLevel, ((Integer) mBulletStack.peek()).intValue());
        } else if (tag.equalsIgnoreCase("br")) {
            mSpannableBuilder.append("\n");
        } else if (tag.equalsIgnoreCase("p")) {
            appendLineBreak(mSpannableBuilder);
        } else if (tag.equalsIgnoreCase("div")) {

            TNoteDivSpannable divSpan = (TNoteDivSpannable) getLastSpan(mSpannableBuilder, TNoteDivSpannable.class);
            int start = mSpannableBuilder.getSpanStart(divSpan);
            int end = mSpannableBuilder.length();

            mSpannableBuilder.removeSpan(divSpan);

            if (start != end && !existsSpanStartWith(mSpannableBuilder, start, AlignmentSpan.class)) {

                Layout.Alignment align = Layout.Alignment.ALIGN_NORMAL;

                if (divSpan.mAlign.equals("center")) {
                    align = Layout.Alignment.ALIGN_CENTER;
                } else if (divSpan.mAlign.equals("right")) {
                    align = Layout.Alignment.ALIGN_OPPOSITE;
                }

                if (mSpannableBuilder.charAt(end - 1) != 10) {
                    mSpannableBuilder.append('\n');
                }

                mSpannableBuilder.setSpan(new AlignmentSpan.Standard(align), start, end, Spanned.SPAN_MARK_MARK);
            }
        } else if (tag.equalsIgnoreCase("em")) {
            replaceLastSpan(mSpannableBuilder, TNoteBoldSpannable.class, new TBoldStyleSpan());
        } else if (tag.equalsIgnoreCase("b")) {
            replaceLastSpan(mSpannableBuilder, TNoteBoldSpannable.class, new TBoldStyleSpan());
        } else if (tag.equalsIgnoreCase("strong")) {
            replaceLastSpan(mSpannableBuilder, TNoteItalicSpannable.class, new TItalicStyleSpan());
        } else if (tag.equalsIgnoreCase("cite")) {
            replaceLastSpan(mSpannableBuilder, TNoteItalicSpannable.class, new TItalicStyleSpan());
        } else if (tag.equalsIgnoreCase("dfn")) {
            replaceLastSpan(mSpannableBuilder, TNoteItalicSpannable.class, new TItalicStyleSpan());
        } else if (tag.equalsIgnoreCase("i")) {
            replaceLastSpan(mSpannableBuilder, TNoteItalicSpannable.class, new TItalicStyleSpan());
        } else if (tag.equalsIgnoreCase("big")) {
            replaceLastSpan(mSpannableBuilder, TNoteSizeBigSpannable.class, new RelativeSizeSpan(1.25F));
        } else if (tag.equalsIgnoreCase("small")) {
            replaceLastSpan(mSpannableBuilder, TNoteSizeSmallSpannable.class, new RelativeSizeSpan(0.8F));
        } else if (tag.equalsIgnoreCase("font")) {

            TNoteTextAttrSpannable fontSpannable = (TNoteTextAttrSpannable) getLastSpan(mSpannableBuilder, TNoteTextAttrSpannable.class);
            int start = mSpannableBuilder.getSpanStart(fontSpannable);
            int end = mSpannableBuilder.length();

            mSpannableBuilder.removeSpan(fontSpannable);

            if (start != end) {

                if ((fontSpannable.mState & TNoteTextAttrSpannable.StateForeColor) != 0) {

                    if (!existsSpanStartWith(mSpannableBuilder, start, ForegroundColorSpan.class)) {
                        mSpannableBuilder.setSpan(new ForegroundColorSpan(0xff000000 | fontSpannable.mForeColor), start, end, Spanned.SPAN_POINT_MARK);
                    }
                }
                if ((fontSpannable.mState & TNoteTextAttrSpannable.StateTextSize) != 0) {

                    if (!existsSpanStartWith(mSpannableBuilder, start, AbsoluteSizeSpan.class)) {
                        mSpannableBuilder.setSpan(new AbsoluteSizeSpan(fontSpannable.mSize), start, end, Spanned.SPAN_POINT_MARK);
                    }
                }
                if ((fontSpannable.mState & TNoteTextAttrSpannable.StateBackColor) != 0) {

                    if (!existsSpanStartWith(mSpannableBuilder, start, BackgroundColorSpan.class)) {
                        mSpannableBuilder.setSpan(new BackgroundColorSpan(0xff000000 | fontSpannable.mBackColor), start, end, Spanned.SPAN_POINT_MARK);
                    }
                }
                if ((fontSpannable.mState & TNoteTextAttrSpannable.StateTextFace) != 0) {

                    if (!existsSpanStartWith(mSpannableBuilder, start, TFontfaceSpan.class)) {
                        mSpannableBuilder.setSpan(new TFontfaceSpan(fontSpannable.mTextFace), start, end, Spanned.SPAN_POINT_MARK);
                    }
                }
            }
        } else if (tag.equalsIgnoreCase("blockquote")) {
            appendLineBreak(mSpannableBuilder);
            replaceLastSpan(mSpannableBuilder, TNoteQuoteSpannable.class, new QuoteSpan());
        } else if (tag.equalsIgnoreCase("tt")) {
            replaceLastSpan(mSpannableBuilder, TNoteTypeFaceSpannable.class, new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {

            TNoteHRefSpannable hrefSpannable = (TNoteHRefSpannable) getLastSpan(mSpannableBuilder, TNoteHRefSpannable.class);
            int start = mSpannableBuilder.getSpanStart(hrefSpannable);
            int end = mSpannableBuilder.length();

            mSpannableBuilder.removeSpan(hrefSpannable);

            if (start != end) {

                if (hrefSpannable.mUrl != null) {
                    mSpannableBuilder.setSpan(new TLinkClickSpan(hrefSpannable.mUrl), start, end, Spanned.SPAN_POINT_MARK);
                }
            }
        } else if (tag.equalsIgnoreCase("u")) {
            replaceLastSpan(mSpannableBuilder, TNoteUnderlineSpannable.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            replaceLastSpan(mSpannableBuilder, TNoteSuperscriptSpannable.class, new TSuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            replaceLastSpan(mSpannableBuilder, TNoteSubscriptSpannable.class, new TSubscriptSpan());
        } else if (tag.equalsIgnoreCase("style")) {
            this.mMetaData = false;
        } else if (tag.equalsIgnoreCase("script")) {
            this.mMetaData = false;
        } else if (tag.equalsIgnoreCase("head")) {
            this.mMetaData = false;
        } else if (tag.equalsIgnoreCase("title")) {
            this.mMetaData = false;
        } else if (tag.length() == 2 && Character.toLowerCase(tag.charAt(0)) == 104 && tag.charAt(1) >= 49 && tag.charAt(1) <= 54) {
            appendLineBreak(mSpannableBuilder);
//            b(mSpannableBuilder);
        }
    }

    public final void endPrefixMapping(String s1) {
    }

    public final void ignorableWhitespace(char ac[], int i1, int j1) {
    }

    public final void processingInstruction(String s1, String s2) {
    }

    public final void setDocumentLocator(Locator locator) {
    }

    public final void skippedEntity(String s1) {
    }

    public final void startDocument() {
    }

    public final void startElement(String uri, String tag, String qName, Attributes atts) {

        if (tag.equalsIgnoreCase("ul")) {

            String style = atts.getValue("style");

            mBulletLevel ++;

            if (style != null && style.length() != 0) {

                if (style.contains("list-style-type:none")) {
                    mBulletStack.push(new Integer(0));
                }
            } else {
                mBulletStack.push(new Integer(1));
            }
        }
        else if (tag.equalsIgnoreCase("ol")) {

            mBulletLevel ++;
            mBulletStack.push(new Integer(2));
        }
        else if (tag.equalsIgnoreCase("li")) {

            int end = mSpannableBuilder.length();
            mSpannableBuilder.setSpan(new TNoteLiSpannable((byte) 0), end, end, Spanned.SPAN_MARK_MARK);
        }
//        else if (tag.equalsIgnoreCase("br")) {
//            appendLineBreak(mSpannableBuilder);
//        }
        else if (tag.equalsIgnoreCase("p")) {
            appendLineBreak(mSpannableBuilder);
        }
        else if (tag.equalsIgnoreCase("div")) {

            String strAlign = atts.getValue("align");
            int end = mSpannableBuilder.length();

            mSpannableBuilder.setSpan(new TNoteDivSpannable(strAlign), end, end, 17);
        }
        else if (tag.equalsIgnoreCase("em")) {
            setTailSpan(mSpannableBuilder, new TNoteBoldSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("b")) {
            setTailSpan(mSpannableBuilder, new TNoteBoldSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("strong")) {
            setTailSpan(mSpannableBuilder, new TNoteItalicSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("cite")) {
            setTailSpan(mSpannableBuilder, new TNoteItalicSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("dfn")) {
            setTailSpan(mSpannableBuilder, new TNoteItalicSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("i")) {
            setTailSpan(mSpannableBuilder, new TNoteItalicSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("big")) {
            setTailSpan(mSpannableBuilder, new TNoteSizeBigSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("small")) {
            setTailSpan(mSpannableBuilder, new TNoteSizeSmallSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("font")) {
            setTailAttrSpan(mSpannableBuilder, atts);
        }
        else if (tag.equalsIgnoreCase("blockquote")) {
            appendLineBreak(mSpannableBuilder);
            setTailSpan(mSpannableBuilder, new TNoteQuoteSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("tt")) {
            setTailSpan(mSpannableBuilder, new TNoteTypeFaceSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("a")) {

            String href = atts.getValue("", "href");
            int end = mSpannableBuilder.length();
            mSpannableBuilder.setSpan(new TNoteHRefSpannable(href), end, end, Spanned.SPAN_MARK_MARK);
        }
        else if (tag.equalsIgnoreCase("u")) {
            setTailSpan(mSpannableBuilder, new TNoteUnderlineSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("sup")) {
            setTailSpan(mSpannableBuilder, new TNoteSuperscriptSpannable((byte) 0));
        }
        else if (tag.equalsIgnoreCase("sub")) {
            setTailSpan(mSpannableBuilder, new TNoteSubscriptSpannable((byte)0));
        }
        else if (tag.equalsIgnoreCase("hr")) {

            TLineDrawSpan lineSpan = new TLineDrawSpan(0xff888888, 480);
            int end = mSpannableBuilder.length();

            mSpannableBuilder.append("\uFFFC");
            mSpannableBuilder.setSpan(lineSpan, end, mSpannableBuilder.length(), Spanned.SPAN_POINT_MARK);
        }
        else if (tag.equalsIgnoreCase("img")) {

            String href = atts.getValue("", "src");
            TBitmapSpan imgSpan = new TBitmapSpan();
            int end = mSpannableBuilder.length();

            imgSpan.mFilePath = TNoteDataUtil.getFileNameFromPath(href);
            mSpannableBuilder.append("\uFFFC");
            mSpannableBuilder.setSpan(imgSpan, end, mSpannableBuilder.length(), Spanned.SPAN_POINT_MARK);
        }
        else if (tag.equalsIgnoreCase("style")) {
            this.mMetaData = true;
        }
        else if (tag.equalsIgnoreCase("script")) {
            this.mMetaData = true;
        }
        else if (tag.equalsIgnoreCase("head")) {
            this.mMetaData = true;
        }
        else if (tag.equalsIgnoreCase("title")) {
            this.mMetaData = true;
        }
    }

    public final void startPrefixMapping(String s1, String s2) {
    }

    static {
        mColorMap = new HashMap();
        mColorMap.put("aqua", Integer.valueOf(0x0000ffff));
        mColorMap.put("black", Integer.valueOf(0x00000000));
        mColorMap.put("blue", Integer.valueOf(0x000000ff));
        mColorMap.put("fuchsia", Integer.valueOf(0xff00ff));
        mColorMap.put("green", Integer.valueOf(0x00008000));
        mColorMap.put("grey", Integer.valueOf(0x808080));
        mColorMap.put("lime", Integer.valueOf(0x0000ff00));
        mColorMap.put("maroon", Integer.valueOf(0x00800000));
        mColorMap.put("navy", Integer.valueOf(0x00000080));
        mColorMap.put("olive", Integer.valueOf(0x00808000));
        mColorMap.put("purple", Integer.valueOf(0x00800080));
        mColorMap.put("red", Integer.valueOf(0x00ff0000));
        mColorMap.put("silver", Integer.valueOf(0x00c0c0c0));
        mColorMap.put("teal", Integer.valueOf(0x00008080));
        mColorMap.put("white", Integer.valueOf(0x00ffffff));
        mColorMap.put("yellow", Integer.valueOf(0x00ffff00));

        mZoomList = new float[]{
                1.5F, 1.4F, 1.3F, 1.2F, 1.1F, 1.0F
        };
    }

    static class TNoteTextAttrSpannable {

        public TNoteTextAttrSpannable() {

        }


        public static final int StateForeColor = 1;
        public static final int StateBackColor = 2;
        public static final int StateTextFace = 4;
        public static final int StateTextSize = 8;

        public String mTextFace = null;
        public int mForeColor = 0;
        public int mBackColor = 0xffffffff;
        public int mSize = 0;
        public int mState = 0;
    }

    static class TNoteDivSpannable {

        public String mAlign;

        TNoteDivSpannable(String align) {

            mAlign = "left";

            if (align != null)
                mAlign = align;
        }
    }

    static class TNoteLiSpannable {

        private TNoteLiSpannable() {
        }

        TNoteLiSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteBoldSpannable {

        private TNoteBoldSpannable() {
        }

        TNoteBoldSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteItalicSpannable {

        private TNoteItalicSpannable() {
        }

        TNoteItalicSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteSizeBigSpannable {

        private TNoteSizeBigSpannable() {
        }

        TNoteSizeBigSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteSizeSmallSpannable {

        private TNoteSizeSmallSpannable() {
        }

        TNoteSizeSmallSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteQuoteSpannable {

        private TNoteQuoteSpannable() {
        }

        TNoteQuoteSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteTypeFaceSpannable {

        private TNoteTypeFaceSpannable() {
        }

        TNoteTypeFaceSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteHRefSpannable {

        public String mUrl;

        public TNoteHRefSpannable(String url) {
            mUrl = url;
        }
    }

    static class TNoteUnderlineSpannable {

        private TNoteUnderlineSpannable() {
        }

        TNoteUnderlineSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteSubscriptSpannable {

        private TNoteSubscriptSpannable() {
        }

        TNoteSubscriptSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteSuperscriptSpannable {

        private TNoteSuperscriptSpannable() {
        }

        TNoteSuperscriptSpannable(byte byte0) {
            this();
        }
    }

    static class TNoteSizeRelSpannable {

        static int getSize(TNoteSizeRelSpannable spannable) {
            return spannable.mSize;
        }

        private int mSize;
    }
}
