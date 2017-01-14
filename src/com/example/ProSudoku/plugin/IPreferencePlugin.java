package com.example.ProSudoku.plugin;

import android.preference.Preference;

public interface IPreferencePlugin {
    void load();
    void save();
    Preference getPreference();
}
