package io.github.bubinimara.davibet.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.util.TweetCreator
import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.Closeable
import java.io.InputStreamReader


/**
 *
 * Created by Davide Parise
 */
 class StreamTweet(private val apiService: ApiService) {
    companion object {
        const val TAG = "StreamTweet"
    }

    /**
     * Convert a json stream from server to a flow of tweet
     * run until the flow is cancelled or an exception is throw
     */
    fun toFlow(track: String): Flow<Tweet> {
        Log.d(DataRepositoryImpl.TAG, "streamTweets: $track")
        val backoff = 1000L
        return flow<Tweet> {

            val call = apiService.track(track)
            val responseBody = call.execute()

            val streamReader = StreamReaderFlow(responseBody)
            val tweetsFlow = streamReader.read()
            emitAll(tweetsFlow)

        }.retryWhen() { e,count->
            Log.e(TAG, "toFlow: Check Retry $count",e )
            if(count > 3) {
                // to much retries
                return@retryWhen false
            }
            if(e is NetworkException && e.code == 420){
                // limit rate reached
                return@retryWhen false
            }
            //retry
            delay(backoff*count)
            true
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Class to read the stream of data and convert to stream of tweets
     * Read the stream and create a flow of tweet object
     */
    private class StreamReaderFlow(private val response: Response<ResponseBody>): Closeable {
        private var responseBody:ResponseBody? = null
        private var reader:JsonReader? = null

        /**
         * Read from http and stream a flow of tweets
         * Read untill the flow is cancelled or an Exception is throw
         */
        fun read():Flow<Tweet>{
            return flow {
                try {
                    if (!response.isSuccessful) {
                        //throw HttpException(response)
                        throw NetworkException(
                            "Server response not successful: code  ${response.code()}",
                            response.code()
                        )
                    }
                    val body =
                        response.body() ?: throw NetworkException("Body is null", response.code())
                    val inputStream = body.byteStream()
                    reader = JsonReader(InputStreamReader(inputStream))
                    val gson = GsonBuilder().create()

                    while (currentCoroutineContext().isActive) { // read until the flow is cancelled
                        try {
                            val j = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
                            emit(TweetCreator.createFromJson(j))
                        } catch (e: JsonParseException) {
                            // Some are not Tweet objects.Just ignore it for the moment
                            // TODO: handle the others type of messages
                            Log.e(TAG, "read: ", e)
                        }
                    }
                }finally {
                    close()
                }
            }
        }

        /**
         * Closing all resources
         */
        override fun close() {
            Log.d(TAG, "close: Clear connection!")
            kotlin.runCatching {
                reader?.close()
                reader = null
            }

            kotlin.runCatching {
                responseBody?.close()
                responseBody = null
            }
        }
    }
}