package com.example.ProSudoku;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

/**
 * Created by Vanya on 21.02.2015
 */
public class Timer extends Activity{

    TimerView timerView;

    private Handler mHandler;
    private boolean mStarted;
    private long milliseconds;

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                long seconds = (System.currentTimeMillis() - milliseconds) / 1000;
                setTitle(String.format("%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timerView = new TimerView(this);
        setContentView(R.layout.abc);
        mHandler = new Handler();
    }


    @Override
    protected void onStart() {
        super.onStart();
        milliseconds = System.currentTimeMillis();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
    }
}
