package com.example.ProSudoku.logic;

public class SudokuRulesUtils {

    /**
     * Fast test if the data is feasible.
     * Does not check if there is more than one solution.
     */
    public static boolean isSudokuFeasible(byte[][] gameBoard) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                // Set M of possible solutions
                byte[] M = new byte[gameBoard.length + 1];

                // Count used numbers in the vertical direction
                for (int a = 0; a < 9; a++)
                    M[gameBoard[a][y]]++;
                // Sudoku feasible?
                if (!feasible(M))
                    return false;

                M = new byte[10];
                // Count used numbers in the horizontal direction
                for (int b = 0; b < 9; b++)
                    M[gameBoard[x][b]]++;
                if (!feasible(M))
                    return false;

                M = new byte[10];

                int n = (int) Math.sqrt(gameBoard.length);
                int sectorX = x / n;
                int sectorY = y / n;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        if (i + sectorX * n != x && j + sectorY * n != y)
                            M[gameBoard[i + sectorX * n][j + sectorY * n]]++;
                    }
                }
                if (!feasible(M))
                    return false;

            }
        }

        return true;
    }

    private static boolean feasible(byte[] M) {
        for (int d = 1; d < M.length; d++)
            if (M[d] > 1)
                return false;

        return true;
    }

    public static int getNumberSpots(byte[][] gameBoard) {
        int num = 0;
        for (byte[] row : gameBoard)
            for (byte cell : row) num += cell == 0 ? 0 : 1;
        return num;
    }
}
