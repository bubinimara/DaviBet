package io.github.bubinimara.davibet

import java.util.concurrent.TimeUnit


/**
 *
 * Created by Davide Parise
 * Global App Configuration
 *
 * All constants here
 */
object AppConfig {
    /*
    * Database
    */

    // lifetime of a tweet - after `lifetime` it is removed
    // <=0 to disable it or value >0 expressed in milliseconds
    /*const*/ val DB_TWEET_LIFETIME = TimeUnit.SECONDS.toMillis(60) // -1L

    // every `interval` truncate de db into `size`
    // <= 0 to disable it or value >0 expressed in milliseconds
    /*const */val DB_TABLE_SIZE_CHECK_INTERVAL_TIME_MS = TimeUnit.MINUTES.toMillis(2)
    const val     DB_TABLE_SIZE = 500

    /*
     * Connection
     */
    const val CONN_CONNECT_TIMEOUT_SECONDS = 30L
    const val CONN_READ_TIMEOUT_SECONDS = 25L
}