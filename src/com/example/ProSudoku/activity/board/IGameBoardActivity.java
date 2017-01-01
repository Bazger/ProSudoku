package com.example.ProSudoku.activity.board;

import com.example.ProSudoku.plugin.IPluginHandlerActivity;

public interface IGameBoardActivity extends IPluginHandlerActivity {
    byte[][] getMemoryMatrix();
    boolean[][] getChangeMatrix();
    int getMatrixRectCount();
}
