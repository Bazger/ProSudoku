package com.example.ProSudoku;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Prefs extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = "Prefs";

    private static final String OPT_MATRIX_BORDER = "border_size";
    private static final String OPT_MATRIX_BORDER_DEF = "1";

    private static final String OPT_THEMES = String.valueOf(R.string.themes_key);
    private static final String OPT_THEMES_DEF = "1";

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
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        this.setTheme(R.style.AppThemeWithoutSound);
        if(!isChanged) {
            Intent intent = new Intent();
            intent.putExtra("isChanged", isChanged);
            setResult(RESULT_OK, intent);
            isChanged = true;
        }
    }

    /** Get the current value of the music option */
    /*public static boolean getMusic(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(OPT_MUSIC, OPT_MUSIC_DEF);
    }*/

    /** Get the current value of the music option */
    public static int getMatrixBorder(Context context, Border border) {
        return matrixBorder[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_MATRIX_BORDER, OPT_MATRIX_BORDER_DEF))][border.ordinal()];
    }

    /** Get the current value of the music option */
    public static String getThemes(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OPT_THEMES, OPT_THEMES_DEF);
    }

    public static void setSettings(Activity activity)
    {

    }
}
