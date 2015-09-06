package com.idragonit.talkpad.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Michale on 2/24/2015.
 */
public class ScrollLayout extends ViewGroup {

    protected Handler mHandler;
    private Scroller mScroller;
    private VelocityTracker mVelocity;
    private int mCurPage;
    private int fScrolling;
    private int mTouchSlop;
    private float mBefPosX;
    private float mBefPosY;

    public ScrollLayout(Context context, AttributeSet attrs) {

        this(context, attrs, 0);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        mHandler = null;
        mScroller = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        fScrolling = 0;
        mCurPage = 0;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int sum = 0;

        for (int i = 0; i < getChildCount(); i ++) {

            View child = getChildAt(i);

            if (child.getVisibility() != View.GONE) {

                int measuredWidth = child.getMeasuredWidth();

                child.layout(sum, 0, sum + measuredWidth, child.getMeasuredHeight());
                sum += measuredWidth;
            }
        }
    }

    private void setCurPage(int page) {

        int nNewPage = Math.max(0, Math.min(page, getChildCount() - 1));

        if (getScrollX() != nNewPage * getWidth()) {

            int pos = nNewPage * getWidth() - getScrollX();

            mScroller.startScroll(getScrollX(), 0, pos, 0, 2 * Math.abs(pos));
            mCurPage = nNewPage;

            invalidate();

            if (mHandler != null) {
                mHandler.sendMessage(mHandler.obtainMessage(5000, mCurPage, 0));
            }
        }
    }

    public final int getCurPage() { //a
        return mCurPage;
    }

    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {

        int action = motionEvent.getAction();

        if (action != MotionEvent.ACTION_MOVE || fScrolling == 0) {

            final float x = motionEvent.getX();
            final float y = motionEvent.getY();

            switch (action) {
                case MotionEvent.ACTION_MOVE: {
                    if ((int)Math.abs(mBefPosX - x) > mTouchSlop) {
                        fScrolling = 1;
                    }
                    break;
                }

                case MotionEvent.ACTION_DOWN: {
                    mBefPosX = x;
                    mBefPosY = y;
                    fScrolling = mScroller.isFinished() ? 0 : 1;
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    fScrolling = 0;
                    break;
                }
            }

            if (fScrolling == 0) {
                return false;
            }
        }

        return true;
    }

    protected void onMeasure(int widthMeasureSpec, final int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int size = MeasureSpec.getSize(widthMeasureSpec);

        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
        }
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("ScrollLayout only can run at EXACTLY mode!");
        }

        for (int i = 0; i < getChildCount(); ++ i) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        scrollTo(size * mCurPage, 0);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {

        if (mVelocity == null) {
            mVelocity = VelocityTracker.obtain();
        }

        mVelocity.addMovement(motionEvent);

        int action = motionEvent.getAction();
        float x = motionEvent.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                mBefPosX = x;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int delta = (int) (mBefPosX - x);
                mBefPosX = x;
                scrollBy(delta, 0);
                break;
            }

            case MotionEvent.ACTION_UP: {
                mVelocity.computeCurrentVelocity(1000);
                int velocity = (int)mVelocity.getXVelocity();

                if (velocity > 200 && mCurPage > 0) {
                    setCurPage(-1 + mCurPage);
                }
                else if (velocity < -200 && mCurPage < -1 + getChildCount()) {
                    setCurPage(1 + mCurPage);
                }
                else {
                    int width = getWidth();
                    setCurPage((getScrollX() + width / 2) / width);
                }

                if (mVelocity != null) {
                    mVelocity.recycle();
                    mVelocity = null;
                }

                fScrolling = 0;
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                fScrolling = 0;
                break;
            }
        }
        return true;
    }

    public void setCallbackHandler(Handler handler) {
        mHandler = handler;
    }

    public void setToScreen(int page) {
        int nPage = Math.max(0, Math.min(page, getChildCount() - 1));

        mCurPage = nPage;
        scrollTo(nPage * getWidth(), 0);
    }
}
