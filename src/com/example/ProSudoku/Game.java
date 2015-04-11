package com.example.ProSudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.*;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Game extends Activity implements IMatrix {
    private static final String TAG = "Sudoku";

    private byte[][] MemoryMatrix;
    private boolean[][] ChangeMatrix;

    private final int matrixRectCount = 9;
    final int numberOfTries = 1000000;

    public static final String KEY_DIFFICULTY =
            "org.example.sudoku.difficulty";

    public static final int DIFFICULTY_BEGINNER = 0;
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;

    protected static final int DIFFICULTY_CONTINUE = -1;

    final static String PREF_MATRIX = "matrix";
    final static String PREF_CHANGE_MATRIX = "change_matrix";

    IRandomizer Randomizer = new DefaultRandomizer();

    private TextView textView;

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


    private Handler mHandler;
    private boolean mStarted;

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted) {
                long seconds = (System.currentTimeMillis()) / 1000;
                //textView.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gametable);
        Log.d(TAG, "onCreate");

        MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
        ChangeMatrix = new boolean[matrixRectCount][matrixRectCount];

        int diff = getIntent().getIntExtra(KEY_DIFFICULTY,
                DIFFICULTY_EASY);
        getMatrix(diff);

        textView = (TextView) findViewById(R.id.textView);
        mHandler = new Handler();

        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);

        //chronometer = (Chronometer) findViewById(R.id.chronometer);
        //chronometer.setBase(SystemClock.elapsedRealtime());
        //chronometer.start();

        /*chronometer
                .setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

                    @Override
                    public void onChronometerTick(Chronometer chronometer) {
                        // TODO Auto-generated method stub
                        long myElapsedMillis = SystemClock.elapsedRealtime()
                                - chronometer.getBase();

                        if (myElapsedMillis > 5000) {
                            String strElapsedMillis = "Прошло больше 5 секунд: "
                                    + myElapsedMillis;
                            Toast.makeText(Game.this,
                                    strElapsedMillis, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });*/


        //setContentView(new DrawView(this, 9, 2, 5));
    }


    @Override
    protected void onStart() {
        super.onStart();
        mStarted = true;
        mHandler.postDelayed(mRunnable, 1000L);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
    protected void onStop() {
        super.onStop();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
    }

    /** Given a difficulty level, come up with a new puzzle */
    private void getMatrix(int diff) {
        // TODO: Continue last game
        switch (diff) {
            case DIFFICULTY_CONTINUE:
                String str = getPreferences(MODE_PRIVATE).getString(PREF_MATRIX, null);
                MemoryMatrix = fromMatrixString(str);
                str = getPreferences(MODE_PRIVATE).getString(PREF_CHANGE_MATRIX, null);
                ChangeMatrix = fromChangeMatrixString(str);
                break;
            case DIFFICULTY_HARD:
                Generate(22 + Randomizer.GetInt(3));
                break;
            case DIFFICULTY_MEDIUM:
                Generate(25 + Randomizer.GetInt(6));
                break;
            case DIFFICULTY_EASY:
            default:
                Generate(30 + Randomizer.GetInt(6));
                break;
        }
    }

    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulty,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface,
                                                int i) {
                                getMatrix(i);
                            }
                        })
                .show();
    }

    /*public class CounterClass extends CountDownTimer
    {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        /*public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) + TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis) + TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println(hms);
            textView.setText(hms);

        }

        @Override
        public void onFinish() {

        }
    }*/

    public byte[][] getData()
    {
        byte[][] result = new byte[MemoryMatrix.length][];
        for (int i = 0; i < MemoryMatrix.length; i++) {
            result[i] = Arrays.copyOf(MemoryMatrix[i], MemoryMatrix[i].length);
        }
        return result;
    }

    public void setData(byte[][] original)
    {
        for (int i = 0; i < original.length; i++)
        {
            MemoryMatrix[i] = Arrays.copyOf(original[i], original[i].length);
        }
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

    /** Convert a puzzle string into an array */
    static protected boolean[][]  fromChangeMatrixString(String string) {
        boolean[][] matrix = new boolean[(int)Math.sqrt(string.length())][(int)Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                if((string.charAt(j + i * 9) - '0') == 1)
                    matrix[i][j] = true;
                else
                    matrix[i][j] = false;

        return matrix;
    }


    private enum Ret { Unique, NotUnique, NoSolution }

    /// <summary>
    /// Generate a new Sudoku from the template.
    /// </summary>
    /// <param name="spots">Number of set spots in Sudoku.</param>
    /// <param name="numberOfTries">Number of tries before ending generation.</param>
    /// <returns>(Number of tries, success)</returns>
    public boolean Generate(int spots)
    {
        // Number of set spots.
        int num = GetNumberSpots();
        // num - number of start table numbers
        if (!IsSudokuFeasible() || num > spots)
        {
            // The supplied data is not feasible.
            // - or -
            // The supplied data has too many spots set.
            return false;
        }

        /////////////////////////////////////
        // Randomize spots
        /////////////////////////////////////

        byte[][] originalData = getData();

        for (long tries = 0; tries < numberOfTries; tries++)
        {
            // Try to generate spots
            if (Gen(spots - num))
            {
                // Test if unique solution.
                if (IsSudokuUnique())
                {
                    for (int i = 0; i < MemoryMatrix.length; i++)
                    {
                        for (int j = 0; j < MemoryMatrix[i].length; j++) {
                            if (MemoryMatrix[i][j] > 0)
                                ChangeMatrix[i][j] = false;
                        }
                    }
                    return true;
                }
            }

            // Start over.
            setData(originalData);
        }

        return false;
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

    /// <summary>
    /// Test generated Sudoku for solvability.
    /// A true Sudoku has one and only one solution.
    /// </summary>
    /// <returns>True if unique</returns>
    public boolean IsSudokuUnique()
    {
        byte[][] m;
        m = getData();
        boolean b = TestUniqueness() == Ret.Unique;
        setData(m);
        return b;
    }


    // Generate spots
    private boolean Gen(int spots)
    {
        for (int i = 0; i < spots; i++)
        {
            int xRand, yRand;

            do // BUG: Random
            {
                xRand = Randomizer.GetInt(9);
                yRand = Randomizer.GetInt(9);
            } while (MemoryMatrix[xRand][yRand] != 0);

            /////////////////////////////////////
            // Get feasible values for spot.
            /////////////////////////////////////

            // Set M of possible solutions
            byte[] M = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

            // Remove used numbers in the vertical direction
            for (int a = 0; a < 9; a++)
                M[MemoryMatrix[a][yRand]] = 0;

            // Remove used numbers in the horizontal direction
            for (int b = 0; b < 9; b++)
                M[MemoryMatrix[xRand][b]] = 0;

            // Remove used numbers in the sub square. BUG: Bad generation
            //int squareIndex = m_subSquare[yRand, xRand];
            //for (int c = 0; c < 9; c++)
            //{
            //    EntryPoint p = m_subIndex[squareIndex, c];
            //    M[m_sudoku[p.x, p.y]] = 0;
            //}

            int n = (int)Math.sqrt(matrixRectCount);
            int sectorX = xRand / n;
            int sectorY = yRand / n;
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

            // Is there a number to use?
            if (cM > 0)
            {
                int e;

                do //BUG: Random
                {
                    // Randomize number from the feasible set M
                    e = Randomizer.GetInt(1, 10);
                } while (M[e] == 0);

                // Set number in Sudoku
                MemoryMatrix[xRand][yRand] = (byte)e;
            }
            else
            {
                // Error
                return false;
            }
        }

        // Successfully generated a feasible set.
        return true;
    }

    // Is there one and only one solution?
    private Ret TestUniqueness()
    {
        // Find untouched location with most information
        int xp = 0;
        int yp = 0;
        byte[] Mp = null;
        int cMp = 10;

        for (int y = 0; y < 9; y++)
        {
            for (int x = 0; x < 9; x++)
            {
                // Is this spot unused?
                if (MemoryMatrix[y][x] == 0)
                {
                    // Set M of possible solutions
                    byte[] M = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

                    // Remove used numbers in the vertical direction
                    for (int a = 0; a < 9; a++)
                        M[MemoryMatrix[a][x]] = 0;

                    // Remove used numbers in the horizontal direction
                    for (int b = 0; b < 9; b++)
                        M[MemoryMatrix[y][b]] = 0;

                    // Remove used numbers in the sub square. BUG: Bad generation
                    //int squareIndex = m_subSquare[y, x];
                    //for (int c = 0; c < 9; c++)
                    //{
                    //    EntryPoint p = m_subIndex[squareIndex, c];
                    //    M[m_sudoku[p.x, p.y]] = 0;
                    //}

                    int n = (int)Math.sqrt(matrixRectCount);
                    int sectorY = y / n;
                    int sectorX = x / n;
                    for (int a = 0; a < n; a++)
                    {
                        for (int b = 0; b < n; b++)
                        {
                            M[MemoryMatrix[a + sectorY * n][b + sectorX * n]] = 0;
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
            return Ret.Unique;

        // Couldn't find a solution?
        if (cMp == 0)
            return Ret.NoSolution;

        // Try elements
        int success = 0;
        for (int i = 1; i < 10; i++)
        {
            if (Mp[i] != 0)
            {

                MemoryMatrix[yp][xp] = Mp[i];

                switch (TestUniqueness())
                {
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
        MemoryMatrix[yp][xp] = 0;

        switch (success)
        {
            case 0:
                return Ret.NoSolution;

            case 1:
                return Ret.Unique;

            default:
                // Won't happen.
                return Ret.NotUnique;
        }
    }

    private boolean Feasible(byte[] M)
    {
        for (int d = 1; d < matrixRectCount + 1; d++)
            if (M[d] > 1)
                return false;

        return true;
    }

    private int GetNumberSpots()
    {
        int num = 0;

        for (int y = 0; y < matrixRectCount; y++)
            for (int x = 0; x < matrixRectCount; x++)
                num += MemoryMatrix[y][x] == 0 ? 0 : 1;

        return num;
    }
}