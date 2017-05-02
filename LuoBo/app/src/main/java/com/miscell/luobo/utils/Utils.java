package com.miscell.luobo.utils;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jishichen on 2017/5/2.
 */
public class Utils {

    private Utils() {
    }

    public static String readFromAssets(Context context, String name) {
        if (null == context) return null;

        InputStream is;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            is = context.getAssets().open(name);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                baos.write(buf, 0, len);
            }
            baos.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toString();
    }

}
