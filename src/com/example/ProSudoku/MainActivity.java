package com.example.ProSudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.example.ProSudoku.activity.about.AboutActivity;
import com.example.ProSudoku.activity.board.game.GameActivity;
import com.example.ProSudoku.activity.board.solver.SolverActivity;
import com.example.ProSudoku.activity.howtoplay.HowToPlayActivity;
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.activity.scores.ScoresActivity;

import java.lang.reflect.Method;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = "Sudoku";
    private boolean isFirstTime = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	    PrefsActivity.setSettings(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
	    PrefsActivity.setBackground(this);

        //Set up Click listeners for all the buttons
        View continueButton = findViewById(R.id.continue_button);
        continueButton.setOnClickListener(this);
        View newButton = findViewById(R.id.new_button);
        newButton.setOnClickListener(this);
        View howToPlayButton = findViewById(R.id.how_to_play);
        howToPlayButton.setOnClickListener(this);
        View aboutButton = findViewById(R.id.about_button);
        aboutButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
        View solverButton = findViewById(R.id.solver_button);
        solverButton.setOnClickListener(this);
        View scoresButton = findViewById(R.id.scores_button);
        scoresButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continue_button:
                if (getSharedPreferences(getSharedPrefFileNameFromActivity(GameActivity.class), MODE_PRIVATE)
                        .getString(GameActivity.PREF_MATRIX, null) != null)
                {
                    Intent n = new Intent(this, GameActivity.class);
                    startActivity(n);
                }
                else
                    openNewGameDialog();
                break;
            case R.id.new_button:
                openNewGameDialog();
                break;
            case R.id.solver_button:
                Intent l = new Intent(this, SolverActivity.class);
	            startActivity(l);
                break;
            case R.id.scores_button:
                Intent j = new Intent(this, ScoresActivity.class);
                startActivity(j);
                break;
            case R.id.how_to_play:
                Intent k = new Intent(this, HowToPlayActivity.class);
                startActivity(k);
                break;
            case R.id.about_button:
                Intent z = new Intent(this, AboutActivity.class);
                startActivity(z);
                break;
            case R.id.exit_button:
                finish();
                break;
        }
    }

    public String getSharedPrefFileNameFromActivity(Class activity)
    {
        return activity.getName().replace(this.getClass().getPackage().getName() + ".", "");
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }*/


    @Override
    protected void onResume() {
        super.onResume();
        if(getThemeId(this) != PrefsActivity.getThemeResId(this))
        {
            finish();
            startActivity(getIntent());
        }
    }

    private int getThemeId(Context context) {
        try {
            Class<?> wrapper = context.getClass();
            Method method = wrapper.getMethod("getThemeResId");
            method.setAccessible(true);
            return (Integer) method.invoke(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, PrefsActivity.class);
                startActivityForResult(intent, 1);

                return true;
            // More items go here (if any) ...
        }
        return false;
    }

    /** Ask the user what difficulty level they want */
    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulties,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface,
                                                int i) {
                                new GameLoadingScreen(MainActivity.this).start(Difficulty.values()[i], false);
                            }
                        })
                .show();
    }
}