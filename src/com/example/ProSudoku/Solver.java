package com.example.ProSudoku;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Vanya on 07.03.2015.
 */
public class Solver extends Activity implements View.OnClickListener, IMatrix {

    TextView message;
    MatrixView matrixView;

    byte[][] MemoryMatrix;
    boolean[][] ChangeMatrix;

    final int matrixRectCount = 9;


    @Override
    public byte[][] getMemoryMatrix() {
        return MemoryMatrix;
    }

    @Override
    public boolean[][] getChangeMatrix() {
        return ChangeMatrix;
    }

    @Override
    public int getMatrixRectCount() {
        return matrixRectCount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solver);
        View solve_button = findViewById(R.id.solve_button);
        solve_button.setOnClickListener(this);
        View clear_button = findViewById(R.id.clear_button);
        clear_button.setOnClickListener(this);
        message = (TextView)findViewById(R.id.textView);
        matrixView = (MatrixView)findViewById(R.id.matrix_view);
        MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
        ChangeMatrix = new boolean[matrixRectCount][matrixRectCount];
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.solve_button:
                if (IsSudokuFeasible()) {
                    if (Solve())
                        message.setText("Solving was completed");
                }
                else
                    message.setText("Can't solve");
                break;
            case R.id.clear_button:
                MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
                message.setText("");
                break;
        }
        matrixView.Update();
    }

    public boolean matrixIsFull()
    {
        for(int i = 0; i < MemoryMatrix.length; i++)
            for (int j = 0; j < MemoryMatrix[i].length; j++)
                if(MemoryMatrix[i][j] == 0)
                    return false;
        return true;
    }

    /// <summary>
    /// Fast test if the data is feasible.
    /// Does not check if there is more than one solution.
    /// </summary>
    /// <returns>True if feasible</returns>
    public boolean IsSudokuFeasible()
    {
        for (int x = 0; x < 9; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                // Set M of possible solutions
                byte[] M = new byte[10];

                // Count used numbers in the vertical direction
                for (int a = 0; a < 9; a++)
                    M[MemoryMatrix[a][y]]++;
                // Sudoku feasible?
                if (!Feasible(M))
                    return false;

                M = new byte[10];
                // Count used numbers in the horizontal direction
                for (int b = 0; b < 9; b++)
                    M[MemoryMatrix[x][b]]++;
                if (!Feasible(M))
                    return false;

                M = new byte[10];
                // Count used numbers in the sub square.  BUG
                //int squareIndex = m_subSquare[y, x];
                //for (int c = 0; c < 9; c++)
                //{
                //    EntryPoint p = m_subIndex[squareIndex, c];
                //    if (p.x != y && p.y != x)
                //        M[m_sudoku[p.x, p.y]]++;
                //}
                //if (!Feasible(M))
                //    return false;

                int n = (int)Math.sqrt(matrixRectCount);
                int sectorX = x / n;
                int sectorY = y / n;
                for (int i = 0; i < n; i++)
                {
                    for (int j = 0; j < n; j++)
                    {
                        if (i + sectorX * n != x && j + sectorY * n != y)
                            M[MemoryMatrix[i + sectorX * n][j + sectorY * n]]++;
                    }
                }
                if (!Feasible(M))
                    return false;

            }
        }

        return true;
    }

    private boolean Feasible(byte[] M)
    {
        for (int d = 1; d < matrixRectCount + 1; d++)
            if (M[d] > 1)
                return false;

        return true;
    }

    /// <summary>
    /// Solves the given Sudoku.
    /// </summary>
    /// <returns>Success</returns>
    public boolean Solve()
    {
        // Find untouched location with most information
        int xp = 0;
        int yp = 0;
        byte[] Mp = null;
        int cMp = 10;

        for (int x = 0; x < 9; x++)
        {
            for (int y = 0; y < 9; y++)
            {
                // Is this spot unused?
                if (MemoryMatrix[x][y] == 0)
                {
                    // Set M of possible solutions
                    byte[] M = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

                    // Remove used numbers in the vertical direction
                    for (int a = 0; a < 9; a++)
                        M[MemoryMatrix[a][y]] = 0;

                    // Remove used numbers in the horizontal direction
                    for (int b = 0; b < 9; b++)
                        M[MemoryMatrix[x][b]] = 0;

                    // Remove used numbers in the sub square. BUG:
                    //int squareIndex = m_subSquare[y, x];
                    //for (int c = 0; c < 9; c++)
                    //{
                    //    EntryPoint p = m_subIndex[squareIndex, c];
                    //    M[m_sudoku[p.x, p.y]] = 0;
                    //}

                    int n = (int)Math.sqrt(MemoryMatrix.length);
                    int sectorX = x / n;
                    int sectorY = y / n;
                    for (int a = 0; a < n; a++)
                    {
                        for (int b = 0; b < n; b++)
                        {
                            M[MemoryMatrix[a + sectorX * n][b + sectorY * n]] = 0;
                        }
                    }


                    int cM = 0;
                    // Calculate cardinality of M
                    for (int d = 1; d < 10; d++)
                        cM += M[d] == 0 ? 0 : 1;

                    // Is there more information in this spot than in the best yet?
                    if (cM < cMp)
                    {
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
        for (int i = 1; i < 10; i++)
        {
            if (Mp[i] != 0)
            {
                MemoryMatrix[xp][yp] = Mp[i];
                if (Solve())
                    return true;
            }
        }

        // Restore to original state.
        MemoryMatrix[xp][yp] = 0;
        return false;
    }
}
