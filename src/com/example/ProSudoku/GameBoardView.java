package com.example.ProSudoku;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.example.ProSudoku.plugin.IGameBoardViewPlugin;

import java.util.List;

class GameBoardView extends View {

    final double widthRatio = 2;//5.56;
    final double yPosRatio = 2;//5;//7.81;
    final double yPosNumberCellRatio = 2;

    private int matrixSpaceSmall;
    private int matrixSpaceBig;
    private int matrixChoseBorder;
    private int matrixBorder;


    private Rect[][] RectMatrix;
    private Rect[] NumberMatrix;

    private Rect gameBoardBackground;
    private Rect numbersBoardBackground;

    private int matrixWidth;
    private int matrixRectCount;
    private int matrixCellWidth;

    private int numberMatrixCellWidth;

    private int matrixSmallSpaceCount;

    private int xPos;
    private int yPos;

    private int numberYPos;
    private int numberXPos;

    //boolean isScreenTouched = false;

    private Point resolution;
    private Point selectedPoint;

    private Paint p;

    private AssetManager asset;
    private Typeface myTypeface;
	private MatrixColors matrixColors;

    private final IGameBoardView activity;
    private List<IGameBoardViewPlugin> plugins;

    //Practical matrix width (without fault)
    public int getMatrixWidth() {
        return matrixCellWidth * matrixRectCount + matrixSpaceBig * (matrixRectCount - matrixSmallSpaceCount - 1) + matrixSpaceSmall * matrixSmallSpaceCount;
    }

    //NumberMatrix width without spaces and borders
    public int getRealNumberMatrixWidth() {
        return numberMatrixCellWidth * NumberMatrix.length + (NumberMatrix.length - 1) * matrixSpaceSmall;
    }

    public GameBoardView(Context context) {
        super(context);
        activity = (IGameBoardView) context;
        initViews(context, null);
    }

    public GameBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (IGameBoardView) context;
        initViews(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        resolution = new Point(parentWidth, parentHeight);
        onLoad();
    }

    private void initViews(Context context, AttributeSet attrs) {
        matrixRectCount = activity.getMatrixRectCount();
        plugins = activity.getPlugins();
        asset = context.getAssets();
	    myTypeface = Typeface.createFromAsset(asset, Prefs.getFonts(context));

		matrixColors = Prefs.setMatrixColor(context);

        //int a = matrixBorder[Integer.parseInt(Prefs.getMatrixBorder(context))][0];
        //Log.d("VIEW", a + "");

        //Prefs sets
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //double_row = sharedPrefs.getBoolean(getResources().getString(R.string.double_row_key), getResources().getBoolean(R.bool.double_row_default));
	    matrixSpaceSmall = Prefs.getMatrixBorder(context, Prefs.Border.Small);
        matrixBorder = matrixSpaceBig = Prefs.getMatrixBorder(context, Prefs.Border.Big);
        matrixChoseBorder = Prefs.getMatrixBorder(context, Prefs.Border.Chose);
    }

    private void onLoad() {

        RectMatrix = new Rect[matrixRectCount][matrixRectCount];
        NumberMatrix = new Rect[matrixRectCount + 1];

        this.matrixWidth = (int) (resolution.x - (resolution.x * widthRatio) / 100);// Matrix width with borders
        this.selectedPoint = new Point(-1, -1);
        p = new Paint();

        loadMatrix();
    }

    private void loadMatrix() {
        xPos = (resolution.x - matrixWidth) / 2 + matrixBorder;
        yPos = (int) ((resolution.y * yPosRatio) / 100) + matrixBorder;
        matrixSmallSpaceCount = matrixRectCount - (int) Math.sqrt(matrixRectCount);
        matrixCellWidth = getMatrixCellWidth(); //(matrixWidth - matrixSpaceBig * (matrixRectCount - matrixSmallSpaceCount - 1) - matrixSpaceSmall * matrixSmallSpaceCount) / matrixRectCount;

        for (int i = 0; i < RectMatrix.length; i++)
            for (int j = 0; j < RectMatrix[i].length; j++) {
                RectMatrix[i][j] = new Rect(xPos + matrixSpaceSmall * j + (matrixSpaceBig - matrixSpaceSmall) * (int) (j / Math.sqrt(matrixRectCount)) + matrixCellWidth * j,
                        yPos + matrixSpaceSmall * i + (matrixSpaceBig - matrixSpaceSmall) * (int) (i / Math.sqrt(matrixRectCount)) + matrixCellWidth * i,
                        xPos + matrixSpaceSmall * j + (matrixSpaceBig - matrixSpaceSmall) * (int) (j / Math.sqrt(matrixRectCount)) + matrixCellWidth * j + matrixCellWidth,
                        yPos + matrixSpaceSmall * i + (matrixSpaceBig - matrixSpaceSmall) * (int) (i / Math.sqrt(matrixRectCount)) + matrixCellWidth * i + matrixCellWidth);
                if (activity.getMemoryMatrix()[i][j] == 0)
                    activity.getChangeMatrix()[i][j] = true;
            }

        //numberYPos = getMatrixWidth() + yPos + resolution.y * yPosNumberCellRatio / 100;
        numberMatrixCellWidth = getNumberMatrixCellWidth();//(matrixWidth - (matrixSpaceSmall * (NumberMatrix.length - 1))) / (NumberMatrix.length);
        numberYPos = (int) (resolution.y - (resolution.y * yPosNumberCellRatio) / 100 - numberMatrixCellWidth - matrixSpaceBig);
        numberXPos = xPos + (getMatrixWidth() - getRealNumberMatrixWidth()) / 2;

        for (int i = 0; i < NumberMatrix.length; i++)
            NumberMatrix[i] = new Rect(numberXPos + numberMatrixCellWidth * i + matrixSpaceSmall * i, numberYPos,
                    numberXPos + numberMatrixCellWidth * i + matrixSpaceSmall * i + numberMatrixCellWidth, numberYPos + numberMatrixCellWidth);

        gameBoardBackground = new Rect(xPos - matrixBorder, yPos - matrixBorder, xPos + getMatrixWidth() + matrixBorder, yPos + getMatrixWidth() + matrixBorder);
        numbersBoardBackground = new Rect(numberXPos - matrixBorder, numberYPos - matrixBorder, numberXPos + getRealNumberMatrixWidth() + matrixBorder, numberYPos + numberMatrixCellWidth + matrixBorder);
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

    //Finding width without remainder
    public int getMatrixCellWidth() {
        int width = matrixWidth;
        double num = (width - matrixSpaceBig * (matrixRectCount - matrixSmallSpaceCount - 1) - matrixSpaceSmall * matrixSmallSpaceCount - 2 * matrixBorder) / matrixRectCount;
        return (int) num;
    }

    //Finding width without remainder
    public int  getNumberMatrixCellWidth() {
        int width = getMatrixWidth();
        double num = (width - (matrixSpaceSmall * (NumberMatrix.length - 1))) / (NumberMatrix.length);
        return (int) num;

    }

/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Random rand = new Random();
        canvas.drawRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
        p.setColor(Color.WHITE);
        p.setTextSize(50);
        //Typeface myTypeface = Typeface.createFromAsset(asset, "Mandarin.ttf");
        canvas.drawText(String.valueOf(getBottom()), 200, 200, p);
        canvas.drawText(String.valueOf(getRight()), 200, 300, p);
        canvas.drawText(String.valueOf(resolution.y), 400, 200, p);
        canvas.drawText(String.valueOf(times), 400, 300, p);
        //Bitmap bit = textAsBitmap(String.valueOf(matrixRectCount), numberMatrixCellWidth, Color.BLACK, myTypeface);
        //canvas.drawBitmap(bit, 0, 0, p);
    }*/

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
                for (int i = 0; i < RectMatrix.length; i++)
                    for (int j = 0; j < RectMatrix[i].length; j++)
                        if (RectMatrix[i][j].contains((int) evX, (int) evY) && new Point(i, j) != selectedPoint) {
                            selectedPoint = new Point(i, j);
                            //flag = true;
                        }

                for (int i = 0; i < NumberMatrix.length; i++) {
                    if (NumberMatrix[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getChangeMatrix()[selectedPoint.x][selectedPoint.y] &&
                            activity.getClass() != Solver.class) {
                        activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] = (byte) (i);
                        //flag = true;
                    }
                    else if (NumberMatrix[i].contains((int) evX, (int) evY) && selectedPoint.x >= 0 && selectedPoint.y >= 0 && activity.getClass() == Solver.class)
                    {
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

        invalidate();
        return true;
    }

    public void Update() {
        invalidate();
    }

    /// <summary>
    /// Change color of not correct rectangle
    /// </summary>
    /// <returns>True if feasible</returns>
    public boolean FeasibleNumbers(Point point) {
        // Check the same numbers in the vertical direction
        for (int a = 0; a <matrixRectCount; a++)
            if (activity.getMemoryMatrix()[a][point.y] == activity.getMemoryMatrix()[point.x][point.y] && a != point.x)
                return true;

        // Check the same numbers in the horizontal direction
        for (int b = 0; b < matrixRectCount; b++)
            if (activity.getMemoryMatrix()[point.x][b] == activity.getMemoryMatrix()[point.x][point.y] && b != point.y)
                return true;

        int n = (int) Math.sqrt(matrixRectCount);
        int sectorX = point.x / n;
        int sectorY = point.y / n;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) {
                if (i + sectorX * n != point.x && j + sectorY * n != point.y)
                    if (activity.getMemoryMatrix()[i + sectorX * n][j + sectorY * n] == activity.getMemoryMatrix()[point.x][point.y])
                        return true;
            }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawRGB(173, 255, 47);
        //canvas.drawARGB(50, 20, 204, 255);

        p.setColor(matrixColors.getBackColor());

        canvas.drawRect(gameBoardBackground, p);
        //if(selectedPoint.x >= 0 && selectedPoint.y >= 0)
        //p.setColor(Color.YELLOW);
        canvas.drawRect(numbersBoardBackground, p);

        //1st version of view
        /*for (int i = 0; i < RectMatrix.length; i++)
            for (int j = 0; j < RectMatrix[i].length; j++) {
                p.setColor(cellColor);
                canvas.drawRect(new Rect(RectMatrix[i][j].left + matrixSpaceSmall, RectMatrix[i][j].top + matrixSpaceSmall, RectMatrix[i][j].right - matrixSpaceSmall, RectMatrix[i][j].bottom - matrixSpaceSmall), p);
                if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
                    p.setColor(sameNumbersColor);
                    canvas.drawRect(RectMatrix[selectedPoint.x][selectedPoint.y], p);
                }
                //else
                //{
                //p.setColor(Color.WHITE);
                //canvas.drawRect(RectMatrix[i][j], p);
                //}
            }*/

        //2nd version of view
        for (int i = 0; i < RectMatrix.length; i++)
            for (int j = 0; j < RectMatrix[i].length; j++)
            {
                //1st v.
                //if(activity.getMemoryMatrix()[i][j] != 0 && FeasibleNumbers(new Point(i,j)))
                //p.setColor(errorColor);
                //else
                p.setColor(matrixColors.getCellColor());

                canvas.drawRect(RectMatrix[i][j], p);
                if (selectedPoint.x >= 0 && selectedPoint.y >= 0) {
                    //What kind of cell was chosen
                    if (activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] == 0)
                        p.setColor(matrixColors.getSameEmptyNumberCellColor());
                    else
                        p.setColor(matrixColors.getChoseEmptyCellColor());

                    if(activity.getMemoryMatrix()[i][j] == activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] && activity.getMemoryMatrix()[i][j] != 0) {
                        if(i == selectedPoint.x && j == selectedPoint.y)
                            p.setColor(matrixColors.getChoseCellColor());
                        canvas.drawRect(RectMatrix[i][j], p);
                        p.setColor(matrixColors.getCellColor());

                        //1st v.
                        //if(FeasibleNumbers(new Point(i,j)))
                        //p.setColor(errorColor);

                        canvas.drawRect(new Rect(RectMatrix[i][j].left + matrixChoseBorder,
                                RectMatrix[i][j].top + matrixChoseBorder,
                                RectMatrix[i][j].right - matrixChoseBorder,
                                RectMatrix[i][j].bottom - matrixChoseBorder), p);
                    }

                    if(activity.getMemoryMatrix()[selectedPoint.x][selectedPoint.y] == 0)
                    {
                        canvas.drawRect(RectMatrix[selectedPoint.x][selectedPoint.y], p);
                        p.setColor(matrixColors.getCellColor());
                        canvas.drawRect(new Rect(RectMatrix[selectedPoint.x][selectedPoint.y].left + matrixChoseBorder,
                                RectMatrix[selectedPoint.x][selectedPoint.y].top + matrixChoseBorder,
                                RectMatrix[selectedPoint.x][selectedPoint.y].right - matrixChoseBorder,
                                RectMatrix[selectedPoint.x][selectedPoint.y].bottom - matrixChoseBorder), p);
                    }
                }
            }

        p.setColor(matrixColors.getCellColor());
        for (Rect aNumberMatrix : NumberMatrix) {
            //1st version of view
            //canvas.drawRect(new Rect(NumberMatrix[i].left + matrixSpaceSmall, NumberMatrix[i].top + matrixSpaceSmall, NumberMatrix[i].right - matrixSpaceSmall, NumberMatrix[i].bottom - matrixSpaceSmall), p);

            //2nd version of view
            canvas.drawRect(aNumberMatrix, p);
        }

        for (int i = 0; i < RectMatrix.length; i++)
            for (int j = 0; j < RectMatrix[i].length; j++) {
                if (activity.getMemoryMatrix()[i][j] > 0) {

                    //2nd v.
                    //Check errors
                    if(activity.getChangeMatrix()[i][j] && FeasibleNumbers(new Point(i, j))) {
                        //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                        Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), matrixCellWidth, matrixColors.getErrorColor(), myTypeface);
                        canvas.drawBitmap(bit, RectMatrix[i][j].centerX() - bit.getWidth() / 2, RectMatrix[i][j].centerY() - bit.getHeight() / 2, p);
                    }

                    else {
                        //Check if numbers are changeable
                        //Check the activity class
                        if (activity.getClass() != Solver.class)
                            if (activity.getChangeMatrix()[i][j]) {
                                //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), matrixCellWidth, matrixColors.getChangeableTextColor(), myTypeface);
                                canvas.drawBitmap(bit, RectMatrix[i][j].centerX() - bit.getWidth() / 2, RectMatrix[i][j].centerY() - bit.getHeight() / 2, p);
                            } else {
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), matrixCellWidth, matrixColors.getIdleTextColor(), myTypeface);
                                canvas.drawBitmap(bit, RectMatrix[i][j].centerX() - bit.getWidth() / 2, RectMatrix[i][j].centerY() - bit.getHeight() / 2, p);
                            }
                        else
                        {
                            if (activity.getChangeMatrix()[i][j]) {
                                //canvas.drawText(String.valueOf(MemoryMatrix[i][j]),textPosition.x, textPosition.y, p );
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), matrixCellWidth, matrixColors.getChangeableTextColor(), myTypeface);
                                canvas.drawBitmap(bit, RectMatrix[i][j].centerX() - bit.getWidth() / 2, RectMatrix[i][j].centerY() - bit.getHeight() / 2, p);
                            } else {
                                Bitmap bit = textAsBitmap(String.valueOf(activity.getMemoryMatrix()[i][j]), matrixCellWidth, matrixColors.getSolveColor(), myTypeface);
                                canvas.drawBitmap(bit, RectMatrix[i][j].centerX() - bit.getWidth() / 2, RectMatrix[i][j].centerY() - bit.getHeight() / 2, p);
                            }
                        }
                    }
                }
            }

        for (int i = 1; i < NumberMatrix.length; i++) {
            Bitmap bit = textAsBitmap(String.valueOf(i), numberMatrixCellWidth, matrixColors.getIdleTextColor(), myTypeface);
            canvas.drawBitmap(bit, NumberMatrix[i].centerX() - bit.getWidth() / 2, NumberMatrix[i].centerY() - bit.getHeight() / 2, p);
        }

        //Bitmap bit = textAsBitmap(currentTime, numberMatrixCellWidth, Color.GREEN, myTypeface);
        //canvas.drawBitmap(bit, 30, 0, p);
        //bit = textAsBitmap(String.valueOf(numberXPos), numberMatrixCellWidth, Color.BLACK, myTypeface);
        //canvas.drawBitmap(bit, 200, 0, p);
        //bit = textAsBitmap(String.valueOf(getTop()), numberMatrixCellWidth, Color.BLACK, myTypeface);
        //canvas.drawBitmap(bit, 400, 0, p);

        for(IGameBoardViewPlugin plugin : plugins)
        {
            plugin.onDraw(canvas);
        }
    }
}

