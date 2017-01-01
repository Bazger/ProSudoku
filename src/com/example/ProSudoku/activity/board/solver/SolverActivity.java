package com.example.ProSudoku.activity.board.solver;

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
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.R;
import com.example.ProSudoku.activity.board.IGameBoardActivity;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.logic.ISudokuGenerator;
import com.example.ProSudoku.logic.ISudokuSolver;
import com.example.ProSudoku.logic.SimpleSudokuSolver;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;

import java.util.ArrayList;
import java.util.List;

import static com.example.ProSudoku.logic.SudokuRulesUtils.isSudokuFeasible;

/**
 * Created by Vanya on 07.03.2015
 */
public class SolverActivity extends Activity implements View.OnClickListener, IGameBoardActivity {
    private static final String TAG = "Solver";

    private TextView message;
    private GameBoardView gameBoardView;
    private Button solve_but;

    private ISudokuSolver sudokuSolver = new SimpleSudokuSolver();
    private ISudokuGenerator sudokuGenerator;

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
    public List<GameBoardViewPlugin> getPlugins(){
        return new ArrayList<GameBoardViewPlugin>();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
	    PrefsActivity.setSettings(this);
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.solver);
	    PrefsActivity.setBackground(this);

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

        gameBoardView = (GameBoardView)findViewById(R.id.matrix_view);
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.solve_button:
                if(!isSolved) {
                    if (isSudokuFeasible(MemoryMatrix)) {
                        for (int i = 0; i < matrixRectCount; i++)
                            for (int j = 0; j < matrixRectCount; j++)
                                if (MemoryMatrix[i][j] == 0)
                                    ChangeMatrix[i][j] = false;
                        if (sudokuSolver.solve(MemoryMatrix))
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
	            isSolved = false;
	            solve_but.setText("Solve");
                break;
        }
        gameBoardView.invalidate();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // Save the current matrix
        getPreferences(MODE_PRIVATE).edit().putString(PREF_MATRIX,
                toMatrixString(MemoryMatrix)).commit();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_CHANGE_MATRIX,
                toChangeMatrixString(ChangeMatrix)).apply();
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
                Intent intent = new Intent(this, PrefsActivity.class);
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
}
