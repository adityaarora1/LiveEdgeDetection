package com.adityaarora.liveedgedetection;

import android.app.Application;

import info.hannes.crashlytic.CrashlyticsTree;
import info.hannes.timber.DebugTree;
import timber.log.Timber;

public class DetectApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new DebugTree());
        if (!BuildConfig.DEBUG)
            Timber.plant(new CrashlyticsTree());
    }
}
