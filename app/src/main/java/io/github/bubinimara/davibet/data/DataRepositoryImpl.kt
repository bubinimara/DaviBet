package io.github.bubinimara.davibet.data

import android.util.Log
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
        return flow  <List<Tweet>> {
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

                launch (CoroutineName("Remove-Expired-Tweet")){
                    while (true) {
                        Log.d(TAG, "REMOVE: remove expired")
                        dbService.removeExpired()
                        delay(TweetDao.TWEET_LIFETIME)
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
        }.catch { e->
            Log.e(TAG, "getTweets: ",e )
        }.flowOn(Dispatchers.IO)
    }
}