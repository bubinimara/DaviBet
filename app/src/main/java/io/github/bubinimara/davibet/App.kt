package io.github.bubinimara.davibet

import android.app.Application
import android.os.Debug
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp


/**
 *
 * Created by Davide Parise
 */
@HiltAndroidApp
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }
    }
}