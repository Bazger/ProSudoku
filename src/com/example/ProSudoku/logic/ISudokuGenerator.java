package com.example.ProSudoku.logic;

import android.support.annotation.Nullable;
import com.example.ProSudoku.Difficulty;

public interface ISudokuGenerator {
    @Nullable
    String generate(Difficulty difficulty);
}
