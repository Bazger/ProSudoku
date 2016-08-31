package com.example.ProSudoku;

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.os.*;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.*;

import java.util.Arrays;

public class Game extends Activity implements IMatrix {

	private byte[][] MemoryMatrix;
	private byte[][] AnswerMatrix;
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
	final static String PREF_TIME= "seconds";
	final static String PREF_ANSWER_MATRIX= "answer_matrix";
	final static String PREF_HINTS= "hints";
	final static String PREF_DIFFICULTY= "difficulty";
	final static String PREF_TIMER_STOP = "timer_stop";


	final String testMatrix = "123456789456789123789123456231674895875912364694538217317265948542897631968341570";

	IRandomizer Randomizer = new DefaultRandomizer();

	private MatrixView matrixView;
	private Button buttonHints;
	private Dialog finishDialog;
	private Dialog recordsDialog;
	private DB db;

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
	private long milliseconds;
	private long saveSeconds;
	private long seconds;
	private boolean isTimerStoped;
	private int hintsCount;
	private int difficulty;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Prefs.setSettings(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gametable);
		Prefs.setBackground(this);

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

		matrixView = (MatrixView) findViewById(R.id.matrix_view);

		/**
		 * Database creating
		 */
		db = new DB(this);
		db.open();

		buttonHints.setOnClickListener(new View.OnClickListener() {
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

		/**
		 * Scores Dialog creating
		 */
		recordsDialog = new Dialog(this);
		recordsDialog.setContentView(R.layout.scores_dialog);
		recordsDialog.setTitle(R.string.new_record_label);
		recordsDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

		final EditText dialogName = (EditText) recordsDialog.findViewById(R.id.dialogEditTextName);
		final TextView dialogTextView =(TextView) recordsDialog.findViewById(R.id.dialogTextView);
		Button dialogButtonSave = (Button) recordsDialog.findViewById(R.id.dialogButtonSave);
		// if button is clicked, save the record and go to finish dialog

		dialogButtonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialogName.getText().length() != 0) {
					db.addRec(dialogName.getText().toString(), Long.parseLong(getPreferences(MODE_PRIVATE).getString(PREF_TIME, null)), DB.Dif.values()[difficulty]);
					recordsDialog.dismiss();
					finishDialog.show();
				} else
					dialogTextView.setText("You need to fill the field below");
			}

		});


		Button dialogButtonContinue = (Button) recordsDialog.findViewById(R.id.dialogButtonContinue);
		// if button is clicked, close this dialog and open finish dialog
		dialogButtonContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				recordsDialog.dismiss();
				finishDialog.show();
			}
		});


		/**
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

	private final Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (mStarted && !IsSudokuFeasible() || GetNumberSpots() != matrixRectCount * matrixRectCount) {
				seconds = saveSeconds + (System.currentTimeMillis() - milliseconds) / 1000;
				setTitle(String.format("%02d:%02d", seconds / 60, seconds % 60));
				mHandler.postDelayed(mRunnable, 1000L);
			}
			else
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
		if(GetNumberSpots() == matrixRectCount * matrixRectCount && !finishDialog.isShowing() && IsSudokuFeasible()) {
			if(!isTimerStoped) {
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
			if(c.getCount() != 0) {
				if (c.getLong(c.getColumnIndex(DB.COLUMN_TIME)) > Long.parseLong(getPreferences(MODE_PRIVATE).getString(PREF_TIME, null)) || c.getCount() < DB.tableCount)
					recordsDialog.show();
				else {
					finishDialog.show();
				}
				return true;
			}
			else
				recordsDialog.show();
		}
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!IsSudokuFeasible() || GetNumberSpots() != matrixRectCount * matrixRectCount) {
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
		if(!isTimerStoped && seconds != 0) {
			getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Long.toString(seconds)).commit();
			setTitle("(" + String.format("%02d:%02d", seconds / 60, seconds % 60) + ")");
		}
		if(!isTimerStoped) {
			savePreferences();
		}
	}

	private void savePreferences()
	{
		getPreferences(MODE_PRIVATE).edit().putString(PREF_MATRIX,
				                                             toMatrixString(MemoryMatrix)).commit();
		getPreferences(MODE_PRIVATE).edit().putString(PREF_ANSWER_MATRIX,
				                                             toMatrixString(AnswerMatrix)).commit();
		getPreferences(MODE_PRIVATE).edit().putString(PREF_CHANGE_MATRIX,
				                                             toChangeMatrixString(ChangeMatrix)).commit();
		getPreferences(MODE_PRIVATE).edit().putString(PREF_HINTS, Integer.toString(hintsCount)).commit();
		getPreferences(MODE_PRIVATE).edit().putString(PREF_TIMER_STOP, Boolean.toString(isTimerStoped)).commit();
	}

	/** Given a difficulty level, come up with a new puzzle */
	private void getMatrix(int diff) {
		// TODO: Continue last game
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
				Generate(23 + Randomizer.GetInt(2));
				AnswerMatrix = getData();
				Solve();
				difficulty = DIFFICULTY_HARD;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_DIFFICULTY, Integer.toString(difficulty)).commit();
				saveSeconds = 0;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Integer.toString(difficulty)).commit();
				hintsCount = 0;
				break;
			case DIFFICULTY_MEDIUM:
				Generate(25 + Randomizer.GetInt(6));
				AnswerMatrix = getData();
				Solve();
				difficulty = DIFFICULTY_MEDIUM;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_DIFFICULTY, Integer.toString(difficulty)).commit();
				saveSeconds = 0;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Integer.toString(difficulty)).commit();
				hintsCount = 1;
				break;
			case DIFFICULTY_EASY:
				//MemoryMatrix =  fromMatrixString(testMatrix);
				Generate(30 + Randomizer.GetInt(6));
				AnswerMatrix = getData();
				Solve();
				difficulty = DIFFICULTY_EASY;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_DIFFICULTY, Integer.toString(difficulty)).commit();
				saveSeconds = 0;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Integer.toString(difficulty)).commit();
				hintsCount = 2;
				break;
			case DIFFICULTY_BEGINNER:
			default:
				Generate(35 + Randomizer.GetInt(6));
				AnswerMatrix = getData();
				Solve();
				difficulty = DIFFICULTY_BEGINNER;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_DIFFICULTY, Integer.toString(difficulty)).commit();
				saveSeconds = 0;
				getPreferences(MODE_PRIVATE).edit().putString(PREF_TIME, Integer.toString(difficulty)).commit();
				hintsCount = 3;
				break;
		}
		setTitle("(" + String.format("%02d:%02d", saveSeconds / 60, saveSeconds % 60) + ")");
		if(hintsCount == 0)
			((ViewManager) buttonHints.getParent()).removeView(buttonHints);
		else
			buttonHints.setText(getResources().getString(R.string.hints_label) + " " + hintsCount);
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
		Intent intent = new Intent(Game.this, Game.class);
		intent.putExtra(Game.KEY_DIFFICULTY, i);
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
				matrixView.Update();
				return true;
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


	/**
	 * Sudoku generation
	 */


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
				matrix[i][j] = (string.charAt(j + i * 9) - '0') == 1;

		return matrix;
	}

	public void Hints()
	{
		Point points[] = new Point[matrixRectCount * matrixRectCount];
		int count = 0;
		for (int i = 0; i < matrixRectCount; i++)
			for (int j = 0; j < matrixRectCount; j++)
				if(ChangeMatrix[i][j] &&(MemoryMatrix[i][j] != AnswerMatrix[i][j] || MemoryMatrix[i][j] == 0))
				{
					points[count++] = new Point(i,j);
				}
		int num = Randomizer.GetInt(count);
		MemoryMatrix[points[num].x][points[num].y] = AnswerMatrix[points[num].x][points[num].y];
		ChangeMatrix[points[num].x][points[num].y] = false;
		matrixView.Update();
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

		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				if (AnswerMatrix[x][y] == 0)
				{
					// Set M of possible solutions
					byte[] M = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

					// Remove used numbers in the vertical direction
					for (int a = 0; a < 9; a++)
						M[AnswerMatrix[a][y]] = 0;

					// Remove used numbers in the horizontal direction
					for (int b = 0; b < 9; b++)
						M[AnswerMatrix[x][b]] = 0;

					// Remove used numbers in the sub square. BUG:
					//int squareIndex = m_subSquare[y, x];
					//for (int c = 0; c < 9; c++)
					//{
					//    EntryPoint p = m_subIndex[squareIndex, c];
					//    M[m_sudoku[p.x, p.y]] = 0;
					//}

					int n = (int) Math.sqrt(AnswerMatrix.length);
					int sectorX = x / n;
					int sectorY = y / n;
					for (int a = 0; a < n; a++) {
						for (int b = 0; b < n; b++) {
							M[AnswerMatrix[a + sectorX * n][b + sectorY * n]] = 0;
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
		for (int i = 1; i < 10; i++)
		{
			if (Mp[i] != 0)
			{
				AnswerMatrix[xp][yp] = Mp[i];
				if (Solve())
					return true;
			}
		}

		// Restore to original state.
		AnswerMatrix[xp][yp] = 0;
		return false;
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

	/*@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		switch (id)
		{
			case RECORDSDIALOG:
				adb.setTitle("Custom dialog");
				// создаем view из dialog.xml
				dialogView = getLayoutInflater()
						             .inflate(R.layout.records_dialog, null);
				// устанавливаем ее, как содержимое тела диалога
				adb.setView(dialogView);
				// находим TexView для отображения кол-ва
				//tvCount = (TextView) view.findViewById(R.id.tvCount);
				return adb.create();
			default:
				adb.setTitle("Custom dialog");
				// создаем view из dialog.xml
				dialogView = getLayoutInflater()
						             .inflate(R.layout.finish_dialog, null);
				// устанавливаем ее, как содержимое тела диалога
				adb.setView(dialogView);
				// находим TexView для отображения кол-ва
				//tvCount = (TextView) view.findViewById(R.id.tvCount);
				return adb.create();
		}
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == RECORDSDIALOG) {
			TextView tvTime = (TextView) dialog.getWindow().findViewById(R.id.tvTime);
			dialog.setTitle(R.string.new_record_label);

			final EditText dialogName = (EditText) dialog.findViewById(R.id.dialogEditTextName);
			final TextView dialogTextView =(TextView) dialog.findViewById(R.id.dialogTextView);
			Button dialogButtonSave = (Button) dialog.findViewById(R.id.dialogButtonSave);
			// if button is clicked, save the record and go to finish dialog

			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

			dialogButtonSave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (dialogName.getText().length() != 0) {
						db.addRec(dialogName.getText().toString(), seconds, DB.Dif.values()[difficulty]);
						dialog.dismiss();
						finishDialog.show();
					} else
						dialogTextView.setText("You need to fill the field below");
				}

			});


			Button dialogButtonContinue = (Button) dialog.findViewById(R.id.dialogButtonContinue);
			// if button is clicked, close this dialog and open finish dialog
			dialogButtonContinue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					finishDialog.show();
				}
			});
		}
	}*/
}