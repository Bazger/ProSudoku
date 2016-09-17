package com.example.ProSudoku;

import android.graphics.Color;

public class MatrixColors {

	private int backColor; // Background
	private int cellColor; // Cell color
	private int sameEmptyNumberCellColor; // ќреол пустой €чейки
	private int choseEmptyCellColor; // ќреол подобной €чейки
	private int choseCellColor; // ќреол выбранной €чейки
	private int changeableTextColor; // ÷вет не закрпленных цифр
	private int idleTextColor; // ÷вет закрепленных цифр
	private int errorColor; // ÷вет ошибочного числа
	private int solveColor; // ÷вет временного числа

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
