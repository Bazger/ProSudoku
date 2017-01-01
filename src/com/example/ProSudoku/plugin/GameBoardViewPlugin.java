package com.example.ProSudoku.plugin;

import android.graphics.Canvas;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import com.example.ProSudoku.activity.board.GameBoardView;

public abstract class GameBoardViewPlugin {
    public GameBoardViewPlugin() {
    }

    public abstract void init(GameBoardView gameBoardView);

    public abstract void onDraw(Canvas canvas);

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    public boolean isActive()
    {
        return true;
    }

    public Preference getPreference() {
        return null;
    }

    public abstract String getPluginName();
}
