package com.example.ProSudoku.generator;

public class SudokuSolver implements ISudokuSolver {

    /**
     * Solves the given Sudoku.
     */
    @Override
    public boolean solve(byte[][] gameBoard) {
        // Find untouched location with most information
        int xp = 0;
        int yp = 0;
        byte[] Mp = null;
        int cMp = 10;

        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                if (gameBoard[x][y] == 0) {
                    // Set M of possible solutions
                    byte[] M = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

                    // Remove used numbers in the vertical direction
                    for (int a = 0; a < 9; a++)
                        M[gameBoard[a][y]] = 0;

                    // Remove used numbers in the horizontal direction
                    for (int b = 0; b < 9; b++)
                        M[gameBoard[x][b]] = 0;

                    // Remove used numbers in the sub square. BUG:
                    //int squareIndex = m_subSquare[y, x];
                    //for (int c = 0; c < 9; c++)
                    //{
                    //    EntryPoint p = m_subIndex[squareIndex, c];
                    //    M[m_sudoku[p.x, p.y]] = 0;
                    //}

                    int n = (int) Math.sqrt(gameBoard.length);
                    int sectorX = x / n;
                    int sectorY = y / n;
                    for (int a = 0; a < n; a++) {
                        for (int b = 0; b < n; b++) {
                            M[gameBoard[a + sectorX * n][b + sectorY * n]] = 0;
                        }
                    }


                    int cM = 0;
                    // Calculate cardinality of M
                    for (int d = 1; d < 10; d++)
                        cM += M[d] == 0 ? 0 : 1;

                    // Is there more information in this spot than in the best yet?
                    if (cM < cMp) {
                        cMp = cM;
                        Mp = M;
                        xp = x;
                        yp = y;
                    }
                }
            }

        }

        // Finished?
        if (cMp == 10)
            return true;

        // Couldn't find a solution?
        if (cMp == 0)
            return false;

        // Try elements
        for (int i = 1; i < 10; i++) {
            if (Mp[i] != 0) {
                gameBoard[xp][yp] = Mp[i];
                if (solve(gameBoard))
                    return true;
            }
        }

        // Restore to original state.
        gameBoard[xp][yp] = 0;
        return false;
    }

}
