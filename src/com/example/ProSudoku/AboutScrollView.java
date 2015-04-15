package com.example.ProSudoku;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Vanya on 15.04.2015
 */
class AboutScrollView extends ScrollView
{
    public Handler mHandler;
    public boolean mStarted;

    private int moveSpeed = 20;

    public AboutScrollView(Context context)
    {
        super(context);
        init(context);
    }

    public AboutScrollView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        init(context);
    }

    private void init(Context context)
    {
        mHandler = new Handler();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
    }

    public final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                scrollBy(0, moveSpeed);
                mHandler.postDelayed(mRunnable, 100);
            }
        }

    };

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt)
    {
        View view = getChildAt(getChildCount() - 1);
        int d = view.getBottom();
        d -= (getHeight()+getScrollY());
        if(d==0 ||d == view.getBottom())
        {
            moveSpeed = moveSpeed * -1;
        }
        else
            super.onScrollChanged(l,t,oldl,oldt);
    }


}
