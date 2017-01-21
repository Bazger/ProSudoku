package com.example.ProSudoku.logic;

public class SudokuLogicUtils {

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

    /**
     * Convert an array into a matrix string
     */
    public static String toMatrixString(byte[][] matrix) {
        StringBuilder str = new StringBuilder();
        for (byte[] element : matrix)
            for (byte element2 : element)
                str.append(element2);
        return str.toString();
    }

    /**
     * Convert a puzzle string into an array
     */
    public static byte[][] fromMatrixString(String string) {
        byte[][] matrix = new byte[(int) Math.sqrt(string.length())][(int) Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (byte) (string.charAt(j + i * 9) - '0');
        return matrix;
    }

    /**
     * Convert an array into a matrix string
     */
    public static String toChangeMatrixString(boolean[][] matrix) {
        StringBuilder str = new StringBuilder();
        for (boolean[] element : matrix)
            for (boolean element2 : element)
                if (element2)
                    str.append(1);
                else
                    str.append(0);
        return str.toString();
    }

    /**
     * Convert a puzzle string into an array
     */
    public static boolean[][] fromChangeMatrixString(String string) {
        boolean[][] matrix = new boolean[(int) Math.sqrt(string.length())][(int) Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (string.charAt(j + i * 9) - '0') == 1;

        return matrix;
    }
}
