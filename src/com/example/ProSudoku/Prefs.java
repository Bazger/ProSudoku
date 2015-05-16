package com.example.ProSudoku;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Prefs extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "Prefs";

    public enum Border{Small, Big, Chose}

    private static final int[][] matrixBorder = new int[][]{
            {2, 5, 4},//     Small
            {5, 10, 7},//    Normal
            {10, 15, 12}};// Large

    private boolean isChanged = false;

    public boolean getIsChanged()
    {
        return isChanged;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    //setSettings(this);
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(!isChanged) {
            Intent intent = new Intent();
            intent.putExtra("isChanged", isChanged);
            setResult(RESULT_OK, intent);
            isChanged = true;
        }
    }

    /** Get the current value of the button sound option */
    public static boolean getButtonSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.sound_button_key),
		        context.getResources().getBoolean(R.bool.sound_button_default));
    }

	/** Get the current value of the music option */
	/*public static boolean getMusic(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.music_key),
				context.getResources().getBoolean(R.bool.music_default));
	}*/


	/** Get the current value of the MatrixBorder option */
    public static int getMatrixBorder(Context context, Border border) {
        return matrixBorder[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getResources().getString(R.string.border_size_key),
		                context.getResources().getString(R.string.border_size_default)))][border.ordinal()];
    }

	/** Get the current value of the Themes option */
	/*public static String getThemes(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(OPT_THEMES, OPT_THEMES_DEF);
	}*/

	/** Get the current value of the Font option */
	public static String getFonts(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(context.getResources().getString(R.string.fonts_key), context.getResources().getString(R.string.fonts_default));
	}


    public static void setSettings(Context context)
    {
	    if(getButtonSound(context))
		    context.setTheme(R.style.AppTheme);
	    else
		    context.setTheme(R.style.AppThemeWithoutSound);
	    /*if(getMusic(context))
		    MusicService.start(context);
	    else
		    MusicService.stop();*/
    }
}
