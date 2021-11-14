package io.github.bubinimara.davibet

import android.app.Application
import io.github.bubinimara.davibet.data.db.AppDb
import io.github.bubinimara.davibet.data.network.NetworkMonitor


/**
 *
 * Created by Davide Parise on 12/11/21.
 */
class App: Application() {
    companion object {
        var database: AppDb? = null
        var networkMonitor: NetworkMonitor? = null
    }
    override fun onCreate() {
        super.onCreate()
        database = AppDb.getDb(this)
        networkMonitor = NetworkMonitor(this)
    }
}