package com.idragonit.talkpad.editor;

import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ParagraphStyle;
import android.text.style.ReplacementSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import com.idragonit.talkpad.editor.data.TNoteDataUtil;
import com.idragonit.talkpad.editor.style.TBitmapSpan;
import com.idragonit.talkpad.editor.style.TBulletStyleSpan;
import com.idragonit.talkpad.editor.style.TFontfaceSpan;
import com.idragonit.talkpad.editor.style.TLineDrawSpan;
import com.idragonit.talkpad.editor.style.TLinkClickSpan;
import com.idragonit.talkpad.editor.style.TSubscriptSpan;
import com.idragonit.talkpad.editor.style.TSuperscriptSpan;

import org.ccil.cowan.tagsoup.Parser;

public final class TNoteContentBuilder {

    public static Spanned getSpannableFromContent(String strContent) {

        Spanned spannable = (new TNoteContentHandler(strContent, new Parser())).getSpannableFromXml();
        CharacterStyle[] characterSpans = spannable.getSpans(0, spannable.length(), CharacterStyle.class);

        for (int i = 0; i < characterSpans.length; ++i) {

            if (!(characterSpans[i] instanceof ReplacementSpan) && !(characterSpans[i] instanceof TLinkClickSpan)) {
                ((SpannableStringBuilder)spannable).setSpan(characterSpans[i], spannable.getSpanStart(characterSpans[i]), spannable.getSpanEnd(characterSpans[i]), Spanned.SPAN_POINT_POINT);
            }
        }

        ParagraphStyle[] paragraphSpans = spannable.getSpans(0, spannable.length(), ParagraphStyle.class);
        for (int i = 0; i < paragraphSpans.length; ++i) {
            ((SpannableStringBuilder)spannable).setSpan(paragraphSpans[i], spannable.getSpanStart(paragraphSpans[i]), spannable.getSpanEnd(paragraphSpans[i]), Spanned.SPAN_MARK_POINT);
        }

        return spannable;
    }

    public static String getContentFromSpannable(Spanned spanned) {

        StringBuilder sb = new StringBuilder();

        buildContentFromSpannable(sb, spanned);

        return sb.toString();
    }

    private static void buildContentFromSpannable(StringBuilder sb, Spanned spanned) {

        int length = spanned.length();

        for (int i = 0; i < spanned.length(); i=i) {

            int nextSpanTrans = spanned.nextSpanTransition(i, length, ParagraphStyle.class);
            ParagraphStyle[] paragraphSpans = spanned.getSpans(i, nextSpanTrans, ParagraphStyle.class);
            String alignStyle = " ";
            TBulletStyleSpan bulletSpan = null;
            boolean fBullet = false;
            boolean fAlign = false;

            for (int j = 0; j < paragraphSpans.length; j ++) {

                if (paragraphSpans[j] instanceof TBulletStyleSpan) {

                    bulletSpan = (TBulletStyleSpan) paragraphSpans[j];
                    if (bulletSpan.mRow == 1) {
                        sb.append(bulletSpan.getBuilletTagString(false));
                    }

                    sb.append("<li>");
                    fBullet = true;
                }
                else if (paragraphSpans[j] instanceof AlignmentSpan) {
                    Layout.Alignment align = ((AlignmentSpan)paragraphSpans[j]).getAlignment();
                    if (align == Layout.Alignment.ALIGN_CENTER) {
                        alignStyle = "align=\"center\" ";
                        fAlign = true;
                    } else if(align == Layout.Alignment.ALIGN_OPPOSITE) {
                        alignStyle = "align=\"right\" ";
                        fAlign = true;
                    } else {
                        fAlign = false;
                    }
                }
            }

            if (fAlign) {
                sb.append("<div " + alignStyle + ">");
            }

            buildContentFromSpannableRange(sb, spanned, i, nextSpanTrans);

            if (fAlign) {
                sb.append("</div>");
            }

            if (fBullet) {
                sb.append("</li>");
                if (bulletSpan.mLevelTail) {
                    sb.append(bulletSpan.getBuilletTagString(true));
                }
            }

            if ((fAlign || fBullet) && nextSpanTrans < spanned.length()) {
                i = nextSpanTrans + 1;
            } else {
                i = nextSpanTrans;
            }
        }
    }

    private static void buildContentFromSpannableRange(StringBuilder sb, Spanned spanned, int start, int end) {

        int nextSpanTrans;

        for (int i = start; i < end; i = nextSpanTrans) {

            nextSpanTrans = spanned.nextSpanTransition(i, end, CharacterStyle.class);
            CharacterStyle[] characterSpans = spanned.getSpans(i, nextSpanTrans, CharacterStyle.class);
            int var8 = i;

            for (int j = 0; j < characterSpans.length; ++j) {

                if (characterSpans[j] instanceof StyleSpan) {

                    int style = ((StyleSpan)characterSpans[j]).getStyle();
                    if ((style & 1) != 0) {
                        sb.append("<b>");
                    }

                    if ((style & 2) != 0) {
                        sb.append("<i>");
                    }
                }

                if (characterSpans[j] instanceof TSuperscriptSpan) {
                    sb.append("<sup>");
                }

                if (characterSpans[j] instanceof TSubscriptSpan) {
                    sb.append("<sub>");
                }

                if (characterSpans[j] instanceof UnderlineSpan) {
                    sb.append("<u>");
                }

                if (characterSpans[j] instanceof StrikethroughSpan) {
                    sb.append("<strike>");
                }

                if (characterSpans[j] instanceof TLinkClickSpan) {

                    sb.append("<a href=\"");
                    sb.append(((TLinkClickSpan) characterSpans[j]).getUrl());
                    sb.append("\">");
                }

                if (characterSpans[j] instanceof TBitmapSpan) {

                    sb.append("<img src=\"");
                    sb.append(TNoteDataUtil.getFileNameFromPath(((TBitmapSpan) characterSpans[j]).getFilePath()));
                    sb.append("\"style=\"max-width: 300px;\">");
                    var8 = nextSpanTrans;
                }

                if (characterSpans[j] instanceof ForegroundColorSpan) {

                    sb.append("<font color =\"#");

                    String strColor = Integer.toHexString(0x1000000 + ((ForegroundColorSpan)characterSpans[j]).getForegroundColor());
                    for (; strColor.length() < 6; strColor = "0" + strColor) {
                        ;
                    }

                    sb.append(strColor);
                    sb.append("\">");
                }

                if (characterSpans[j] instanceof BackgroundColorSpan) {

                    sb.append("<font style =\"background-color:#");

                    String strColor = Integer.toHexString(0x1000000 + ((BackgroundColorSpan)characterSpans[j]).getBackgroundColor());
                    for (; strColor.length() < 6; strColor = "0" + strColor) {
                        ;
                    }

                    sb.append(strColor);
                    sb.append("\">");
                }

                if (characterSpans[j] instanceof AbsoluteSizeSpan) {

                    sb.append("<font style =\"font-size:");
                    sb.append(((AbsoluteSizeSpan) characterSpans[j]).getSize());
                    sb.append("px");
                    sb.append("\">");
                }

                if (characterSpans[j] instanceof TypefaceSpan) {
                    if (((TypefaceSpan)characterSpans[j]).getFamily().equals("monospace")) {
                        sb.append("<tt>");
                    }
                    else if (characterSpans[j] instanceof TFontfaceSpan) {
                        sb.append("<font style =\"font-family:");
                        sb.append(((TFontfaceSpan) characterSpans[j]).getFontUri());
                        sb.append("\">");
                    }
                }

                if (characterSpans[j] instanceof TLineDrawSpan) {

                    int nColor = ((TLineDrawSpan)characterSpans[j]).getColor();
                    String strColor = String.format("%X", nColor);

                    if (strColor.length() == 8) {
                        strColor = strColor.substring(2);
                    }

                    sb.append("<hr size=\"1px\" color=\"#" + strColor + "\"/>");
                    var8 = nextSpanTrans;
                }

                if (characterSpans[j] instanceof TBulletStyleSpan.TBaseDrawSpan) {
                    var8 = nextSpanTrans;
                }
            }

            buildSpannableBody(sb, spanned, var8, nextSpanTrans);

            for (int j = characterSpans.length - 1; j >= 0; --j) {

                if (characterSpans[j] instanceof BackgroundColorSpan) {
                    sb.append("</font>");
                }

                if (characterSpans[j] instanceof ForegroundColorSpan) {
                    sb.append("</font>");
                }

                if (characterSpans[j] instanceof AbsoluteSizeSpan) {
                    sb.append("</font>");
                }

                if (characterSpans[j] instanceof TFontfaceSpan) {
                    sb.append("</font>");
                }

                if (characterSpans[j] instanceof TLinkClickSpan) {
                    sb.append("</a>");
                }

                if (characterSpans[j] instanceof StrikethroughSpan) {
                    sb.append("</strike>");
                }

                if (characterSpans[j] instanceof UnderlineSpan) {
                    sb.append("</u>");
                }

                if (characterSpans[j] instanceof TSubscriptSpan) {
                    sb.append("</sub>");
                }

                if (characterSpans[j] instanceof TSuperscriptSpan) {
                    sb.append("</sup>");
                }

                if (characterSpans[j] instanceof TypefaceSpan && ((TypefaceSpan)characterSpans[j]).getFamily().equals("monospace")) {
                    sb.append("</tt>");
                }

                if (characterSpans[j] instanceof StyleSpan) {

                    int style = ((StyleSpan)characterSpans[j]).getStyle();

                    if ((style & 1) != 0) {
                        sb.append("</b>");
                    }

                    if ((style & 2) != 0) {
                        sb.append("</i>");
                    }
                }
            }
        }
    }

    private static void buildSpannableBody(StringBuilder sb, Spanned spanned, int start, int end) {

        for (; start < end; ++ start) {

            char ch = spanned.charAt(start);

            if (ch == 60) {
                sb.append("&lt;");
            }
            else if (ch == 62) {
                sb.append("&gt;");
            }
            else if (ch == 38) {
                sb.append("&amp;");
            }
            else if (ch == 10) {
                sb.append("<br>");
            }
            else if (ch <= 126 && ch >= 32) {
                if (ch == 32) {
                    sb.append("&nbsp;");
                }
                else {
                    sb.append(ch);
                }
            }
            else {
                sb.append(ch);
            }
        }
    }
}
