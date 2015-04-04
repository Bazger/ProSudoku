package com.example.ProSudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class MyActivity extends Activity implements OnClickListener {

    private static final String TAG = "Sudoku";
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.continue_button:
                startGame(Game.DIFFICULTY_CONTINUE);
                break;
            case R.id.new_button:
                openNewGameDialog();
                break;
            case R.id.solver_button:
                Intent l = new Intent(this,Solver.class);
                startActivity(l);
                break;
            case R.id.how_to_play:
                Intent j = new Intent(this,HowToPlay.class);
                startActivity(j);
                break;
            case R.id.exit_button:
                finish();
                break;
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }*/



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
                startActivity(new Intent(this, Prefs.class));
                return true;
            // More items go here (if any) ...
        }
        return false;
    }

    /** Ask the user what difficulty level they want */
    private void openNewGameDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.new_game_title)
                .setItems(R.array.difficulty,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface,
                                                int i) {
                                startGame(i);
                            }
                        })
                .show();
    }

    private void startGame(int i) {
        Log.d(TAG, "clicked on " + i);
        Intent intent = new Intent(MyActivity.this, Game.class);
        intent.putExtra(Game.KEY_DIFFICULTY, i);
        startActivity(intent);
    }
}
