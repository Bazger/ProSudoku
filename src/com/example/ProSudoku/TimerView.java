package com.example.ProSudoku;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by Vanya on 02.04.2015
 */
public class TimerView extends View {

    private String currentTime = "";
    private Point resolution;

    private AssetManager asset;
    private Typeface myTypeface;

    private Paint p;

    private double yBorderRatio = 10;
    private int yPos;
    private int textSize;

    private int timeInMilliseconds = 5999000;

    private int textColor = Color.WHITE;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resolution = new Point(parentWidth, parentHeight);
        onLoad();
    }

    public TimerView(Context context) {
        super(context);
        initViews(context);
    }

    public TimerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(Context context) {
        asset = context.getAssets();
        myTypeface = Typeface.createFromAsset(asset, "Mandarin.ttf");
    }


    private void onLoad() {
        p = new Paint();

        yPos = (int)((resolution.y * yBorderRatio) / 100);
        textSize = resolution.y - yPos;

        new CountDownTimer(timeInMilliseconds, 1000) {

            public void onTick(long millisUntilFinished) {
                long seconds = (timeInMilliseconds - millisUntilFinished) / 1000;
                currentTime = String.format("%02d:%02d", seconds / 60, seconds % 60);
                invalidate();
            }

            public void onFinish() {
                currentTime = ("99+");
                invalidate();
            }
        }.start();
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor, Typeface font) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(font);
        int width = (int) (paint.measureText(text) + 0.5f); // round
        float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        p.setColor(Color.WHITE);
        Bitmap bit = textAsBitmap(currentTime, textSize, textColor, myTypeface);
        canvas.drawBitmap(bit, resolution.x / 2 - bit.getWidth() / 2, yPos + resolution.y / 2 - bit.getHeight() / 2, p);
        //Square.drawText(currentTime, 200,200, squareColor);
    }

}
