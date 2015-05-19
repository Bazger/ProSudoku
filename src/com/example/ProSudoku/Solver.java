package com.example.ProSudoku;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Vanya on 07.03.2015
 */
public class Solver extends Activity implements View.OnClickListener, IMatrix {
    private static final String TAG = "Solver";

    TextView message;
    MatrixView matrixView;
    Button solve_but;

    byte[][] MemoryMatrix;
    boolean[][] ChangeMatrix;

    boolean isSolved;

    final int matrixRectCount = 9;

    final static String PREF_MATRIX = "matrix";
    final static String PREF_CHANGE_MATRIX = "change_matrix";

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
	    Prefs.setSettings(this);
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.solver);
	    Prefs.setBackground(this);

        View solve_button = findViewById(R.id.solve_button);
        solve_button.setOnClickListener(this);
        View clear_button = findViewById(R.id.clear_all_button);
        clear_button.setOnClickListener(this);
        solve_but = (Button)findViewById(R.id.solve_button);


        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        message = (TextView)findViewById(R.id.textView);
        message.setText(getResources().getString(R.string.sudoku_solve_label));
        message.setTextColor(Color.WHITE);

        MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
        ChangeMatrix = new boolean[matrixRectCount][matrixRectCount];
        isSolved = false;

        if(getPreferences(MODE_PRIVATE).getString(PREF_MATRIX, null) != null) {
            String str = getPreferences(MODE_PRIVATE).getString(PREF_MATRIX, null);
            MemoryMatrix = fromMatrixString(str);
            str = getPreferences(MODE_PRIVATE).getString(PREF_CHANGE_MATRIX, null);
            ChangeMatrix = fromChangeMatrixString(str);
            for (int i = 0; i < matrixRectCount; i++)
                for (int j = 0; j < matrixRectCount; j++)
                    if (!ChangeMatrix[i][j]) {
                        isSolved = true;
                        solve_but.setText("Clear");
                        message.setText("Solving was completed");
                        break;
                    }
        }

        matrixView = (MatrixView)findViewById(R.id.matrix_view);
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.solve_button:
                if(!isSolved) {
                    if (IsSudokuFeasible()) {
                        for (int i = 0; i < matrixRectCount; i++)
                            for (int j = 0; j < matrixRectCount; j++)
                                if (MemoryMatrix[i][j] == 0)
                                    ChangeMatrix[i][j] = false;
                        if (Solve())
                            message.setText("Solving was completed");
                        isSolved = true;
                        solve_but.setText("Clear");
                    } else
                        message.setText("Can't solve");
                }
                else
                {
                    for (int i = 0; i < matrixRectCount; i++)
                        for (int j = 0; j < matrixRectCount; j++)
                            if (!ChangeMatrix[i][j]) {
                                MemoryMatrix[i][j] = 0;
                                ChangeMatrix[i][j] = true;
                            }
                    isSolved = false;
                    solve_but.setText("Solve");
                    message.setText(getResources().getString(R.string.sudoku_solve_label));
                }
                break;
            case R.id.clear_all_button:
                MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
                for (int i = 0; i < matrixRectCount; i++)
                    for (int j = 0; j < matrixRectCount; j++)
                        if (!ChangeMatrix[i][j]) {
                            ChangeMatrix[i][j] = true;
                        }
                message.setText(getResources().getString(R.string.sudoku_solve_label));
                break;
        }
        matrixView.Update();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // Save the current matrix
        getPreferences(MODE_PRIVATE).edit().putString(PREF_MATRIX,
                toMatrixString(MemoryMatrix)).commit();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_CHANGE_MATRIX,
                toChangeMatrixString(ChangeMatrix)).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, Prefs.class);
                startActivityForResult(intent, 1);
                return true;
            case android.R.id.home:
                finish();
                // More items go here (if any) ...
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
	    Intent intent = new Intent();
	    intent.putExtra("isChanged", true);
	    setResult(RESULT_OK, intent);
	    finish();
	    startActivity(getIntent());
    }

    /** Convert an array into a matrix string */
    static private String toMatrixString(byte[][] matrix)
    {
        StringBuilder str =  new StringBuilder();
        for(byte[] element : matrix)
            for(byte element2 : element )
                str.append(element2);
        return str.toString();
    }

    /** Convert a puzzle string into an array */
    static protected byte[][]  fromMatrixString(String string) {
        byte[][] matrix = new byte[(int)Math.sqrt(string.length())][(int)Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (byte)(string.charAt(j + i * 9) - '0');
        return matrix;
    }

    /** Convert a puzzle string into an array */
    static protected boolean[][]  fromChangeMatrixString(String string) {
        boolean[][] matrix = new boolean[(int)Math.sqrt(string.length())][(int)Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (string.charAt(j + i * 9) - '0') == 1;

        return matrix;
    }

    /** Convert an array into a matrix string */
    static private String toChangeMatrixString(boolean[][] matrix)
    {
        StringBuilder str =  new StringBuilder();
        for(boolean [] element : matrix)
            for(boolean element2 : element )
                if(element2)
                    str.append(1);
                else
                    str.append(0);
        return str.toString();
    }

    public boolean matrixIsFull()
    {
        for (byte[] aMemoryMatrix : MemoryMatrix)
            for (byte anAMemoryMatrix : aMemoryMatrix)
                if (anAMemoryMatrix == 0)
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
