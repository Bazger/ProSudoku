package com.example.ProSudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by Vanya on 02.04.2015.
 */
public class TimerView extends View {

    String currentTime;


    public TimerView(Context context){
        super(context);
        setFocusable(true);

        Thread myThread = null;
        Runnable myRunnableThread = new CountDownRunner();
        myThread= new Thread(myRunnableThread);
        myThread.start();
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                currentTime = ("seconds remaining: " + millisUntilFinished / 1000);
                invalidate();
            }

            public void onFinish() {
                currentTime = ("done!");
                invalidate();
            }
        }.start();

    }

    public void doWork() {
        new Runnable() {
            public void run() {
                try{
                    Date dt = new Date();
                    int hours = dt.getHours();
                    int minutes = dt.getMinutes();
                    int seconds = dt.getSeconds();
                    currentTime = hours + ":" + minutes + ":" + seconds;
                }catch (Exception e) {}
            }
        };
    }


    class CountDownRunner implements Runnable{
        // @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                    doWork();
                    Thread.sleep(1000);
                    // Pause of 1 Second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        invalidate();
        return true;
    }

    protected void onDraw(Canvas Square)
    {
        super.onDraw(Square);
        Paint squareColor = new Paint();
        squareColor.setColor(Color.WHITE);
        squareColor.setTextSize(60);
        Square.drawText(currentTime, 200,200, squareColor);
        return;
    }

}
