package com.example.ProSudoku.logic;

import android.support.annotation.Nullable;
import com.example.ProSudoku.DefaultRandomizer;
import com.example.ProSudoku.Difficulty;
import com.example.ProSudoku.IRandomizer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

public class WebSudokuGenerator implements ISudokuGenerator {

    private final static String LINK = "https://sudoku.bestcrosswords.ru/generator";
    private final static String GENERATION_SEED = "id";
    private final static String GENERATION_LEVEL = "level";
    private final static int TIMEOUT = 20000;
    private final static Dictionary<Difficulty, Integer> GAME_DIFFICULTY_TO_LEVEL = new Hashtable<Difficulty, Integer>() {
        {
            put(Difficulty.Beginner, 2);
            put(Difficulty.Easy, 3);
            put(Difficulty.Medium, 4);
            put(Difficulty.Hard, 5);
        }
    };
    private IRandomizer randomizer = new DefaultRandomizer();

    @Nullable
    @Override
    public String generate(Difficulty difficulty) {
        try {
            Document doc = downloadWebPageHTML(difficulty);
            return findSudokuGameBoard(doc);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Document downloadWebPageHTML(Difficulty difficulty) throws Exception {

        String urlSeed = GENERATION_SEED + "=" + randomizer.GetInt(0, Integer.MAX_VALUE);
        String urlLevel = GENERATION_LEVEL + "=" + GAME_DIFFICULTY_TO_LEVEL.get(difficulty);
        URL url = new URL(LINK + "?" + urlSeed + "&" + urlLevel);

        return Jsoup.parse(url,TIMEOUT);
    }

    private String findSudokuGameBoard(Document doc)
    {
        String gameBoard = doc.select("ins.sudoku").attr("data-grid").replaceAll("\\p{P}","");
        return gameBoard;
    }
}
