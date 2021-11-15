package io.github.bubinimara.davibet

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.github.bubinimara.davibet.data.db.DatabaseService
import io.github.bubinimara.davibet.data.network.NetworkMonitor


/**
 *
 * Created by Davide Parise on 12/11/21.
 */
@HiltAndroidApp
class App: Application() {
    companion object {
        var database: DatabaseService? = null
        var networkMonitor: NetworkMonitor? = null
    }
    override fun onCreate() {
        super.onCreate()
        database = DatabaseService.getDb(this)
        networkMonitor = NetworkMonitor(this)
    }
}