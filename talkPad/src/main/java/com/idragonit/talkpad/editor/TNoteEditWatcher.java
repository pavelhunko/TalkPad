package com.idragonit.talkpad.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ReplacementSpan;

import com.idragonit.talkpad.editor.style.TBitmapSpan;

import java.util.Arrays;
import java.util.Comparator;

final class TNoteEditWatcher implements TextWatcher {

    int mEditPos;
    TNoteEditText mEdit;

    TNoteEditWatcher(TNoteEditText edit) {

        super();

        mEdit = edit;
        mEditPos = 0;
    }

    static TNoteEditText getEditText(TNoteEditWatcher watcher) {
        return watcher.mEdit;
    }

    @Override
    public final void afterTextChanged(Editable s) {

        AlignmentSpan[] spans = mEdit.getText().getSpans(0, mEditPos, AlignmentSpan.class);

        if (spans.length > 0) {

            Arrays.sort(spans, new TNoteSpanComparator(this));

            for (int i = spans.length - 1; i >= 0; -- i) {

                int start = TNoteEditText.getSpanStartPos(mEdit, spans[i]);
                int end   = TNoteEditText.getSpanEndPos(mEdit, spans[i]);

                mEdit.getText().removeSpan(spans[i]);
                mEdit.getText().setSpan(spans[i], start, end, 18);
            }
        }
    }

    @Override
    public final void beforeTextChanged(CharSequence s, int start, int count, int after) {

        mEditPos = start;

        if (count > 0) {

            ReplacementSpan[] replaceSpans = mEdit.getText().getSpans(start, start + count, ReplacementSpan.class);

            for (int i = 0; i < replaceSpans.length; ++ i) {

                mEdit.getText().removeSpan(replaceSpans[i]);

                if(replaceSpans[i] instanceof TBitmapSpan) {
                    ((TBitmapSpan) replaceSpans[i]).onDelete(mEdit.getText(), false);
                }
            }

            AlignmentSpan[] alignSpans = mEdit.getText().getSpans(start, start + count, AlignmentSpan.class);

            for (int i = 0; i < alignSpans.length; ++ i) {

                int spanStart = mEdit.getText().getSpanStart(alignSpans[i]);

                if (spanStart > start && spanStart <= start + count) {
                    mEdit.getText().removeSpan(alignSpans[i]);
                }
            }
        } else {
            TNoteEditText.refreshEditText(mEdit, start);
        }
    }

    @Override
    public final void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    class TNoteSpanComparator implements Comparator {

        TNoteEditWatcher mWatcher;

        TNoteSpanComparator(TNoteEditWatcher watcher) {
            super();

            mWatcher = watcher;
        }

        public final int compare(Object obj1, Object obj2) {
            return TNoteEditText.getSpanStartPos(TNoteEditWatcher.getEditText(mWatcher), obj1) - TNoteEditText.getSpanStartPos(TNoteEditWatcher.getEditText(mWatcher), obj2);
        }
    }
}
