package com.example.ProSudoku.activity.board;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.activity.board.solver.SolverActivity;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;

import java.util.List;

public class GameBoardView extends View {

    private static final double WIDTH_RATIO = 2;//5.56;
    private static final double Y_POS_RATIO = 2;//5; //7.81;
    private static final double Y_POS_NUMBER_CELL_RATIO = 2;

    private int gameBSpaceSmall;
    private int gameBSpaceBig;
    private int gameBChoseBorder;
    private int gameBBorder;

    private Rect[][] GameBoardRects;
    private Rect[] NumberBoardRects;

    private Rect gameBBackground;
    private Rect numberBBackground;

    private int gameBWidth;
    private int gameBRectsCount;
    private int gameBCellWidth;

    private int numberBCellWidth;

    private int gameBSmallSpaceCount;

    private int xPos;
    private int yPos;

    private int numberYPos;
    private int numberXPos;

    //boolean isScreenTouched = false;

    private Point resolution;
    private Point selectedPoint;

    private Paint p;

    private AssetManager asset;
    private Typeface gameBTypeface;
    private GameBoardColors gameBColors;

    private final IGameBoardActivity activity;
    private static List<GameBoardViewPlugin> plugins;

    //Practical matrix width (without fault)
    private int getGameBoardWidth() {
        return gameBCellWidth * gameBRectsCount + gameBSpaceBig * (gameBRectsCount - gameBSmallSpaceCount - 1) + gameBSpaceSmall * gameBSmallSpaceCount;
    }

    //NumberBoardRects width without spaces and borders
    private int getRealNumberBoardWidth() {
        return numberBCellWidth * NumberBoardRects.length + (NumberBoardRects.length - 1) * gameBSpaceSmall;
    }

    public GameBoardView(Context context) {
        super(context);
        activity = (IGameBoardActivity) context;
        initView(context, null);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (IGameBoardActivity) context;
        initView(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resolution = new Point(parentWidth, parentHeight);
        load();
        loadPlugins();
    }

    private void initView(Context context, AttributeSet attrs) {
        gameBRectsCount = activity.getMatrixRectCount();
        asset = context.getAssets();
        gameBTypeface = Typeface.createFromAsset(asset, PrefsActivity.getFonts(context));

        gameBColors = PrefsActivity.setMatrixColor(context);

        //int a = gameBBorder[Integer.parseInt(Prefs.getMatrixBorder(context))][0];
        //Log.d("VIEW", a + "");

        //Prefs sets
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //double_row = sharedPrefs.getBoolean(getResources().getString(R.string.double_row_key), getResources().getBoolean(R.bool.double_row_default));
        gameBSpaceSmall = PrefsActivity.getMatrixBorder(context, PrefsActivity.Border.Small);
        gameBBorder = gameBSpaceBig = PrefsActivity.getMatrixBorder(context, PrefsActivity.Border.Big);
        gameBChoseBorder = PrefsActivity.getMatrixBorder(context, PrefsActivity.Border.Chose);
    }

    private void loadPlugins() {
        plugins = activity.getPlugins();
        for (GameBoardViewPlugin plugin : plugins) {
            plugin.init(this);
        }
    }

    private void load() {

        GameBoardRects = new Rect[gameBRectsCount][gameBRectsCount];
        NumberBoardRects = new Rect[gameBRectsCount + 1];

        this.gameBWidth = (int) (resolution.x - (resolution.x * WIDTH_RATIO) / 100);// Matrix width with borders
        this.selectedPoint = new Point(-1, -1);
        p = new Paint();

        loadGameBoard();
        loadNumberBoard();
    }

    private void loadGameBoard() {
        xPos = (resolution.x - gameBWidth) / 2 + gameBBorder;
        yPos = (int) ((resolution.y * Y_POS_RATIO) / 100) + gameBBorder;
        gameBSmallSpaceCount = gameBRectsCount - (int) Math.sqrt(gameBRectsCount);
        gameBCellWidth = getGameBoardCellWidth(); //(gameBWidth - gameBSpaceBig * (gameBRectsCount - gameBSmallSpaceCount - 1) - gameBSpaceSmall * gameBSmallSpaceCount) / gameBRectsCount;

        for (int i = 0; i < GameBoardRects.length; i++) {
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                GameBoardRects[i][j] = new Rect(
                        xPos + gameBSpaceSmall * j + (gameBSpaceBig - gameBSpaceSmall) * (int) (j / Math.sqrt(gameBRectsCount)) + gameBCellWidth * j,
                        yPos + gameBSpaceSmall * i + (gameBSpaceBig - gameBSpaceSmall) * (int) (i / Math.sqrt(gameBRectsCount)) + gameBCellWidth * i,
                        xPos + gameBSpaceSmall * j + (gameBSpaceBig - gameBSpaceSmall) * (int) (j / Math.sqrt(gameBRectsCount)) + gameBCellWidth * j + gameBCellWidth,
                        yPos + gameBSpaceSmall * i + (gameBSpaceBig - gameBSpaceSmall) * (int) (i / Math.sqrt(gameBRectsCount)) + gameBCellWidth * i + gameBCellWidth);
                if (activity.getMemoryMatrix()[i][j] == 0)
                    activity.getChangeMatrix()[i][j] = true;
            }
        }

        gameBBackground = new Rect(xPos - gameBBorder, yPos - gameBBorder, xPos + getGameBoardWidth() + gameBBorder, yPos + getGameBoardWidth() + gameBBorder);
    }

    private void loadNumberBoard() {
        //numberYPos = getGameBoardWidth() + yPos + resolution.y * Y_POS_NUMBER_CELL_RATIO / 100;
        numberBCellWidth = getNumberBoardCellWidth();//(gameBWidth - (gameBSpaceSmall * (NumberBoardRects.length - 1))) / (NumberBoardRects.length);
        numberYPos = (int) (resolution.y - (resolution.y * Y_POS_NUMBER_CELL_RATIO) / 100 - numberBCellWidth - gameBSpaceBig);
        numberXPos = xPos + (getGameBoardWidth() - getRealNumberBoardWidth()) / 2;

        for (int i = 0; i < NumberBoardRects.length; i++)
            NumberBoardRects[i] = new Rect(numberXPos + numberBCellWidth * i + gameBSpaceSmall * i, numberYPos,
                    numberXPos + numberBCellWidth * i + gameBSpaceSmall * i + numberBCellWidth, numberYPos + numberBCellWidth);

        numberBBackground = new Rect(numberXPos - gameBBorder, numberYPos - gameBBorder, numberXPos + getRealNumberBoardWidth() + gameBBorder, numberYPos + numberBCellWidth + gameBBorder);
    }

    //Finding width without remainder
    private int getGameBoardCellWidth() {
        int width = gameBWidth;
        double num = (width - gameBSpaceBig * (gameBRectsCount - gameBSmallSpaceCount - 1) - gameBSpaceSmall * gameBSmallSpaceCount - 2 * gameBBorder) / gameBRectsCount;
        return (int) num;
    }

    //Finding width without remainder
    private int getNumberBoardCellWidth() {
        int width = getGameBoardWidth();
        double num = (width - (gameBSpaceSmall * (NumberBoardRects.length - 1))) / (NumberBoardRects.length);
        return (int) num;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        // coordinates of Touch-event
        float evX = event.getX();
        float evY = event.getY();

        //boolean flag = false;

        switch (event.getAction()) {
            // Touch has been started
            case MotionEvent.ACTION_DOWN:
                // if touch was started in square
                for (int i = 0; i < GameBoardRects.length; i++)
                    for (int j = 0; j < GameBoardRects[i].length; j++)
                        if (GameBoardRects[i][j].contains((int) evX, (int) evY) && new Point(i, j) != selectedPoint) {
                            selectedPoint = new Point(i, j);
                            //flag = true;
                        }

                for (int i = 0; i < NumberBoardRects.length; i++) {
                    if (NumberBoardRects[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getChangeMatrix()[selectedPoint.x][selectedPoint.y] &&
                            activity.getClass() != SolverActivity.class) {
                        activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] = (byte) (i);
                        //flag = true;
                    } else if (NumberBoardRects[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getClass() == SolverActivity.class) {
                        activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] = (byte) (i);
                        activity.getChangeMatrix()[selectedPoint.x][selectedPoint.y] = true;
                    }
                }
                //isScreenTouched = flag;
                break;
            //case MotionEvent.ACTION_UP:
            //if (!isScreenTouched)
            //selectedPoint = new Point(-1,-1);
        }

        for (GameBoardViewPlugin plugin : plugins) {
            plugin.onTouchEvent(event);
        }

        invalidate();
        return true;
    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor, Typeface font) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(font);
        int width = (int) (paint.measureText(text) + 0.5f); // round
        float baseline = (int) (-paint.ascent() + 0.5f); // ascent() is negative
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    /// <summary>
    /// Change color of not correct rectangle
    /// </summary>
    /// <returns>True if number is unique by the sudoku rules</returns>
    public boolean isUniqueNumber(int x, int y) {
        // Check the same numbers in the vertical direction
        for (int a = 0; a < gameBRectsCount; a++)
            if (activity.getMemoryMatrix()[a][y] == activity.getMemoryMatrix()[x][y] && a != x)
                return false;

        // Check the same numbers in the horizontal direction
        for (int b = 0; b < gameBRectsCount; b++)
            if (activity.getMemoryMatrix()[x][b] == activity.getMemoryMatrix()[x][y] && b != y)
                return false;

        int n = (int) Math.sqrt(gameBRectsCount);
        int sectorX = x / n;
        int sectorY = y / n;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                if (i + sectorX * n != x && j + sectorY * n != y)
                    if (activity.getMemoryMatrix()[i + sectorX * n][j + sectorY * n] == activity.getMemoryMatrix()[x][y])
                        return false;
            }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawRGB(173, 255, 47);
        //canvas.drawARGB(50, 20, 204, 255);

        p.setColor(gameBColors.getBackColor());

        canvas.drawRect(gameBBackground, p);
        //if(selectedPoint.x >= 0 && selectedPoint.y >= 0)
        //p.setColor(Color.YELLOW);
        canvas.drawRect(numberBBackground, p);

        //1st version of view
        /*for (int i = 0; i < GameBoardRects.length; i++)
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                p.setColor(cellColor);
                canvas.drawRect(new Rect(GameBoardRects[i][j].left + gameBSpaceSmall, GameBoardRects[i][j].top + gameBSpaceSmall, GameBoardRects[i][j].right - gameBSpaceSmall, GameBoardRects[i][j].bottom - gameBSpaceSmall), p);
                if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
                    p.setColor(sameNumbersColor);
                    canvas.drawRect(GameBoardRects[selectedPoint.x][selectedPoint.y], p);
                }
                //else
                //{
                //p.setColor(Color.WHITE);
                //canvas.drawRect(GameBoardRects[i][j], p);
                //}
            }*/

        //2nd version of view
        for (int i = 0; i < GameBoardRects.length; i++)
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                //1st v.
                //if(activity.getMemoryMatrix()[i][j] != 0 && !isUniqueNumber(new Point(i,j)))
                //p.setColor(errorColor);
                //else
                p.setColor(gameBColors.getCellColor());

                canvas.drawRect(GameBoardRects[i][j], p);
                if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
                    //What kind of cell was chosen
                    if (activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] == 0)
                        p.setColor(gameBColors.getSameEmptyNumberCellColor());
                    else
                        p.setColor(gameBColors.getChoseEmptyCellColor());

                    if (activity.getMemoryMatrix()[i][j] == activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] && activity.getMemoryMatrix()[i][j] != 0) {
                        if (i == selectedPoint.x && j == selectedPoint.y)
                            p.setColor(gameBColors.getChoseCellColor());
                        canvas.drawRect(GameBoardRects[i][j], p);
                        p.setColor(gameBColors.getCellColor());

                        //1st v.
                        //if(!isUniqueNumber(new Point(i,j)))
                        //p.setColor(errorColor);

                        canvas.drawRect(new Rect(GameBoardRects[i][j].left + gameBChoseBorder,
                                GameBoardRects[i][j].top + gameBChoseBorder,
                                GameBoardRects[i][j].right - gameBChoseBorder,
                                GameBoardRects[i][j].bottom - gameBChoseBorder), p);
                    }

                    if (activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] == 0) {
                        canvas.drawRect(GameBoardRects[selectedPoint.x][selectedPoint.y], p);
                        p.setColor(gameBColors.getCellColor());
                        canvas.drawRect(new Rect(GameBoardRects[selectedPoint.x][selectedPoint.y].left + gameBChoseBorder,
                                GameBoardRects[selectedPoint.x][selectedPoint.y].top + gameBChoseBorder,
                                GameBoardRects[selectedPoint.x][selectedPoint.y].right - gameBChoseBorder,
                                GameBoardRects[selectedPoint.x][selectedPoint.y].bottom - gameBChoseBorder), p);
                    }
                }
            }

        p.setColor(gameBColors.getCellColor());
        for (Rect aNumberMatrix : NumberBoardRects) {
            //1st version of view
            //canvas.drawRect(new Rect(NumberBoardRects[i].left + gameBSpaceSmall, NumberBoardRects[i].top + gameBSpaceSmall, NumberBoardRects[i].right - gameBSpaceSmall, NumberBoardRects[i].bottom - gameBSpaceSmall), p);

            //2nd version of view
            canvas.drawRect(aNumberMatrix, p);
        }

        for (int i = 0; i < GameBoardRects.length; i++)
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                if (activity.getMemoryMatrix()[i][j] > 0) {

                    //2nd v.
                    //Check errors
                    if (activity.getChangeMatrix()[i][j] && !isUniqueNumber(i, j)) {
                        //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                        Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getErrorColor(), gameBTypeface);
                        canvas.drawBitmap(bit, GameBoardRects[i][j].centerX() - bit.getWidth() / 2, GameBoardRects[i][j].centerY() - bit.getHeight() / 2, p);
                    } else {
                        //Check if numbers are changeable
                        //Check the activity class
                        if (activity.getClass() != SolverActivity.class)
                            if (activity.getChangeMatrix()[i][j]) {
                                //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getChangeableTextColor(), gameBTypeface);
                                canvas.drawBitmap(bit, GameBoardRects[i][j].centerX() - bit.getWidth() / 2, GameBoardRects[i][j].centerY() - bit.getHeight() / 2, p);
                            } else {
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getIdleTextColor(), gameBTypeface);
                                canvas.drawBitmap(bit, GameBoardRects[i][j].centerX() - bit.getWidth() / 2, GameBoardRects[i][j].centerY() - bit.getHeight() / 2, p);
                            }
                        else {
                            if (activity.getChangeMatrix()[i][j]) {
                                //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getChangeableTextColor(), gameBTypeface);
                                canvas.drawBitmap(bit, GameBoardRects[i][j].centerX() - bit.getWidth() / 2, GameBoardRects[i][j].centerY() - bit.getHeight() / 2, p);
                            } else {
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getSolveColor(), gameBTypeface);
                                canvas.drawBitmap(bit, GameBoardRects[i][j].centerX() - bit.getWidth() / 2, GameBoardRects[i][j].centerY() - bit.getHeight() / 2, p);
                            }
                        }
                    }
                }
            }

        for (int i = 1; i < NumberBoardRects.length; i++) {
            Bitmap bit = textAsBitmap(String.valueOf(i), numberBCellWidth, gameBColors.getIdleTextColor(), gameBTypeface);
            canvas.drawBitmap(bit, NumberBoardRects[i].centerX() - bit.getWidth() / 2, NumberBoardRects[i].centerY() - bit.getHeight() / 2, p);
        }

        //Bitmap bit = textAsBitmap(currentTime, numberBCellWidth, Color.GREEN, gameBTypeface);
        //canvas.drawBitmap(bit, 30, 0, p);
        //bit = textAsBitmap(String.valueOf(numberXPos), numberBCellWidth, Color.BLACK, gameBTypeface);
        //canvas.drawBitmap(bit, 200, 0, p);
        //bit = textAsBitmap(String.valueOf(getTop()), numberBCellWidth, Color.BLACK, gameBTypeface);
        //canvas.drawBitmap(bit, 400, 0, p);

        for (GameBoardViewPlugin plugin : plugins) {
            plugin.onDraw(canvas);
        }
    }

    public Rect[] getNumberBoardRects() {
        return NumberBoardRects;
    }

    public Typeface getGameBoardTypeface() {
        return gameBTypeface;
    }

    public GameBoardColors getGameBoardColors() {
        return gameBColors;
    }


    public IGameBoardActivity getActivity() {
        return activity;
    }

}

