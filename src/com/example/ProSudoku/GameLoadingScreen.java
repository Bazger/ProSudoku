package com.example.ProSudoku;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.TextView;
import com.example.ProSudoku.activity.board.game.GameActivity;
import com.example.ProSudoku.activity.prefs.PrefsActivity;
import com.example.ProSudoku.logic.ISudokuGenerator;
import com.example.ProSudoku.logic.SimpleSudokuGenerator;
import com.example.ProSudoku.logic.WebSudokuGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class GameLoadingScreen {

    private ProgressDialog loadDialog;
    private Context context;
    private GenerateBoardTask generateBoardTask;
    private Difficulty difficulty;
    private boolean closeCallingActivity;
    private ArrayList<String> loadingMessages;
    private Thread updatedMessagesProcess = new Thread(new Runnable() {
        @Override
        public void run() {
            int i = 0;
            try {
                while (true) {
                    Message msg = new Message();
                    msg.obj = loadingMessages.get(i % loadingMessages.size());
                    loadDialogHandler.sendMessage(msg);
                    TimeUnit.MILLISECONDS.sleep(context.getResources().getInteger(R.integer.loading_change_messages_time));
                    i++;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    private Handler loadDialogHandler = new Handler() {
        public void handleMessage(Message msg) {
            loadDialog.setMessage(((String)msg.obj));
        }
    };


    public GameLoadingScreen(Context context) {
        this.context = context;
        loadingMessages = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(R.array.loading_messages)));
        Collections.shuffle(loadingMessages);
    }

    public void start(Difficulty difficulty, boolean closeCallingActivity) {
        createLoadDialog();
        this.difficulty = difficulty;
        this.closeCallingActivity = closeCallingActivity;

        ISudokuGenerator generator;
        if (PrefsActivity.getDownloadSudoku(context) && isInternetAvailable()) {
            generator = new WebSudokuGenerator();
            loadDialog.setMessage(context.getResources().getString(R.string.loading_screen_downloading_message));
        } else {
            generator = new SimpleSudokuGenerator();
            updatedMessagesProcess.start();
        }

        generateBoardTask = new GenerateBoardTask(generator);
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

    private boolean isInternetAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity.getActiveNetworkInfo() != null) {
            if (connectivity.getActiveNetworkInfo().isConnected())
                return true;
        }
        return false;
    }

    private class GenerateBoardTask extends AsyncTask<Void, Void, String> {

        private ISudokuGenerator generator;

        private GenerateBoardTask(ISudokuGenerator generator) {
            this.generator = generator;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            return generator.generate(difficulty);
        }

        @Override
        protected void onPostExecute(String gameBoard) {
            super.onPostExecute(gameBoard);
            if(updatedMessagesProcess.isAlive()) {
                updatedMessagesProcess.interrupt();
            }

            if (gameBoard == null) {
                loadDialog.dismiss();
                return;
            }

            startGame(gameBoard);
            if (closeCallingActivity) {
                ((Activity) context).finish();
            }
        }
    }
}


