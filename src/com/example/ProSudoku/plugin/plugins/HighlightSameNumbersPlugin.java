package com.example.ProSudoku.plugin.plugins;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.activity.board.IGameBoardActivity;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;
import com.example.ProSudoku.plugin.IPreferencePlugin;

import static android.content.Context.MODE_PRIVATE;

public class HighlightSameNumbersPlugin extends GameBoardViewPlugin implements IPreferencePlugin {

    private IGameBoardActivity activity;
    private Rect[][] gameBRects;
    private byte[][] memoryMatrix;
    private Typeface gameBTypeface;
    private GameBoardColors gameBColors;
    private Point selectedPoint;
    private int gameBChoseBorder;

    private Paint p;
    private CheckBoxPreference preference;

    public HighlightSameNumbersPlugin(Context context) {
        super(context);
        preference = new CheckBoxPreference(context);
        preference.setTitle("Highlight Same Numbers");
        preference.setSummary("Highlighting same numbers on the game board");
        preference.setKey(getPluginName());
        preference.setDefaultValue(true);
    }

    @Override
    public void init(GameBoardView gameBoardView) {
        super.init(gameBoardView);
        this.activity = gameBoardView.getActivity();
        this.gameBRects = gameBoardView.getGameBoardRects();
        this.memoryMatrix = gameBoardView.getActivity().getMemoryMatrix();
        this.gameBTypeface = gameBoardView.getGameBoardTypeface();
        this.gameBColors = gameBoardView.getGameBoardColors();
        this.selectedPoint = gameBoardView.getSelectedPoint();
        this.gameBChoseBorder = gameBoardView.getGameBChoseBorder();

        this.p = new Paint();
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (int i = 0; i < gameBRects.length; i++) {
            for (int j = 0; j < gameBRects[i].length; j++) {
                if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
                    if (memoryMatrix[selectedPoint.x][selectedPoint.y] == 0 || (i == selectedPoint.x && j == selectedPoint.y)) {
                        continue;
                    }
                    if (memoryMatrix[selectedPoint.x][selectedPoint.y] != memoryMatrix[i][j]) {
                        continue;
                    }
                    p.setColor(gameBColors.getChoseEmptyCellColor());
                    canvas.drawRect(gameBRects[i][j], p);
                    p.setColor(gameBColors.getCellColor());
                    canvas.drawRect(new Rect(gameBRects[i][j].left + gameBChoseBorder,
                            gameBRects[i][j].top + gameBChoseBorder,
                            gameBRects[i][j].right - gameBChoseBorder,
                            gameBRects[i][j].bottom - gameBChoseBorder), p);
                }
            }
        }

    }

    @Override
    public boolean isActive() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(getPluginName(), Boolean.FALSE);
    }

    public void load() {
    }

    public void save() {
    }

    @Override
    protected String getCleanPluginName() {
        return "HighlightSameNumbersPlugin";
    }

    public Preference getPreference() {
        return preference;
    }
}
