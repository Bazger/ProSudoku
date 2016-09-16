package com.example.ProSudoku.logic;

import com.example.ProSudoku.DefaultRandomizer;
import com.example.ProSudoku.IRandomizer;
import java.util.Arrays;

import static com.example.ProSudoku.logic.SudokuRulesUtils.getNumberSpots;
import static com.example.ProSudoku.logic.SudokuRulesUtils.isSudokuFeasible;

public class SimpleSudokuGenerator implements ISudokuGenerator{

    private static 	final int numberOfTries = 1000000;
    private IRandomizer randomizer = new DefaultRandomizer();
    private enum Ret {Unique, NotUnique, NoSolution}

    /**
     * Generate a new Sudoku from the template.
     * @param spots Number of set spots in Sudoku.
     * @param gameBoard Game matrix
     * @return True if sudoku generated normally
     */
    public boolean generate(int spots, byte[][] gameBoard) {
        // Number of set spots.
        int num = getNumberSpots(gameBoard);
        // num - number of start table numbers
        if (!isSudokuFeasible(gameBoard) || num > spots) {
            // The supplied data is not feasible.
            // - or -
            // The supplied data has too many spots set.
            return false;
        }

        /////////////////////////////////////
        // Randomize spots
        /////////////////////////////////////

        byte[][] originalData = getCopyOfGameBoard(gameBoard);

        for (long tries = 0; tries < numberOfTries; tries++) {
            // Try to generate spots
            if (generateSpots(spots - num, gameBoard)) {
                // Test if unique solution.
                if (isSudokuUnique(gameBoard)) {
                    return true;
                }
            }

            // Start over.
            setOriginalBoardToGameBoard(originalData, gameBoard);
        }

        return false;
    }


    /**
     * Test generated Sudoku for solvability.
     * A true Sudoku has one and only one solution.
     *
     * @param gameBoard Game matrix
     * @return True if Sudoku is unique
     */
    private boolean isSudokuUnique(byte[][] gameBoard) {
        byte[][] m;
        m = getCopyOfGameBoard(gameBoard);
        boolean b = TestUniqueness(gameBoard) == Ret.Unique;
        setOriginalBoardToGameBoard(m, gameBoard);
        return b;
    }

    /**
     * Generate spots
     */
    private boolean generateSpots(int spots, byte[][] gameBoard) {
        for (int i = 0; i < spots; i++) {
            int xRand, yRand;

            do // BUG: Random
            {
                xRand = randomizer.GetInt(9);
                yRand = randomizer.GetInt(9);
            } while (gameBoard[xRand][yRand] != 0);

            /////////////////////////////////////
            // Get feasible values for spot.
            /////////////////////////////////////

            // Set M of possible solutions
            byte[] M = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

            // Remove used numbers in the vertical direction
            for (int a = 0; a < 9; a++)
                M[gameBoard[a][yRand]] = 0;

            // Remove used numbers in the horizontal direction
            for (int b = 0; b < 9; b++)
                M[gameBoard[xRand][b]] = 0;

            int n = (int) Math.sqrt(gameBoard.length);
            int sectorX = xRand / n;
            int sectorY = yRand / n;
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    M[gameBoard[a + sectorX * n][b + sectorY * n]] = 0;
                }
            }

            int cM = 0;
            // Calculate cardinality of M
            for (int d = 1; d < 10; d++)
                cM += M[d] == 0 ? 0 : 1;

            // Is there a number to use?
            if (cM > 0) {
                int e;

                do {
                    // Randomize number from the feasible set M
                    e = randomizer.GetInt(1, 10);
                } while (M[e] == 0);

                // Set number in Sudoku
                gameBoard[xRand][yRand] = (byte) e;
            } else {
                // Error
                return false;
            }
        }

        // Successfully generated a feasible set.
        return true;
    }

    /**
     * Is there one and only one solution?
     * @param gameBoard Game matrix
     */
    private Ret TestUniqueness(byte[][] gameBoard) {
        // Find untouched location with most information
        int xp = 0;
        int yp = 0;
        byte[] Mp = null;
        int cMp = 10;

        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                // Is this spot unused?
                if (gameBoard[y][x] == 0) {
                    // Set M of possible solutions
                    byte[] M = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

                    // Remove used numbers in the vertical direction
                    for (int a = 0; a < 9; a++)
                        M[gameBoard[a][x]] = 0;

                    // Remove used numbers in the horizontal direction
                    for (int b = 0; b < 9; b++)
                        M[gameBoard[y][b]] = 0;

                    // Remove used numbers in the sub square. BUG: Bad generation
                    //int squareIndex = m_subSquare[y, x];
                    //for (int c = 0; c < 9; c++)
                    //{
                    //    EntryPoint p = m_subIndex[squareIndex, c];
                    //    M[m_sudoku[p.x, p.y]] = 0;
                    //}

                    int n = (int) Math.sqrt(gameBoard.length);
                    int sectorY = y / n;
                    int sectorX = x / n;
                    for (int a = 0; a < n; a++) {
                        for (int b = 0; b < n; b++) {
                            M[gameBoard[a + sectorY * n][b + sectorX * n]] = 0;
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
            return Ret.Unique;

        // Couldn't find a solution?
        if (cMp == 0)
            return Ret.NoSolution;

        // Try elements
        int success = 0;
        for (int i = 1; i < 10; i++) {
            if (Mp[i] != 0) {

                gameBoard[yp][xp] = Mp[i];

                switch (TestUniqueness(gameBoard)) {
                    case Unique:
                        success++;
                        break;

                    case NotUnique:
                        return Ret.NotUnique;

                    case NoSolution:
                        break;
                }

                // More than one solution found?
                if (success > 1)
                    return Ret.NotUnique;
            }
        }

        // Restore to original state.
        gameBoard[yp][xp] = 0;

        switch (success) {
            case 0:
                return Ret.NoSolution;

            case 1:
                return Ret.Unique;

            default:
                // Won't happen.
                return Ret.NotUnique;
        }
    }

    private byte[][] getCopyOfGameBoard(byte[][] gameBoard) {
        byte[][] result = new byte[gameBoard.length][];
        for (int i = 0; i < gameBoard.length; i++) {
            result[i] = Arrays.copyOf(gameBoard[i], gameBoard[i].length);
        }
        return result;
    }

    private void setOriginalBoardToGameBoard(byte[][] original, byte[][] gameBoard) {
        for (int i = 0; i < original.length; i++) {
            gameBoard[i] = Arrays.copyOf(original[i], original[i].length);
        }
    }
}
