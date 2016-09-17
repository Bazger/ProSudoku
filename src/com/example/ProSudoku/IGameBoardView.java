package com.example.ProSudoku;

import com.example.ProSudoku.plugin.IGameBoardViewPlugin;
import java.util.List;

public interface IGameBoardView {

    byte[][] getMemoryMatrix();
    boolean[][] getChangeMatrix();
    int getMatrixRectCount();
    List<IGameBoardViewPlugin> getPlugins();
}
