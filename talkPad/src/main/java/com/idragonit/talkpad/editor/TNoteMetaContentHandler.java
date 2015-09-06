package com.idragonit.talkpad.editor;

import android.text.SpannableStringBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.*;

public final class TNoteMetaContentHandler implements ContentHandler {

    HashMap mMetaDataMap = new HashMap();
    private String mXmlContent;
    private XMLReader mXmlReader;
    private SpannableStringBuilder mSpannableBuilder;
    private boolean mSkip = false;
    private boolean mTitle = false;

    public TNoteMetaContentHandler() {
    }

    public static HashMap getMetaContentMap(String content) {
        TNoteMetaContentHandler contentHandler = new TNoteMetaContentHandler();

        contentHandler.mXmlReader = new Parser();
        contentHandler.mXmlContent = content;
        contentHandler.mSpannableBuilder = new SpannableStringBuilder();
        contentHandler.mXmlReader.setContentHandler(contentHandler);

        try {
            contentHandler.mXmlReader.parse(new InputSource(new StringReader(contentHandler.mXmlContent)));
        }
        catch (IOException e) {
        }
        catch (SAXException e) {
        }
        catch (Exception e) {
        }

        contentHandler.mMetaDataMap.put("body", contentHandler.mSpannableBuilder.toString());

        return contentHandler.mMetaDataMap;
    }

    public final void characters(char[] chars, int start, int length) {

        if (!this.mSkip) {

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < length; ++i) {

                char ch = chars[i + start];
                if (ch != 32 && ch != 10) {

                    if (ch == 160) {
                        sb.append(' ');
                    } else {
                        sb.append(ch);
                    }
                }
                else {

                    if (sb.length() == 0) {

                        int var10 = mSpannableBuilder.length();
                        if (var10 == 0) {
                            ch = 10;
                        } else {
                            ch = mSpannableBuilder.charAt(var10 - 1);
                        }
                    } else {
                        ch = sb.charAt(sb.length() - 1);
                    }

                    if (ch != 32 && ch != 10) {
                        sb.append(' ');
                    }
                }
            }

            if (this.mTitle) {
                mMetaDataMap.put("title", sb.toString());
            } else {
                mSpannableBuilder.append(sb);
            }
        }
    }

    public final void endDocument() {
    }

    public final void endElement(String uri, String tag, String qName) {

        if (tag.compareToIgnoreCase("title") == 0) {
            mTitle = false;
        }
        else if (tag.equalsIgnoreCase("style")) {
            mSkip = false;
        }
        else if (tag.equalsIgnoreCase("script")) {
            mSkip = false;
        }
    }

    public final void endPrefixMapping(String s) {
    }

    public final void ignorableWhitespace(char ac[], int i, int j) {
    }

    public final void processingInstruction(String s, String s1) {
    }

    public final void setDocumentLocator(Locator locator) {
    }

    public final void skippedEntity(String s) {
    }

    public final void startDocument() {
    }

    public final void startElement(String uri, String tag, String qName, Attributes attrs) {

        if (tag.compareToIgnoreCase("meta") == 0) {

            for (int i = 0; i < attrs.getLength(); ++i) {
                mMetaDataMap.put(attrs.getLocalName(i), attrs.getValue(i));
            }
        }

        if (tag.compareToIgnoreCase("title") == 0) {
            this.mTitle = true;
            mMetaDataMap.put("title", "");
        }
        else if (tag.equalsIgnoreCase("style")) {
            this.mSkip = true;
        }
        else if (tag.equalsIgnoreCase("script")) {
            this.mSkip = true;
        }
    }

    public final void startPrefixMapping(String s, String s1) {
    }
}
