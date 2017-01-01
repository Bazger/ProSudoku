package com.example.ProSudoku.activity.board;

import android.graphics.Color;

public class GameBoardColors {

	private int backColor; // Заставка
	private int cellColor; // Цвет ячейки
	private int sameEmptyNumberCellColor; // Ореол пустой ячейки
	private int choseEmptyCellColor; // Ореол подобной ячейки
	private int choseCellColor; // Ореол выбранной ячейки
	private int changeableTextColor; // Цвет не закрпленных цифр
	private int idleTextColor; // Цвет закрепленных цифр
	private int errorColor; // Цвет ошибочного числа
	private int solveColor; // Цвет временного числа

	public GameBoardColors(int choseCellColor, int choseEmptyCellColor, int changeableTextColor)
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
