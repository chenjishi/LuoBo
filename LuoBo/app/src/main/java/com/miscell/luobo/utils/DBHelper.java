package com.miscell.luobo.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.miscell.luobo.home.Feed;
import com.miscell.luobo.home.Gif;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jishichen on 2017/5/3.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "luobo.db";
    private static final int DB_VERSION = 1;

    private static final String TB_NAME_FAVORITE = "favorites";
    private static final String TB_NAME_GIF = "gifs";

    private static final String CATEGORY = "category";
    private static final String TITLE = "title";
    private static final String IMAGE_COUNT = "image_count";
    private static final String DESC = "desc";
    private static final String TIME = "time";
    private static final String VIEWS = "views";
    private static final String TAG = "tag";
    private static final String THUMB = "thumb";
    private static final String URL = "url";
    private static final String GIF_DESC = "gif_desc";
    private static final String GIF_URL = "gif_url";

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
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME_FAVORITE + " (" +
                CATEGORY + " TEXT," +
                TITLE + " TEXT," +
                IMAGE_COUNT + " INTEGER," +
                DESC + " TEXT," +
                TIME + " TEXT," +
                VIEWS + " TEXT," +
                TAG + " TEXT," +
                THUMB + " TEXT," +
                URL + " TEXT PRIMARY KEY)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TB_NAME_GIF + " (" +
                GIF_URL + " TEXT PRIMARY KEY," +
                GIF_DESC + " TEXT, " +
                TIME + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mDb.execSQL("DROP TABLE IF EXISTS " + TB_NAME_FAVORITE);
        mDb.execSQL("DROP TABLE IF EXISTS " + TB_NAME_GIF);
        onCreate(db);
    }

    public List<Feed> loadFavorites() {
        List<Feed> list = null;
        Cursor cursor = null;

        String sql = "SELECT * FROM " + TB_NAME_FAVORITE;
        try {
            cursor = mDb.rawQuery(sql, null);
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                Feed feed = new Feed();
                feed.category = cursor.getString(0);
                feed.title = cursor.getString(1);
                feed.imageCount = cursor.getInt(2);
                feed.desc = cursor.getString(3);
                feed.time = cursor.getString(4);
                feed.views = cursor.getString(5);
                feed.tag = cursor.getString(6);
                feed.thumb = cursor.getString(7);
                feed.url = cursor.getString(8);

                list.add(feed);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) cursor.close();
        }

        return list;
    }

    public void insertFeed(Feed feed) {
        if (null == feed) return;

        String sql = "INSERT OR REPLACE INTO " + TB_NAME_FAVORITE +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        mDb.execSQL(sql, new String[]{
                feed.category,
                feed.title,
                feed.imageCount + "",
                feed.desc,
                feed.time,
                feed.views,
                feed.tag,
                feed.thumb,
                feed.url});
    }

    public List<Gif> loadGifList() {
        List<Gif> list = null;
        Cursor cursor = null;

        String sql = "SELECT * FROM " + TB_NAME_GIF;
        try {
            cursor = mDb.rawQuery(sql, null);
            list = new ArrayList<>();
            while (cursor.moveToNext()) {
                Gif gif = new Gif();
                gif.url = cursor.getString(0);
                gif.desc = cursor.getString(1);
                gif.time = cursor.getLong(2);

                list.add(gif);
            }
        } catch (Exception e) {
        } finally {
            if (null != cursor) cursor.close();
        }

        return list;
    }

    public void insertGif(String url, String desc) {
        if (TextUtils.isEmpty(url)) return;

        String sql = "INSERT OR REPLACE INTO " + TB_NAME_GIF +
                " VALUES (?, ?, ?)";
        mDb.execSQL(sql, new String[]{
                url,
                desc,
                System.currentTimeMillis() + ""});
    }

    public boolean isFavorite(String url) {
        String sql = "SELECT * FROM " + TB_NAME_FAVORITE + " WHERE " + URL +
                "='" + url + "'";
        Cursor cursor = null;
        boolean b = false;

        try {
            cursor = mDb.rawQuery(sql, null);
            b = cursor.moveToNext();
        } catch (Exception e) {
        } finally {
            if (null != cursor) cursor.close();
        }

        return b;
    }
}
