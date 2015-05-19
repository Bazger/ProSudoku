package com.example.ProSudoku;

/**
 * Created by Vanya on 18.05.2015
 */
public final class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Polo.ttf");
	}
}