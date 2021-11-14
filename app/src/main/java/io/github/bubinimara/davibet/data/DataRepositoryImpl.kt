package io.github.bubinimara.davibet.data

import io.github.bubinimara.davibet.data.model.Tweet
import okhttp3.ResponseBody
import retrofit2.Response
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.db.TweetDao
import io.github.bubinimara.davibet.data.mapper.TweetCreator
import io.github.bubinimara.davibet.data.network.StreamTweet
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.coroutines.suspendCoroutine


class DataRepositoryImpl(private val apiService:ApiService,private val dbService:TweetDao) : DataRepository {
    companion object{
        const val TAG = "DataRepository"
    }

    fun getTweets(track: String):Flow<List<Tweet>>{
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

    fun streamTweets2(track: String):Flow<Tweet>{
        return flow {
            var i = 0
            while (true){
                emit(Tweet("$i"))
                i++
                delay(500)
            }

        }
    }
    override fun streamTweets(track: String): Flow<Tweet> {
        Log.d(TAG, "streamTweets: $track")
        return flow<Tweet> {
            val call = apiService.track()
            val responseBody = call.execute()
            val streamReader = StreamReaderFlow(responseBody)
            val tweetsFlow = streamReader.read()
            emitAll(tweetsFlow)

        }.retry(3) { e->
            true
        }.flowOn(Dispatchers.IO)
    }

    // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
    class StreamReaderFlow(private val response: Response<ResponseBody>):Closeable{
        private var inputStream: InputStream? = null

        fun read():Flow<Tweet>{
            return flow {
                try {
                    if (!response.isSuccessful) {
                        throw Throwable("Server response not successful ")
                    }
                    val body = response.body() ?: throw Exception("Body is null")
                    inputStream = body.byteStream()
                    val reader = JsonReader(InputStreamReader(inputStream))
                    val gson = GsonBuilder().create()
                    while (currentCoroutineContext().isActive) {
                        val j = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
                        try {
                            emit(TweetCreator.createFromJson(j))
                        } catch (e: JsonParseException) {
                            // Some are not Tweet objects.Just ignore it for the moment
                            // TODO: handle the others type of messages
                            Log.e(TAG, "read: ",e )
                        }
                    }
                    close()
                } catch (e: JsonSyntaxException) {
                    Log.e(TAG, "read: Json Exception:",e )
                }
            }
        }

        override fun close() {
            inputStream?.also {
                close()
            }
        }
    }
}