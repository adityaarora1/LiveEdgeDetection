package com.adityaarora.liveedgedetection

import android.app.Application
import info.hannes.crashlytic.CrashlyticsTree
import info.hannes.timber.DebugTree
import timber.log.Timber

class DetectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(DebugTree())
        if (!BuildConfig.DEBUG) Timber.plant(CrashlyticsTree())
    }
}