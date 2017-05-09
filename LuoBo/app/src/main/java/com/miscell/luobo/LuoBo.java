package com.miscell.luobo;

import android.app.Application;
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.flurry.android.FlurryAgent;
import com.miscell.luobo.utils.FileUtils;

/**
 * Created by jishichen on 2017/5/2.
 */
public class LuoBo extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new FlurryAgent.Builder().build(this, "45SSN8VDKBMG5RJXVQ8B");
        FileUtils.init(this);
    }
}
