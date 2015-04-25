package com.example.ProSudoku;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Vanya on 21.02.2015
 */
public class About extends Activity{

    AboutScrollView chat_ScrollView;
    TextView chat_text_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //timerView = new TimerView(this);
        setContentView(R.layout.about);

        chat_ScrollView = (AboutScrollView) this.findViewById(R.id.chat_ScrollView);
        chat_text_chat = (TextView) this.findViewById(R.id.chat_text_chat);
    }
}