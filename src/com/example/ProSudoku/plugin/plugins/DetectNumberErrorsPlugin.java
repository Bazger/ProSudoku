package com.example.ProSudoku.plugin.plugins;


import android.content.Context;
import android.graphics.*;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.activity.board.IGameBoardActivity;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;

public class DetectNumberErrorsPlugin extends GameBoardViewPlugin {

    private IGameBoardActivity activity;
    private Rect[][] gameBRects;
    private Bitmap[][] gameBNumbers;
    private Typeface gameBTypeface;
    private GameBoardColors gameBColors;

    private Paint p;

    public DetectNumberErrorsPlugin(Context context) {
        super(context);
    }

    public void init(GameBoardView gameBoardView) {
        super.init(gameBoardView);
        this.activity = gameBoardView.getActivity();
        this.gameBRects = gameBoardView.getGameBoardRects();
        this.gameBTypeface = gameBoardView.getGameBoardTypeface();
        this.gameBColors = gameBoardView.getGameBoardColors();
        this.gameBNumbers = gameBoardView.getGameBoardNumbers();
        p = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < gameBRects.length; i++) {
            for (int j = 0; j < gameBRects[i].length; j++) {
                if (activity.getMemoryMatrix()[i][j] > 0) {
                    if (activity.getChangeMatrix()[i][j] && !getGameBoardView().isUniqueNumber(i, j)) {
                        gameBNumbers[i][j] = getGameBoardView().textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), getGameBoardView().getGameBoardCellWidth(), gameBColors.getErrorColor(), gameBTypeface);
                    } else {
                        if (activity.getChangeMatrix()[i][j]) {
                            gameBNumbers[i][j] = getGameBoardView().textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), getGameBoardView().getGameBoardCellWidth(), gameBColors.getChangeableTextColor(), gameBTypeface);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected String getCleanPluginName() {
        return "DetectNumberErrorsPlugin";
    }
}
