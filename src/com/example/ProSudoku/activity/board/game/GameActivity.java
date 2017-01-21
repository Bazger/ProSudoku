package com.example.ProSudoku.activity.board.game;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.*;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.*;
import com.example.ProSudoku.*;
import com.example.ProSudoku.activity.board.GameBoardView;
import com.example.ProSudoku.activity.board.PluginHandlerActivity;
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.activity.scores.DB;
import com.example.ProSudoku.logic.*;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;
import com.example.ProSudoku.plugin.plugins.DetectNumberErrorsPlugin;
import com.example.ProSudoku.plugin.plugins.HighlightSameNumbersPlugin;
import com.example.ProSudoku.plugin.plugins.ShowFinishedNumbersPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.ProSudoku.Consts.BOARD_SIDE_RECT_COUNT;
import static com.example.ProSudoku.logic.SudokuLogicUtils.*;

public class GameActivity extends PluginHandlerActivity {

    private byte[][] MemoryMatrix;
    private byte[][] AnswerMatrix;
    private boolean[][] ChangeMatrix;

    public static final String KEY_DIFFICULTY =
            "org.example.sudoku.difficulty";

    public final static String PREF_MATRIX = "matrix";
    public final static String PREF_CHANGE_MATRIX = "change_matrix";
    public final static String PREF_TIME = "seconds";
    public final static String PREF_ANSWER_MATRIX = "answer_matrix";
    public final static String PREF_HINTS = "hints";
    public final static String PREF_DIFFICULTY = "difficulty";
    public final static String PREF_TIMER_STOP = "timer_stop";

    //Board for testing scores.
    final String testMatrix = "123456789456789123789123456231674895875912364694538217317265948542897631968341570";

    private IRandomizer defaultRandomizer = new DefaultRandomizer();
    private ISudokuSolver sudokuSolver = new SimpleSudokuSolver();

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
        return BOARD_SIDE_RECT_COUNT;
    }

    @Override
    public List<GameBoardViewPlugin> getPlugins() {
        return plugins;
    }


    private Handler timerRunnableHandler;
    private boolean timerRunnableStarted;
    private long milliseconds;
    private long saveSeconds;
    private long seconds;
    private boolean isTimerStopped;
    private int hintsCount;
    private Difficulty difficulty;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrefsActivity.setSettings(this);
        plugins = new ArrayList<GameBoardViewPlugin>(Arrays.asList(
                new HighlightSameNumbersPlugin(this),
                new DetectNumberErrorsPlugin(this),
                new ShowFinishedNumbersPlugin(this)
        ));

        super.onCreate(savedInstanceState);

        setContentView(R.layout.gametable);
        PrefsActivity.setBackground(this);

        MemoryMatrix = new byte[BOARD_SIDE_RECT_COUNT][BOARD_SIDE_RECT_COUNT];
        ChangeMatrix = new boolean[BOARD_SIDE_RECT_COUNT][BOARD_SIDE_RECT_COUNT];
        AnswerMatrix = new byte[BOARD_SIDE_RECT_COUNT][BOARD_SIDE_RECT_COUNT];
        buttonHints = (Button) findViewById(R.id.hints_button);

        final ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Difficulty diff = Difficulty.values()[getIntent().getIntExtra(KEY_DIFFICULTY,
                Difficulty.Continue.ordinal())];
        initializeGameSettings(diff);

        gameBoardView = (GameBoardView) findViewById(R.id.matrix_view);

        createDb();

        timerRunnableHandler = new Handler();

        getIntent().putExtra(KEY_DIFFICULTY, Difficulty.Continue);

        createScoresDialog();
        createFinishDialog();
    }

    /**
     * Database creating
     */
    private void createDb() {
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
    }

    /**
     * Scores Dialog creating
     */
    private void createScoresDialog() {
        scoresDialog = new Dialog(this);
        scoresDialog.setContentView(R.layout.scores_dialog);
        scoresDialog.setTitle(R.string.game_new_score_label);
        scoresDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        final EditText dialogName = (EditText) scoresDialog.findViewById(R.id.dialogEditTextName);
        final TextView dialogTextView = (TextView) scoresDialog.findViewById(R.id.dialogTextView);
        Button dialogButtonSave = (Button) scoresDialog.findViewById(R.id.dialogButtonSave);
        // if button is clicked, save the score and go to finish dialog

        dialogButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogName.getText().length() != 0) {
                    db.addRec(dialogName.getText().toString(), getPreferences(MODE_PRIVATE).getLong(PREF_TIME, 0), DB.Dif.values()[difficulty.ordinal()]);
                    scoresDialog.dismiss();
                    finishDialog.show();
                } else
                    dialogTextView.setText(R.string.game_need_to_fill);
            }

        });


        Button dialogButtonContinue = (Button) scoresDialog.findViewById(R.id.dialogButtonContinue);
        // if button is clicked, close this dialog and show finish dialog
        dialogButtonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scoresDialog.dismiss();
                finishDialog.show();
            }
        });
    }

    /**
     * Finish Dialog creating
     */
    private void createFinishDialog() {
        finishDialog = new Dialog(this);
        finishDialog.setContentView(R.layout.finish_dialog);
        finishDialog.setTitle(R.string.welldone_label);


        Button dialogButtonNew = (Button) finishDialog.findViewById(R.id.dialogButtonNew);
        // if button is clicked, show new game diaolog
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

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (timerRunnableStarted && !isSudokuFeasible(MemoryMatrix) || getNumberSpots(MemoryMatrix) != BOARD_SIDE_RECT_COUNT * BOARD_SIDE_RECT_COUNT) {
                seconds = saveSeconds + (System.currentTimeMillis() - milliseconds) / 1000;
                setTitle(String.format("%02d:%02d", seconds / 60, seconds % 60));
                timerRunnableHandler.postDelayed(timerRunnable, 1000L);
            } else
                setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
        }

    };

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        // TODO Auto-generated method stub
        super.dispatchTouchEvent(ev);
        /*float evY = ev.getY();
        if (!timerRunnableStarted && evY > actionBarHeight) {
            milliseconds = System.currentTimeMillis();
            timerRunnableStarted = true;
            timerRunnableHandler.postDelayed(timerRunnable, 1000L);
            setTitle(String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60));
            return true;
        }*/
        if (getNumberSpots(MemoryMatrix) == BOARD_SIDE_RECT_COUNT * BOARD_SIDE_RECT_COUNT && !finishDialog.isShowing() && isSudokuFeasible(MemoryMatrix)) {
            if (!isTimerStopped) {
                isTimerStopped = true;
                setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
                getPreferences(MODE_PRIVATE).edit().putLong(PREF_TIME, seconds).apply();
                for (int i = 0; i < MemoryMatrix.length; i++)
                    for (int j = 0; j < MemoryMatrix[i].length; j++)
                        ChangeMatrix[i][j] = false;
                savePreferences();
            }

            Cursor c = db.getQuery(null, DB.COLUMN_DIFFICULTY + " == ?", new String[]{difficulty + ""}, null, null, DB.COLUMN_TIME);
            c.moveToLast();
            if (c.getCount() != 0) {
                if (c.getLong(c.getColumnIndex(DB.COLUMN_TIME)) > getPreferences(MODE_PRIVATE).getLong(PREF_TIME, 0) || c.getCount() < DB.tableCount)
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
        if (!isSudokuFeasible(MemoryMatrix) || getNumberSpots(MemoryMatrix) != BOARD_SIDE_RECT_COUNT * BOARD_SIDE_RECT_COUNT) {
            saveSeconds = getPreferences(MODE_PRIVATE).getLong(PREF_TIME, 0);
            new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {
                }

                public void onFinish() {
                    milliseconds = System.currentTimeMillis();
                    timerRunnableStarted = true;
                    timerRunnableHandler.postDelayed(timerRunnable, 1000L);
                    setTitle(String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60));
                }

            }.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerRunnableStarted = false;
        timerRunnableHandler.removeCallbacks(timerRunnable);
        if (!isTimerStopped && seconds != 0) {
            getPreferences(MODE_PRIVATE).edit().putLong(PREF_TIME, seconds).commit();
            setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
        }
        if (!isTimerStopped) {
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
        getPreferences(MODE_PRIVATE).edit().putInt(PREF_HINTS, hintsCount).apply();
        getPreferences(MODE_PRIVATE).edit().putBoolean(PREF_TIMER_STOP, isTimerStopped).apply();
    }

    /**
     * Given a difficulty level, come up with a new puzzle
     */
    private void initializeGameSettings(Difficulty diff) {
            switch (diff) {
                case Continue:
                    MemoryMatrix = fromMatrixString(getPreferences(MODE_PRIVATE).getString(PREF_MATRIX, null));
                    AnswerMatrix = fromMatrixString(getPreferences(MODE_PRIVATE).getString(PREF_ANSWER_MATRIX, null));
                    ChangeMatrix = fromChangeMatrixString(getPreferences(MODE_PRIVATE).getString(PREF_CHANGE_MATRIX, null));

                    saveSeconds = getPreferences(MODE_PRIVATE).getLong(PREF_TIME, 0);
                    hintsCount = getPreferences(MODE_PRIVATE).getInt(PREF_HINTS, 0);
                    difficulty = Difficulty.values()[getPreferences(MODE_PRIVATE).getInt(PREF_DIFFICULTY, 0)];
                    isTimerStopped = getPreferences(MODE_PRIVATE).getBoolean(PREF_TIMER_STOP, false);
                    break;
                case Hard:
                    initializeMatrices(Difficulty.Hard, 0);
                    break;
                case Medium:
                    initializeMatrices(Difficulty.Medium, 0);
                    break;
                case Easy:
                    initializeMatrices(Difficulty.Easy, 1);
                    break;
                case Beginner:
                default:
                    initializeMatrices(Difficulty.Beginner, 2);
                    break;
            }
        setTitle("(" + String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60) + ")");
        if (hintsCount == 0)
            ((ViewManager) buttonHints.getParent()).removeView(buttonHints);
        else
            buttonHints.setText(getResources().getString(R.string.hints_label) + " " + hintsCount);
    }

    private void initializeMatrices(Difficulty difficulty, int hintsCount) {
        MemoryMatrix = fromMatrixString(getIntent().getStringExtra(PREF_MATRIX));
        setChangeMatrix();
        AnswerMatrix = getCopyOfMemoryMatrix();
        sudokuSolver.solve(AnswerMatrix);
        this.difficulty = difficulty;
        getPreferences(MODE_PRIVATE).edit().putInt(PREF_DIFFICULTY, difficulty.ordinal()).apply();
        this.saveSeconds = 0;
        getPreferences(MODE_PRIVATE).edit().putLong(PREF_TIME, this.saveSeconds).apply();
        this.hintsCount = hintsCount;
    }

    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulties,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface,
                                                int i) {
                                new GameLoadingScreen(GameActivity.this).start(Difficulty.values()[i], true);
                            }
                        })
                .show();
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

    private byte[][] getCopyOfMemoryMatrix() {
        byte[][] result = new byte[MemoryMatrix.length][];
        for (int i = 0; i < MemoryMatrix.length; i++) {
            result[i] = Arrays.copyOf(MemoryMatrix[i], MemoryMatrix[i].length);
        }
        return result;
    }

    private void copyToMemoryMatrix(byte[][] original) {
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

    private void Hints() {
        Point points[] = new Point[BOARD_SIDE_RECT_COUNT * BOARD_SIDE_RECT_COUNT];
        int count = 0;
        for (int i = 0; i < BOARD_SIDE_RECT_COUNT; i++)
            for (int j = 0; j < BOARD_SIDE_RECT_COUNT; j++)
                if (ChangeMatrix[i][j] && (MemoryMatrix[i][j] != AnswerMatrix[i][j] || MemoryMatrix[i][j] == 0)) {
                    points[count++] = new Point(i, j);
                }
        int num = defaultRandomizer.GetInt(count);
        MemoryMatrix[points[num].x][points[num].y] = AnswerMatrix[points[num].x][points[num].y];
        ChangeMatrix[points[num].x][points[num].y] = false;
        gameBoardView.invalidate();
    }
}