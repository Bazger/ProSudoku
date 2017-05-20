package com.example.ProSudoku.plugin.plugins;

import android.content.Context;
import android.graphics.*;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import com.example.ProSudoku.R;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;
import com.example.ProSudoku.plugin.IPreferencePlugin;

public class ShowFinishedNumbersPlugin extends GameBoardViewPlugin implements IPreferencePlugin {

    private Rect[] numberBoardRects;
    private Bitmap[] numberBoardNumbers;
    private byte[][] memoryMatrix;
    private Typeface typeface;
    private GameBoardColors colors;
    private SudokuNumber[] uNumbers;

    private Paint p;
    private CheckBoxPreference preference;

    public ShowFinishedNumbersPlugin(Context context) {
        super(context);
        preference = new CheckBoxPreference(context);
        preference.setTitle(R.string.plugin_show_finished_number_title);
        preference.setSummary(R.string.plugin_show_finished_number_summary);
        preference.setKey(getPluginName());
        preference.setDefaultValue(false);
    }

    private class SudokuNumber {
        int count;
        boolean uniqueness;

        SudokuNumber() {
            this.count = 0;
            this.uniqueness = true;
        }
    }

    @Override
    public void init(GameBoardView gameBoardView) {
        super.init(gameBoardView);

        this.numberBoardRects = gameBoardView.getNumberBoardRects();
        this.memoryMatrix = gameBoardView.getActivity().getMemoryMatrix();
        this.typeface = gameBoardView.getGameBoardTypeface();
        this.colors = gameBoardView.getGameBoardColors();
        this.numberBoardNumbers = gameBoardView.getNumberBoardNumbers();

        this.p = new Paint();

        this.uNumbers = new SudokuNumber[numberBoardRects.length];
        for (int i = 0; i < uNumbers.length; i++) {
            uNumbers[i] = new SudokuNumber();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (SudokuNumber uNumber : uNumbers) {
            uNumber.count = 0;
            uNumber.uniqueness = true;
        }

        for (int i = 0; i < memoryMatrix.length; i++) {
            for (int j = 0; j < memoryMatrix[i].length; j++) {
                if (memoryMatrix[i][j] != 0) {
                    if (uNumbers[memoryMatrix[i][j]].uniqueness && !getGameBoardView().isUniqueNumber(i, j)) {
                        uNumbers[memoryMatrix[i][j]].uniqueness = false;
                    }
                    uNumbers[memoryMatrix[i][j]].count += 1;
                }
            }
        }

        for (SudokuNumber uNumber : uNumbers) {
            if (uNumber.count != 9) {
                uNumber.uniqueness = false;
            }
        }

        for (int i = 1; i < numberBoardRects.length; i++) {
            if (uNumbers[i].uniqueness) {
                numberBoardNumbers[i] = getGameBoardView().textAsBitmap(String.valueOf(i), numberBoardRects[i].width(), colors.getSameCellsToSelectedCellColor(), typeface);
            }
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }


    @Override
    public boolean isActive() {
        return preference.isChecked();
    }

    @Override
    public Preference getPreference() {
        return preference;
    }

    @Override
    protected String getCleanPluginName() {
        return "ShowFinishedNumbersPlugin";
    }
}
