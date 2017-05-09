package com.miscell.luobo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by jishichen on 2017/3/31.
 */
public class Config {

    private static final String CONFIG_FILE_NAME = "app_config";

    private static final String KEY_CHECK_UPDATE_TIME = "check_time";

    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private Config() {
        throw new AssertionError();
    }

    public static void saveLastCheckTime(Context context, long t) {
        putLong(context, KEY_CHECK_UPDATE_TIME, t + ONE_DAY);
    }

    public static long getLastCheckTime(Context context) {
        return getLong(context, KEY_CHECK_UPDATE_TIME, -1L);
    }

    private static long getLong(Context context, String key, long defaultValue) {
        return getPreferences(context).getLong(key, defaultValue);
    }

    private static void putLong(Context context, String key, long value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
    }
}
