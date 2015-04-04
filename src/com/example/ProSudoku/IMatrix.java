package com.example.ProSudoku;

/**
 * Created by Vanya on 09.03.2015.
 */
public interface IMatrix {

    byte[][] getMemoryMatrix();
    boolean[][] getChangeMatrix();
    int getMatrixRectCount();
}
