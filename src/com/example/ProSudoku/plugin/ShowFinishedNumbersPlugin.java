package com.example.ProSudoku.plugin;

import android.content.Context;
import android.graphics.*;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.activity.board.GameBoardView;

public class ShowFinishedNumbersPlugin extends GameBoardViewPlugin {

    private Context mContext;
    private GameBoardView gameBoardView;

    private Rect[] numberBoardRects;
    private byte[][] memoryMatrix;
    private Typeface typeface;
    private GameBoardColors colors;
    private SudokuNumber[] uNumbers;

    private Paint p;
    private CheckBoxPreference preference;

    public ShowFinishedNumbersPlugin(Context context)
    {
        mContext = context;
        preference = new CheckBoxPreference(context);
        preference.setTitle("Finished Numbers");
        preference.setSummary("Show finished numbers");
        preference.setKey("finishedNumbersPlugin");
        preference.setDefaultValue(false);
    }


    private class SudokuNumber {
        int count;
        boolean uniqueness;
        SudokuNumber()
        {
            this.count = 0;
            this.uniqueness = true;
        }
    }

    @Override
    public void init(GameBoardView gameBoardView) {
        this.gameBoardView = gameBoardView;

        this.numberBoardRects = gameBoardView.getNumberBoardRects();
        this.memoryMatrix = gameBoardView.getActivity().getMemoryMatrix();
        this.typeface = gameBoardView.getGameBoardTypeface();
        this.colors = gameBoardView.getGameBoardColors();

        this.p = new Paint();

        this.uNumbers = new SudokuNumber[numberBoardRects.length];
        for (int i = 0; i < uNumbers.length; i++)
        {
            uNumbers[i] = new SudokuNumber();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (SudokuNumber uNumber : uNumbers)  {
            uNumber.count = 0;
            uNumber.uniqueness = true;
        }

        for (int i = 0; i < memoryMatrix.length; i++) {
            for (int j = 0; j < memoryMatrix[i].length; j++) {
                if (memoryMatrix[i][j] != 0) {
                    if (uNumbers[memoryMatrix[i][j]].uniqueness && !gameBoardView.isUniqueNumber(i, j)) {
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
            if(uNumbers[i].uniqueness) {
                Bitmap bit = gameBoardView.textAsBitmap(String.valueOf(i), numberBoardRects[i].width(), colors.getErrorColor(), typeface);
                canvas.drawBitmap(bit, numberBoardRects[i].centerX() - bit.getWidth() / 2, numberBoardRects[i].centerY() - bit.getHeight() / 2, p);
            }
        }
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
    public String getPluginName() {
        return this.toString();
    }
}
