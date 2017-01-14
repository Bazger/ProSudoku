package com.example.ProSudoku.plugin.plugins;


import android.content.Context;
import android.graphics.*;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.activity.board.IGameBoardActivity;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;

public class HighlightNumbersForSolverPlugin extends GameBoardViewPlugin {

    private IGameBoardActivity activity;
    private Rect[][] gameBRects;
    private Bitmap[][] gameBNumbers;
    private Typeface gameBTypeface;
    private GameBoardColors gameBColors;

    private Paint p;

    public HighlightNumbersForSolverPlugin(Context context) {
        super(context);
    }

    public void init(GameBoardView gameBoardView) {
        super.init(gameBoardView);
        this.gameBRects = gameBoardView.getGameBoardRects();
        this.gameBTypeface = gameBoardView.getGameBoardTypeface();
        this.gameBColors = gameBoardView.getGameBoardColors();
        this.gameBNumbers = gameBoardView.getGameBoardNumbers();
        this.activity = gameBoardView.getActivity();
        p = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < gameBRects.length; i++) {
            for (int j = 0; j < gameBRects[i].length; j++) {
                if (activity.getMemoryMatrix()[i][j] > 0) {
                    if (activity.getChangeMatrix()[i][j]) {
                        gameBNumbers[i][j] = getGameBoardView().textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), getGameBoardView().getGameBoardCellWidth(), gameBColors.getChangeableTextColor(), gameBTypeface);
                    } else {
                        gameBNumbers[i][j] = getGameBoardView().textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), getGameBoardView().getGameBoardCellWidth(), gameBColors.getSolveColor(), gameBTypeface);
                    }
                }
            }
        }
    }

    @Override
    protected String getCleanPluginName() {
        return "HighlightNumbersForSolverPlugin";
    }
}
