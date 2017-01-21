package com.example.ProSudoku;

import java.util.Locale;

public final class Application extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		SetFontByLanguage();
	}

	private  void SetFontByLanguage()
	{
		String s = Locale.getDefault().getLanguage();
		if (s.equals("ru")) {
			FontsOverride.setDefaultFont(this, "MONOSPACE", "BrushType.ttf");

		} else {
			FontsOverride.setDefaultFont(this, "MONOSPACE", "Polo.ttf");

		}
	}
}