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

import java.util.ArrayList;
import java.util.List;

public class GameBoardView extends View {

    private static final double WIDTH_RATIO = 2;//5.56;
    private static final double Y_POS_RATIO = 2;//5; //7.81;
    private static final double Y_POS_NUMBER_CELL_RATIO = 2;

    private int gameBSpaceSmall;
    private int gameBSpaceBig;

    private int gameBChoseBorder;
    private int gameBBorder;

    private Bitmap[][] GameBoardNumbers;
    private Rect[][] GameBoardRects;

    private Bitmap[] NumberBoardNumbers;
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
    private List<GameBoardViewPlugin> plugins;

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
        List<GameBoardViewPlugin> activePlugins = new ArrayList<GameBoardViewPlugin>();
        if (activity instanceof PluginHandlerActivity) {
            PluginHandlerActivity pluginActivity = (PluginHandlerActivity) activity;
            if (pluginActivity.getPlugins() != null) {
                for (GameBoardViewPlugin plugin : pluginActivity.getPlugins()) {
                    if (plugin.isActive()) {
                        activePlugins.add(plugin);
                        plugin.init(this);
                    }
                }
            }
        }
        plugins = activePlugins;
    }

    private void load() {

        GameBoardRects = new Rect[gameBRectsCount][gameBRectsCount];
        GameBoardNumbers = new Bitmap[gameBRectsCount][gameBRectsCount];
        NumberBoardRects = new Rect[gameBRectsCount + 1];
        NumberBoardNumbers = new Bitmap[gameBRectsCount + 1];

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
    public int getGameBoardCellWidth() {
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

        switch (event.getAction()) {
            // Touch has been started
            case MotionEvent.ACTION_DOWN:
                // if touch was started in square
                for (int i = 0; i < GameBoardRects.length; i++)
                    for (int j = 0; j < GameBoardRects[i].length; j++)
                        if (GameBoardRects[i][j].contains((int) evX, (int) evY) && new Point(i, j) != selectedPoint) {
                            selectedPoint.set(i, j);
                        }

                for (int i = 0; i < NumberBoardRects.length; i++) {
                    if (NumberBoardRects[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getChangeMatrix()[selectedPoint.x][selectedPoint.y] &&
                            activity.getClass() != SolverActivity.class) {
                        activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] = (byte) (i);
                    } else if (NumberBoardRects[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getClass() == SolverActivity.class) {
                        activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] = (byte) (i);
                        activity.getChangeMatrix()[selectedPoint.x][selectedPoint.y] = true;
                    }
                }
                break;
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
        p.setColor(gameBColors.getBackColor());

        canvas.drawRect(gameBBackground, p);
        canvas.drawRect(numberBBackground, p);

        for (int i = 0; i < GameBoardNumbers.length; i++) {
            for (int j = 0; j < GameBoardNumbers[i].length; j++) {
                GameBoardNumbers[i][j] = null;
            }
        }

        for (int i = 1; i < NumberBoardRects.length; i++) {
            NumberBoardNumbers[i] = null;
        }

        for (int i = 0; i < GameBoardRects.length; i++) {
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                p.setColor(gameBColors.getCellColor());
                canvas.drawRect(GameBoardRects[i][j], p);
            }
        }

        if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
            //What kind of cell was chosen
            if (activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] == 0)
                p.setColor(gameBColors.getSameEmptyNumberCellColor());
            else
                p.setColor(gameBColors.getChoseCellColor());

            canvas.drawRect(GameBoardRects[selectedPoint.x][selectedPoint.y], p);
            p.setColor(gameBColors.getCellColor());
            canvas.drawRect(new Rect(GameBoardRects[selectedPoint.x][selectedPoint.y].left + gameBChoseBorder,
                    GameBoardRects[selectedPoint.x][selectedPoint.y].top + gameBChoseBorder,
                    GameBoardRects[selectedPoint.x][selectedPoint.y].right - gameBChoseBorder,
                    GameBoardRects[selectedPoint.x][selectedPoint.y].bottom - gameBChoseBorder), p);
        }

        p.setColor(gameBColors.getCellColor());
        for (Rect numberBRect : NumberBoardRects) {
            //1st version of view
            //canvas.drawRect(new Rect(NumberBoardRects[i].left + gameBSpaceSmall, NumberBoardRects[i].top + gameBSpaceSmall, NumberBoardRects[i].right - gameBSpaceSmall, NumberBoardRects[i].bottom - gameBSpaceSmall), p);

            //2nd version of view
            canvas.drawRect(numberBRect, p);
        }

        for (int i = 0; i < GameBoardRects.length; i++) {
            for (int j = 0; j < GameBoardRects[i].length; j++) {
                if (activity.getMemoryMatrix()[i][j] > 0) {
                    if (activity.getChangeMatrix()[i][j]) {
                        //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                        GameBoardNumbers[i][j] = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getSolveColor(), gameBTypeface);
                    } else {
                        GameBoardNumbers[i][j] = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), gameBCellWidth, gameBColors.getIdleTextColor(), gameBTypeface);
                    }
                }
            }
        }

        for (int i = 1; i < NumberBoardRects.length; i++) {
            NumberBoardNumbers[i] = textAsBitmap(String.valueOf(i), numberBCellWidth, gameBColors.getIdleTextColor(), gameBTypeface);
        }

        for (GameBoardViewPlugin plugin : plugins) {
            plugin.onDraw(canvas);
        }

        for (int i = 0; i < GameBoardNumbers.length; i++) {
            for (int j = 0; j < GameBoardNumbers[i].length; j++) {
                if(GameBoardNumbers[i][j] != null) {
                    canvas.drawBitmap(GameBoardNumbers[i][j], GameBoardRects[i][j].centerX() - GameBoardNumbers[i][j].getWidth() / 2, GameBoardRects[i][j].centerY() - GameBoardNumbers[i][j].getHeight() / 2, p);
                }
            }
        }

        for (int i = 1; i < NumberBoardRects.length; i++) {
            if(NumberBoardNumbers[i] != null) {
                canvas.drawBitmap(NumberBoardNumbers[i], NumberBoardRects[i].centerX() - NumberBoardNumbers[i].getWidth() / 2, NumberBoardRects[i].centerY() - NumberBoardNumbers[i].getHeight() / 2, p);
            }
        }

    }

    public Rect[] getNumberBoardRects() {
        return NumberBoardRects;
    }

    public Rect[][] getGameBoardRects() {
        return GameBoardRects;
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

    public Point getSelectedPoint() {
        return selectedPoint;
    }

    public Bitmap[][] getGameBoardNumbers() {
        return GameBoardNumbers;
    }

    public Bitmap[] getNumberBoardNumbers() {
        return NumberBoardNumbers;
    }

    public int getGameBChoseBorder() {
        return gameBChoseBorder;
    }
}

