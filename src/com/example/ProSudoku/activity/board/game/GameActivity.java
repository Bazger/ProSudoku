package com.example.ProSudoku.activity.board.game;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.os.*;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.*;
import com.example.ProSudoku.*;
import com.example.ProSudoku.activity.board.IGameBoardActivity;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.activity.scores.DB;
import com.example.ProSudoku.logic.*;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;
import com.example.ProSudoku.plugin.ShowFinishedNumbersPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.ProSudoku.logic.SudokuRulesUtils.getNumberSpots;
import static com.example.ProSudoku.logic.SudokuRulesUtils.isSudokuFeasible;

public class GameActivity extends Activity implements IGameBoardActivity {

    private byte[][] MemoryMatrix;
    private byte[][] AnswerMatrix;
    private boolean[][] ChangeMatrix;

    public static final int matrixRectCount = 9;

    public static final String KEY_DIFFICULTY =
            "org.example.sudoku.difficulty";

    public static final int DIFFICULTY_BEGINNER = 0;
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;

    public static final int DIFFICULTY_CONTINUE = -1;

    public final static String PREF_MATRIX = "matrix";
    public final static String PREF_CHANGE_MATRIX = "change_matrix";
    public final static String PREF_TIME = "seconds";
    public final static String PREF_ANSWER_MATRIX = "answer_matrix";
    public final static String PREF_HINTS = "hints";
    public final static String PREF_DIFFICULTY = "difficulty";
    public final static String PREF_TIMER_STOP = "timer_stop";

    //Board for testing scores.
    final String testMatrix = "123456789456789123789123456231674895875912364694538217317265948542897631968341570";

    private IRandomizer randomizer = new DefaultRandomizer();
    private ISudokuSolver sudokuSolver = new SimpleSudokuSolver();
    private ISudokuGenerator sudokuGenerator = new SimpleSudokuGenerator();

    private Resources res;

    private GameBoardView gameBoardView;
    private Button buttonHints;
    private Dialog finishDialog;
    private Dialog scoresDialog;
    private DB db;

    private static List<GameBoardViewPlugin> plugins;

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
        return this.plugins;
    }


    private Handler mHandler;
    private boolean mStarted;
    private long milliseconds;
    private long saveSeconds;
    private long seconds;
    private boolean isTimerStoped;
    private int hintsCount;
    private int difficulty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefsActivity.setSettings(this);
        super.onCreate(savedInstanceState);
        initPlugins();
        setContentView(R.layout.gametable);
        PrefsActivity.setBackground(this);
        res = getResources();

        MemoryMatrix = new byte[matrixRectCount][matrixRectCount];
        ChangeMatrix = new boolean[matrixRectCount][matrixRectCount];
        AnswerMatrix = new byte[matrixRectCount][matrixRectCount];
        buttonHints = (Button) findViewById(R.id.hints_button);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        int diff = getIntent().getIntExtra(KEY_DIFFICULTY,
                DIFFICULTY_EASY);
        getMatrix(diff);

        gameBoardView = (GameBoardView) findViewById(R.id.matrix_view);

		/*
         * Database creating
		 */
        db = new DB(this);
        db.open();

        buttonHints.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (hintsCount > 0) {
                    Hints();
                    hintsCount--;
                    buttonHints.setText(getResources().getString(R.string.hints_label) + " " + hintsCount);
                    if (hintsCount == 0)
                        ((ViewManager) buttonHints.getParent()).removeView(buttonHints);
                }
            }
        });

        mHandler = new Handler();

        getIntent().putExtra(KEY_DIFFICULTY, DIFFICULTY_CONTINUE);

		/*
         * Scores Dialog creating
		 */
        scoresDialog = new Dialog(this);
        scoresDialog.setContentView(R.layout.scores_dialog);
        scoresDialog.setTitle(R.string.new_score_label);
        scoresDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText dialogName = (EditText) scoresDialog.findViewById(R.id.dialogEditTextName);
        final TextView dialogTextView = (TextView) scoresDialog.findViewById(R.id.dialogTextView);
        Button dialogButtonSave = (Button) scoresDialog.findViewById(R.id.dialogButtonSave);
        // if button is clicked, save the score and go to finish dialog

        dialogButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogName.getText().length() != 0) {
                    db.addRec(dialogName.getText().toString(), Long.parseLong(getPreferences(MODE_PRIVATE).getString(PREF_TIME, null)), DB.Dif.values()[difficulty]);
                    scoresDialog.dismiss();
                    finishDialog.show();
                } else
                    dialogTextView.setText("You need to fill the field below");
            }

        });


        Button dialogButtonContinue = (Button) scoresDialog.findViewById(R.id.dialogButtonContinue);
        // if button is clicked, close this dialog and open finish dialog
        dialogButtonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoresDialog.dismiss();
                finishDialog.show();
            }
        });


		/*
         * Finish Dialog creating
		 */
        finishDialog = new Dialog(this);
        finishDialog.setContentView(R.layout.finish_dialog);
        finishDialog.setTitle(R.string.welldone_label);


        Button dialogButtonNew = (Button) finishDialog.findViewById(R.id.dialogButtonNew);
        // if button is clicked, open new game diaolog
        dialogButtonNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewGameDialog();
            }
        });

        Button dialogButtonFinish = (Button) finishDialog.findViewById(R.id.dialogButtonFinish);
        // if button is clicked, close the custom finishDialog
        dialogButtonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPreferences(MODE_PRIVATE).edit().clear().commit();
                finishDialog.dismiss();
                finish();
            }
        });


        finishDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface arg0) {
                getPreferences(MODE_PRIVATE).edit().clear().commit();
                finish();
            }
        });
    }

    private void initPlugins()
    {
        plugins = new ArrayList<GameBoardViewPlugin>(Arrays.asList(
                new ShowFinishedNumbersPlugin(this))
        );
    }

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mStarted && !isSudokuFeasible(MemoryMatrix) || getNumberSpots(MemoryMatrix) != matrixRectCount * matrixRectCount) {
                seconds = saveSeconds + (System.currentTimeMillis() - milliseconds) / 1000;
                setTitle(String.format("%02d:%02d", seconds / 60, seconds % 60));
                mHandler.postDelayed(mRunnable, 1000L);
            } else
                setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
        }

    };

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        // TODO Auto-generated method stub
        super.dispatchTouchEvent(ev);
        /*float evY = ev.getY();
        if (!mStarted && evY > actionBarHeight) {
            milliseconds = System.currentTimeMillis();
            mStarted = true;
            mHandler.postDelayed(mRunnable, 1000L);
            setTitle(String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60));
            return true;
        }*/
        if (getNumberSpots(MemoryMatrix) == matrixRectCount * matrixRectCount && !finishDialog.isShowing() && isSudokuFeasible(MemoryMatrix)) {
            if (!isTimerStoped) {
                isTimerStoped = true;
                setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
                getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Long.toString(seconds)).commit();
                for (int i = 0; i < MemoryMatrix.length; i++)
                    for (int j = 0; j < MemoryMatrix[i].length; j++)
                        ChangeMatrix[i][j] = false;
                savePreferences();
            }

            Cursor c = db.getQuery(null, DB.COLUMN_DIFFICULTY + " == ?", new String[]{difficulty + ""}, null, null, DB.COLUMN_TIME);
            c.moveToLast();
            if (c.getCount() != 0) {
                if (c.getLong(c.getColumnIndex(DB.COLUMN_TIME)) > Long.parseLong(getPreferences(MODE_PRIVATE).getString(PREF_TIME, null)) || c.getCount() < DB.tableCount)
                    scoresDialog.show();
                else {
                    finishDialog.show();
                }
                return true;
            } else
                scoresDialog.show();
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isSudokuFeasible(MemoryMatrix) || getNumberSpots(MemoryMatrix) != matrixRectCount * matrixRectCount) {
            String str = getPreferences(MODE_PRIVATE).getString(PREF_TIME, null);
            saveSeconds = Long.parseLong(str, 10);
            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    milliseconds = System.currentTimeMillis();
                    mStarted = true;
                    mHandler.postDelayed(mRunnable, 1000L);
                    setTitle(String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60));
                }

            }.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStarted = false;
        mHandler.removeCallbacks(mRunnable);
        if (!isTimerStoped && seconds != 0) {
            getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Long.toString(seconds)).commit();
            setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
        }
        if (!isTimerStoped) {
            savePreferences();
        }
    }

    private void savePreferences() {
        getPreferences(MODE_PRIVATE).edit().putString(PREF_MATRIX,
                toMatrixString(MemoryMatrix)).apply();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_ANSWER_MATRIX,
                toMatrixString(AnswerMatrix)).apply();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_CHANGE_MATRIX,
                toChangeMatrixString(ChangeMatrix)).apply();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_HINTS, Integer.toString(hintsCount)).apply();
        getPreferences(MODE_PRIVATE).edit().putString(PREF_TIMER_STOP, Boolean.toString(isTimerStoped)).apply();
    }

    /**
     * Given a difficulty level, come up with a new puzzle
     */
    private void getMatrix(int diff) {
        switch (diff) {
            case DIFFICULTY_CONTINUE:
                String str = getPreferences(MODE_PRIVATE).getString(PREF_MATRIX, null);
                MemoryMatrix = fromMatrixString(str);
                str = getPreferences(MODE_PRIVATE).getString(PREF_ANSWER_MATRIX, null);
                AnswerMatrix = fromMatrixString(str);
                str = getPreferences(MODE_PRIVATE).getString(PREF_CHANGE_MATRIX, null);
                ChangeMatrix = fromChangeMatrixString(str);
                str = getPreferences(MODE_PRIVATE).getString(PREF_TIME, null);
                saveSeconds = Long.parseLong(str, 10);
                str = getPreferences(MODE_PRIVATE).getString(PREF_HINTS, null);
                hintsCount = Integer.parseInt(str, 10);
                str = getPreferences(MODE_PRIVATE).getString(PREF_DIFFICULTY, null);
                difficulty = Integer.parseInt(str, 10);
                str = getPreferences(MODE_PRIVATE).getString(PREF_TIMER_STOP, null);
                isTimerStoped = Boolean.parseBoolean(str);
                break;
            case DIFFICULTY_HARD:
                getMatrixHelper(24 + randomizer.GetInt(3), DIFFICULTY_HARD, 0);
                break;
            case DIFFICULTY_MEDIUM:
                getMatrixHelper(27 + randomizer.GetInt(3), DIFFICULTY_MEDIUM, 1);
                break;
            case DIFFICULTY_EASY:
                getMatrixHelper(30 + randomizer.GetInt(3), DIFFICULTY_EASY, 2);
                break;
            case DIFFICULTY_BEGINNER:
            default:
                getMatrixHelper(33 + randomizer.GetInt(3), DIFFICULTY_BEGINNER, 3);
                break;
        }
        setTitle("(" + String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60) + ")");
        if (hintsCount == 0)
            ((ViewManager) buttonHints.getParent()).removeView(buttonHints);
        else
            buttonHints.setText(getResources().getString(R.string.hints_label) + " " + hintsCount);
    }

    private void getMatrixHelper(int generateCellsCount, int difficulty, int hintsCount) {
		/*Check if sudoku generated correctly, else return to main screen*/
        if (!sudokuGenerator.generate(generateCellsCount, MemoryMatrix)) {
            //TODO: return case;
        }
        setChangeMatrix();
        AnswerMatrix = getData();
        sudokuSolver.solve(AnswerMatrix);
        this.difficulty = difficulty;
        getPreferences(MODE_PRIVATE).edit().putString(PREF_DIFFICULTY, Integer.toString(difficulty)).apply();
        this.saveSeconds = 0;
        getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Long.toString(this.saveSeconds)).apply();
        this.hintsCount = hintsCount;
    }

    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulty,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface,
                                                int i) {
                                finish();
                                startGame(i);
                            }
                        })
                .show();
    }

    private void startGame(int i) {
        Intent intent = new Intent(GameActivity.this, GameActivity.class);
        intent.putExtra(GameActivity.KEY_DIFFICULTY, i);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_paper:
                openNewGameDialog();
                return true;
            case R.id.clear_all_button:
                for (int i = 0; i < MemoryMatrix.length; i++)
                    for (int j = 0; j < MemoryMatrix[i].length; j++)
                        if (ChangeMatrix[i][j])
                            MemoryMatrix[i][j] = 0;
                gameBoardView.invalidate();
                return true;
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
        if (data == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("isChanged", true);
        setResult(RESULT_OK, intent);
        finish();
        startActivity(getIntent());
    }

    private byte[][] getData() {
        byte[][] result = new byte[MemoryMatrix.length][];
        for (int i = 0; i < MemoryMatrix.length; i++) {
            result[i] = Arrays.copyOf(MemoryMatrix[i], MemoryMatrix[i].length);
        }
        return result;
    }

    private void setData(byte[][] original) {
        for (int i = 0; i < original.length; i++) {
            MemoryMatrix[i] = Arrays.copyOf(original[i], original[i].length);
        }
    }

    private void setChangeMatrix() {
        for (int i = 0; i < MemoryMatrix.length; i++) {
            for (int j = 0; j < MemoryMatrix[i].length; j++) {
                if (MemoryMatrix[i][j] > 0)
                    ChangeMatrix[i][j] = false;
            }
        }
    }

    /**
     * Convert an array into a matrix string
     */
    static private String toMatrixString(byte[][] matrix) {
        StringBuilder str = new StringBuilder();
        for (byte[] element : matrix)
            for (byte element2 : element)
                str.append(element2);
        return str.toString();
    }

    /**
     * Convert a puzzle string into an array
     */
    private static byte[][] fromMatrixString(String string) {
        byte[][] matrix = new byte[(int) Math.sqrt(string.length())][(int) Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (byte) (string.charAt(j + i * 9) - '0');
        return matrix;
    }

    /**
     * Convert an array into a matrix string
     */
    private static String toChangeMatrixString(boolean[][] matrix) {
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
    private static boolean[][] fromChangeMatrixString(String string) {
        boolean[][] matrix = new boolean[(int) Math.sqrt(string.length())][(int) Math.sqrt(string.length())];
        for (int i = 0; i < matrix.length; i++)
            for (int j = 0; j < matrix[i].length; j++)
                matrix[i][j] = (string.charAt(j + i * 9) - '0') == 1;

        return matrix;
    }

    private void Hints() {
        Point points[] = new Point[matrixRectCount * matrixRectCount];
        int count = 0;
        for (int i = 0; i < matrixRectCount; i++)
            for (int j = 0; j < matrixRectCount; j++)
                if (ChangeMatrix[i][j] && (MemoryMatrix[i][j] != AnswerMatrix[i][j] || MemoryMatrix[i][j] == 0)) {
                    points[count++] = new Point(i, j);
                }
        int num = randomizer.GetInt(count);
        MemoryMatrix[points[num].x][points[num].y] = AnswerMatrix[points[num].x][points[num].y];
        ChangeMatrix[points[num].x][points[num].y] = false;
        gameBoardView.invalidate();
    }
}