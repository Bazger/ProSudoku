package com.example.ProSudoku;

import android.graphics.Color;

/**
 * Created by Vanya on 23.05.2015
 */
public class MatrixColors {

	private int backColor;
	private int cellColor;
	private int sameEmptyNumberCellColor;
	private int choseEmptyCellColor;
	private int choseCellColor;
	private int changeableTextColor;
	private int idleTextColor;
	private int errorColor;
	private int solveColor;

	public MatrixColors(int choseCellColor,int choseEmptyCellColor, int changeableTextColor)
	{

		this.choseCellColor = choseCellColor;//Color.rgb(50, 205, 50);
		this.choseEmptyCellColor = choseEmptyCellColor;
		this.sameEmptyNumberCellColor = Color.RED;//Color.parseColor("#EB4604");
		

		this.backColor = Color.BLACK;
		this.cellColor = Color.WHITE;
		this.idleTextColor = Color.BLACK;
		this.changeableTextColor = changeableTextColor;
		this.errorColor = Color.parseColor("#EB4604");
		this.solveColor = Color.GRAY;
	}

	public int getBackColor() {
		return backColor;
	}
	public int getCellColor() {
		return cellColor;
	}
	public int getChangeableTextColor() {
		return changeableTextColor;
	}
	public int getChoseCellColor() {
		return choseCellColor;
	}
	public int getChoseEmptyCellColor() {
		return choseEmptyCellColor;
	}
	public int getErrorColor() {
		return errorColor;
	}
	public int getIdleTextColor() {
		return idleTextColor;
	}
	public int getSameEmptyNumberCellColor() {
		return sameEmptyNumberCellColor;
	}
	public int getSolveColor() {
		return solveColor;
	}
}
