package com.example.ProSudoku;

/**
 * Created by Vanya on 16.04.2015
 */

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DB implements BaseColumns{

    public enum Dif {Beginner, Easy, Medium, Hard}

    private static final String DB_TABLE = "records";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_DIFFICULTY = "difficulty";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static int tableCount = 10;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DB_TABLE + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    COLUMN_TIME + INTEGER_TYPE + COMMA_SEP +
                    COLUMN_DIFFICULTY + INTEGER_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DB_TABLE;

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(DB_TABLE, null, null, null, null, null, null);
    }

    public Cursor getQuery(String[] str1, String str2, String[] str3, String str4, String str5, String str6) {
        return mDB.query(DB_TABLE, str1, str2,  str3, str4, str5, str6);
    }

    // добавить запись в DB_TABLE
    public void addRec(String txt, long seconds, Dif dif) {
        Cursor c = getQuery(null, DB.COLUMN_DIFFICULTY + " == ?", new String[]{dif.ordinal() + ""}, null, null, DB.COLUMN_TIME);
        if(c.getCount() >= tableCount) {
            c.moveToLast();
            delRec(c.getInt(c.getColumnIndex(DB._ID)));
        }
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, txt);
        cv.put(COLUMN_TIME, seconds);
        cv.put(COLUMN_DIFFICULTY, dif.ordinal());
        mDB.insert(DB_TABLE, null, cv);
    }

    // удалить запись из DB_TABLE
    public void delRec(long id) {
        mDB.delete(DB_TABLE, _ID + " = " + id, null);
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "Records.db";
        private static final int DB_VERSION = 1;

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
            /*ContentValues cv = new ContentValues();
            for (int i = 1; i < 5; i++) {
                //addRec("sometext " + i, i * 5, Dif.values()[i - 1]);
                cv.put(COLUMN_NAME, "sometext " + i);
                cv.put(COLUMN_TIME, i * 5);
                cv.put(COLUMN_DIFFICULTY, i - 1);
                db.insert(DB_TABLE, null, cv);
            }*/
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}