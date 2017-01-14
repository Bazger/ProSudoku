package com.example.ProSudoku.activity.board;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.example.ProSudoku.plugin.GameBoardViewPlugin;
import com.example.ProSudoku.plugin.IPreferencePlugin;

import java.util.List;

public abstract class PluginHandlerActivity extends Activity implements IGameBoardActivity {
    public abstract List<GameBoardViewPlugin> getPlugins();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (getPlugins() != null) {
            for (GameBoardViewPlugin plugin : getPlugins()) {
                if (!(plugin instanceof IPreferencePlugin)) {
                    continue;
                }
                IPreferencePlugin preferencePlugin = (IPreferencePlugin) plugin;
                preferencePlugin.load();
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getPlugins() != null) {
            for (GameBoardViewPlugin plugin : getPlugins()) {
                if (!(plugin instanceof IPreferencePlugin)) {
                    continue;
                }
                IPreferencePlugin preferencePlugin = (IPreferencePlugin) plugin;
                preferencePlugin.save();
            }
        }
    }
}
