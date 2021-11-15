package io.github.bubinimara.davibet.data

import android.util.Log
import io.github.bubinimara.davibet.AppConfig
import io.github.bubinimara.davibet.data.db.TweetDao
import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.ApiService
import io.github.bubinimara.davibet.data.network.StreamTweet
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Responsibility of this class is to fetch data from network and populate the db
 * Maintain the db up-to-date cleaning old tweets and truncate the db
 *
 * The single source of truth is the db - the user always reflect what in the db is
 *
 */
class DataRepositoryImpl(private val apiService: ApiService, private val dbService:TweetDao) : DataRepository {
    companion object{
        const val TAG = "DataRepository"
    }

    /**
     * Get the list of tweet
     */
    override fun getTweets(track: String):Flow<List<Tweet>>{
        return flow {
            dbService.removeTweets()

            // new coroutine in the same scope of the parent
            // if parent end this end too
            coroutineScope {
                launch(CoroutineName("Stream-Tweet")) {
                    StreamTweet(apiService).toFlow(track)
                        .conflate() // backpressure - let go to loose some tweet
                        .collect{
                        Log.d(TAG, "INSERT: Inserting tweet $it")
                        dbService.insertTweet(it)
                    }
                }

                if(AppConfig.DB_TWEET_LIFETIME>0) {
                    launch(CoroutineName("Remove-Expired-Tweet")) {
                        while (true) {
                            Log.d(TAG, "REMOVE: remove expired")
                            dbService.removeExpired()
                            delay(AppConfig.DB_TWEET_LIFETIME)
                        }
                    }
                }

                if(AppConfig.DB_TABLE_SIZE_CHECK_INTERVAL_TIME_MS>0) {
                    launch(CoroutineName("Truncate-Table")) {
                        while (true) {
                            Log.d(TAG, "TRUNCATE: truncate db")
                            dbService.truncateTable()
                            delay(AppConfig.DB_TABLE_SIZE_CHECK_INTERVAL_TIME_MS)
                        }
                    }
                }

                // intentionally block
                try {
                    // emit all tweets from the database
                    emitAll(dbService.getTweets())
                } catch (e: Exception) {
                    Log.e(TAG, "getTweets:emitAll End ", e)
                }

                // free all resources
                Log.d(TAG, "getTweets: END")
            }
        }.flowOn(Dispatchers.IO)
    }
}