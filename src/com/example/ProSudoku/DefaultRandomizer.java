package com.example.ProSudoku;

import java.util.Random;

/**
 * Created by Vanya on 04.03.2015.
 */
public class DefaultRandomizer implements IRandomizer {

    Random rnd = new Random();

    public int GetInt(int max) {
        return rnd.nextInt(max);
    }

    public int GetInt(int min, int max) {
        Random random = new Random();
        int generated = random.nextInt(max - min);//<--Between so Max -Number
        return generated + min;
    }
}
