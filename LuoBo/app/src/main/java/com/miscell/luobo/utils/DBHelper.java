package com.miscell.luobo.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.miscell.luobo.home.Feed;

/**
 * Created by jishichen on 2017/5/3.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "luobo.db";
    private static final int DB_VERSION = 1;

    private static SQLiteDatabase mDb = null;
    private static DBHelper INSTANCE = null;

    public static DBHelper getInstance(Context context) {
        if (null == INSTANCE) {
            INSTANCE = new DBHelper(context);
            mDb = INSTANCE.getWritableDatabase();
        }

        return INSTANCE;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Feed getFavoriteById(String id) {
        if (TextUtils.isEmpty(id)) return null;

        Feed feed = null;
        return feed;
    }

}
