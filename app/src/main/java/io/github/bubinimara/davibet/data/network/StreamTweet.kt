package io.github.bubinimara.davibet.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.ApiService
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.mapper.TweetCreator
import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import okhttp3.ResponseBody
import okhttp3.internal.wait
import retrofit2.Response
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*


/**
 *
 * Created by Davide Parise on 14/11/21.
 */
class StreamTweet(val apiService: ApiService) {
    companion object {
        const val TAG = "StreamTweet"
    }

    fun toFlow(track: String): Flow<Tweet> {
        Log.d(DataRepositoryImpl.TAG, "streamTweets: $track")
        return flow<Tweet> {
            val call = apiService.track(track)
            val responseBody = call.execute()
            val streamReader = StreamReaderFlow(responseBody)
            val tweetsFlow = streamReader.read()
            emitAll(tweetsFlow)

        }.retry(3) { e->
            Log.e(TAG, "streamTweets: ",e )
            false // not retry
        }.flowOn(Dispatchers.IO)
    }

    // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
    class StreamReaderFlow(private val response: Response<ResponseBody>): Closeable {
        private var inputStream: InputStream? = null

        fun read():Flow<Tweet>{
            return flow {
                try {
                    if (!response.isSuccessful) {
                        throw Throwable("Server response not successful: code  ${response.code()}")
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
                            Log.e(DataRepositoryImpl.TAG, "read: ",e )
                        }
                    }
                } catch (e: JsonSyntaxException) {
                    Log.e(DataRepositoryImpl.TAG, "read: Json Exception:",e )
                }
            }
        }

        override fun close() {
            inputStream?.also {
                close()
            }
            inputStream = null
        }
    }
}