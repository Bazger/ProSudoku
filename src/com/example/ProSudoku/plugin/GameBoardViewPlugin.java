package com.example.ProSudoku.plugin;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import com.example.ProSudoku.activity.board.GameBoardView;

public abstract class GameBoardViewPlugin {

    private Context context;
    private GameBoardView gameBoardView;

    public GameBoardView getGameBoardView() {
        return gameBoardView;
    }
    public Context getContext() {
        return context;
    }

    public GameBoardViewPlugin(Context context) {
        this.context = context;
    }

    public void init(GameBoardView gameBoardView){
        this.gameBoardView = gameBoardView;
    }

    public abstract void onDraw(Canvas canvas);

    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return false;
    }

    public boolean isActive()
    {
        return true;
    }

    public String getPluginName(){
        return getCleanPluginName() + context.getClass().getSimpleName();
    }

    protected abstract String getCleanPluginName();

}
