package com.miscell.luobo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by jishichen on 2017/4/14.
 */
public class FileUtils {

    private FileUtils() {
    }

    public static void init(Context context) {
        mkDirs(getImageCacheDir(context));
    }

    public static void mkDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists()) file.mkdirs();
    }

    public static String getImageCacheDir(Context context) {
        return getRootDir(context) + "/image/";
    }

    public static String getRootDir(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String cacheDir = "/Android/data/" + context.getPackageName();
            return Environment.getExternalStorageDirectory() + cacheDir;
        } else {
            String path = null;
            File cacheDir = context.getCacheDir();
            if (cacheDir.exists()) path = cacheDir.getAbsolutePath();
            return path;
        }
    }
}
