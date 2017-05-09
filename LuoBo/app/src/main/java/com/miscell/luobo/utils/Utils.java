package com.miscell.luobo.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jishichen on 2017/5/2.
 */
public class Utils {

    private Utils() {
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static TextView generateTextView(Context context, int resId, int color, float size) {
        return generateTextView(context, context.getString(resId), color, size);
    }

    public static TextView generateTextView(Context context, String text, int color, float size) {
        TextView view = new TextView(context);
        view.setTextColor(color);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        view.setText(text);

        return view;
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

    public static int dp2px(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (density * dp + .5f);
    }

    public static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context ctx, int resId) {
        showToast(ctx, ctx.getString(resId));
    }

    public static boolean isNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null != cm) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (null != info) {
                return info.isConnected();
            }
        }

        return false;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
        }

        return pi;
    }

    public static String getVersionName(Context context) {
        PackageInfo pi = getPackageInfo(context);
        return null != pi ? pi.versionName : "";
    }

    public static int getVersionCode(Context context) {
        PackageInfo pi = getPackageInfo(context);
        return null != pi ? pi.versionCode : 0;
    }
}
