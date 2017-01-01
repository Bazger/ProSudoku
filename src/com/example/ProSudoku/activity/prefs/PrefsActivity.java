package com.example.ProSudoku.activity.prefs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.*;
import android.support.v4.view.PagerTitleStrip;
import com.example.ProSudoku.MyActivity;
import com.example.ProSudoku.R;
import com.example.ProSudoku.activity.board.GameBoardColors;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public enum Border {Small, Big, Chose}

    private static final int[][] matrixBorder = new int[][]{
            {2, 5, 4},//     Small
            {5, 10, 7},//    Normal
            {10, 15, 12}};// Large

    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setSettings(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        try {
            loadPluginsToPrefs(this.getCallingActivity().getClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!isChanged) {
            Intent intent = new Intent();
            isChanged = true;
            intent.putExtra("isChanged", true);
            setResult(RESULT_OK, intent);
        }
    }

    private void loadPluginsToPrefs(String pluginsClassName) throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InstantiationException,
            InvocationTargetException {
        Class<?> pluginClass = Class.forName(pluginsClassName);
        Method getPluginsMethod = pluginClass.getMethod("getPlugins");
        List<GameBoardViewPlugin> plugins = (List<GameBoardViewPlugin>) getPluginsMethod.invoke(pluginClass.newInstance(), null);

        boolean pluginsWithPrefs = false;
        for (GameBoardViewPlugin plugin : plugins) {
            if(plugin.getPreference() != null)
            {
                pluginsWithPrefs = true;
                break;
            }
        }
        if(!pluginsWithPrefs)
        {
            return;
        }

        PreferenceScreen preferenceScreen = (PreferenceScreen) findPreference(getResources().getString(R.string.app_preferences));

        PreferenceCategory pluginCategory = new PreferenceCategory(this);
        pluginCategory.setSummary("Plugins for Game Board mapping");
        pluginCategory.setTitle("Plugins");
        preferenceScreen.addPreference(pluginCategory);

        for (GameBoardViewPlugin plugin : plugins) {
            pluginCategory.addPreference(plugin.getPreference());
        }
    }

    /** Get the current value of the button sound option */
    /*public static boolean getButtonSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.sound_button_key),
		        context.getResources().getBoolean(R.bool.sound_button_default));
    }*/

    /** Get the current value of the music option */
    /*public static boolean getMusic(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.music_key),
				context.getResources().getBoolean(R.bool.music_default));
	}*/


    /**
     * Get the current value of the MatrixBorder option
     */
    public static int getMatrixBorder(Context context, Border border) {
        return matrixBorder[Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getResources().getString(R.string.border_size_key),
                        context.getResources().getString(R.string.border_size_default)))][border.ordinal()];
    }

    /**
     * Get the current value of the Themes option
     */
    public static int getThemes(Context context) {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getResources().getString(R.string.themes_key), context.getResources().getString(R.string.themes_default)));
    }

    /**
     * Get the current value of the Font option
     */
    public static String getFonts(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getResources().getString(R.string.fonts_key), context.getResources().getString(R.string.fonts_default));
    }


    public static void setSettings(Context context) {
        switch (getThemes(context)) {
            default:
                context.setTheme(R.style.AppDayTheme);
                break;
            case 1:
                context.setTheme(R.style.AppNightTheme);
                break;
        }
	    /*if(getButtonSound(context))
		    context.setTheme(R.style.AppTheme);
	    else
		    context.setTheme(R.style.AppThemeWithoutSound);*/
	    /*if(getMusic(context))
		    MusicService.start(context);
	    else
		    MusicService.stop();*/
    }

    public static void setBackground(Activity context) {
        switch (getThemes(context)) {
            default:
                if (context.getClass() == MyActivity.class)
                    context.getWindow().getDecorView().getRootView().setBackgroundResource(R.drawable.day_title);
                else
                    context.getWindow().getDecorView().getRootView().setBackgroundResource(R.drawable.day);
                break;
            case 1:
                if (context.getClass() == MyActivity.class)
                    context.getWindow().getDecorView().getRootView().setBackgroundResource(R.drawable.night_title);
                else
                    context.getWindow().getDecorView().getRootView().setBackgroundResource(R.drawable.night);
                break;
        }
    }

    public static void setPagerTitleStripColor(Context context, PagerTitleStrip pager) {
        switch (getThemes(context)) {
            default:
                pager.setBackgroundResource(R.color.day_pager);
                pager.setTextColor(context.getResources().getColor(R.color.day_pager_text));
                break;
            case 1:
                pager.setBackgroundResource(R.color.night_pager);
                pager.setTextColor(context.getResources().getColor(R.color.night_pager_text));
                break;
        }
    }

    public static GameBoardColors setMatrixColor(Context context) {
        Resources res = context.getResources();
        switch (getThemes(context)) {
            default:
                return new GameBoardColors(res.getColor(R.color.day_popup), res.getColor(R.color.day_normal), res.getColor(R.color.matrix_view_text));
            case 1:
                return new GameBoardColors(res.getColor(R.color.night_popup), res.getColor(R.color.night_pager), res.getColor(R.color.day_popup));
        }
    }

}
