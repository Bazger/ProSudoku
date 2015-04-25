package com.example.ProSudoku;

import android.content.Context;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Vanya on 15.04.2015
 */
class AboutScrollView extends ScrollView {

    public Handler mHandler;
    public boolean mStarted;

    int startPos;
    private int moveSpeed = 1;
    private int threadSpeed = 5;

    public AboutScrollView(Context context) {
        super(context);
        init(context);
    }

    public AboutScrollView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context) {
        mHandler = new Handler();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
    }

    public final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                scrollBy(0, moveSpeed);
                mHandler.postDelayed(mRunnable, threadSpeed);
            }
        }

    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        View view = getChildAt(getChildCount() - 1);
        startPos = (view.getBottom() - (getHeight() + getScrollY() + view.getTop()));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        if(mStarted) {
            mStarted = false;
            mHandler.removeCallbacks(mRunnable);
            new CountDownTimer(20000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    mStarted = true;
                    mHandler = new Handler();
                    mHandler.postDelayed(mRunnable, threadSpeed);
                }

            }.start();
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        View view = getChildAt(getChildCount() - 1);
        int diff = (view.getBottom() - (getHeight() + getScrollY() + view.getTop()));// Calculate the scrolldiff
        if (diff == startPos) {  // if diff is zero, then the bottom has been reached
            moveSpeed = Math.abs(moveSpeed);
        }
        if(diff == 0)
        {
            moveSpeed = -Math.abs(moveSpeed);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
