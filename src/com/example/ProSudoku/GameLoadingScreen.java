package com.example.ProSudoku;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import com.example.ProSudoku.activity.board.game.GameActivity;
import com.example.ProSudoku.logic.ISudokuGenerator;
import com.example.ProSudoku.logic.SimpleSudokuGenerator;

import static android.content.ContentValues.TAG;

public class GameLoadingScreen {

    private ProgressDialog loadDialog;
    private Context context;
    private GenerateBoardTask generateBoardTask;
    private Difficulty difficulty;
    private boolean closeCallingActivity;

    public GameLoadingScreen(Context context) {
        this.context = context;
    }

    public void start(Difficulty difficulty, boolean closeCallingActivity) {
        createLoadDialog();
        this.difficulty = difficulty;
        this.closeCallingActivity = closeCallingActivity;

        generateBoardTask = new GenerateBoardTask();
        generateBoardTask.execute();
    }

    private void createLoadDialog() {
        loadDialog = new ProgressDialog(context);
        loadDialog.setTitle(R.string.loading_screen_title);
        loadDialog.setMessage(context.getResources().getString(R.string.loading_screen_message));
        loadDialog.setCancelable(false);
        loadDialog.setButton(Dialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.loading_screen_cancel_but), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadDialog.dismiss();
                generateBoardTask.cancel(true);
            }
        });
        loadDialog.show();
    }

    private void startGame(String gameBoard) {
        Log.d(TAG, "clicked on " + difficulty);
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(GameActivity.KEY_DIFFICULTY, difficulty.ordinal());
        intent.putExtra(GameActivity.PREF_MATRIX, gameBoard);
        loadDialog.dismiss();
        context.startActivity(intent);
    }

    private class GenerateBoardTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            ISudokuGenerator generator = new SimpleSudokuGenerator();
            return generator.generate(difficulty);
        }

        @Override
        protected void onPostExecute(String gameBoard) {
            super.onPostExecute(gameBoard);
            if(gameBoard != null)
            {
                startGame(gameBoard);
                if(closeCallingActivity)
                {
                    ((Activity)context).finish();
                }
            }
        }
    }
}


