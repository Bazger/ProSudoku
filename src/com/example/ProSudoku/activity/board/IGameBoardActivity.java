package com.example.ProSudoku.activity.board;

public interface IGameBoardActivity{
    byte[][] getMemoryMatrix();
    boolean[][] getChangeMatrix();
    int getMatrixRectCount();
}
