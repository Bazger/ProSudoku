package com.example.ProSudoku.activity.board;

import android.graphics.Color;

public class GameBoardColors {

	private int backColor; // Заставка
	private int cellColor; // Цвет ячейки
	private int selectedEmptyCellColor; // Выделение для пустой ячейки
	private int sameCellsToSelectedCellColor; // Выделение для ячеек с таким же числом как у выделенной ячейки
	private int selectedNotEmptyCellColor; // Выделение для НЕ пустой ячейки
	private int changeableTextColor; // Цвет не закрпленных цифр
	private int idleTextColor; // Цвет закрепленных цифр
	private int errorColor; // Цвет ошибочного числа
	private int solveColor; // Цвет временного числа

	public GameBoardColors(int selectedNotEmptyCellColor, int sameCellsToSelectedCellColor, int changeableTextColor)
	{

		this.selectedNotEmptyCellColor = selectedNotEmptyCellColor;//Color.rgb(50, 205, 50);
		this.sameCellsToSelectedCellColor = sameCellsToSelectedCellColor;
		this.selectedEmptyCellColor = Color.RED;//Color.parseColor("#EB4604");
		

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
	public int getSelectedNotEmptyCellColor() {
		return selectedNotEmptyCellColor;
	}
	public int getSameCellsToSelectedCellColor() {
		return sameCellsToSelectedCellColor;
	}
	public int getErrorColor() {
		return errorColor;
	}
	public int getIdleTextColor() {
		return idleTextColor;
	}
	public int getSelectedEmptyCellColor() {
		return selectedEmptyCellColor;
	}
	public int getSolveColor() {
		return solveColor;
	}
}
