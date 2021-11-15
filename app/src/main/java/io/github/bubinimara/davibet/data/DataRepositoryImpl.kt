package io.github.bubinimara.davibet.data

import android.util.Log
import io.github.bubinimara.davibet.AppConfig
import io.github.bubinimara.davibet.data.db.TweetDao
import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.ApiService
import io.github.bubinimara.davibet.data.network.StreamTweet
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


class DataRepositoryImpl(private val apiService: ApiService, private val dbService:TweetDao) : DataRepository {
    companion object{
        const val TAG = "DataRepository"
    }

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
                    // never here
                    Log.d(TAG, "INSERT: end insert")
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
                            dbService.truncateTable()
                            delay(AppConfig.DB_TABLE_SIZE_CHECK_INTERVAL_TIME_MS)
                        }
                    }
                }
                // block
                try {
                    // emit all tweets from the database
                    emitAll(dbService.getTweets())
                } catch (e: Exception) {
                    Log.e(TAG, "getTweets:EMITALL ", e)
                }

                // todo:free all resources
                Log.d(TAG, "getTweets: ALL END")
            }
        }.flowOn(Dispatchers.IO)
    }
}