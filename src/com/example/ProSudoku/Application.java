package com.example.ProSudoku;

public final class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		FontsOverride.setDefaultFont(this, "MONOSPACE", "Polo.ttf");
	}
}