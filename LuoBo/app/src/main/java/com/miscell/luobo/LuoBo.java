package com.miscell.luobo;

import android.app.Application;
import com.flurry.android.FlurryAgent;

/**
 * Created by jishichen on 2017/5/2.
 */
public class LuoBo extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new FlurryAgent.Builder().build(this, "45SSN8VDKBMG5RJXVQ8B");
    }
}
