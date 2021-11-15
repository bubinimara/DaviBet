package io.github.bubinimara.davibet.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.DataRepositoryImpl
import io.github.bubinimara.davibet.data.mapper.TweetCreator
import io.github.bubinimara.davibet.data.model.Tweet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader


/**
 *
 * Created by Davide Parise
 */
class StreamTweet(val apiService: ApiService) {
    companion object {
        const val TAG = "StreamTweet"
    }

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
            Log.e(TAG, "streamTweets: Try Retry ",e )
            if(count > 3)
                return@retryWhen false
            when(e){
                is IOException -> {}
                is HttpException -> {
                    if(e.code() == 420){
                        return@retryWhen false
                    }
                }
                else ->  {}          }
            delay(backoff*count)
            true
        }.flowOn(Dispatchers.IO)
    }

    // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
    class StreamReaderFlow(private val response: Response<ResponseBody>): Closeable {
        private var inputStream: InputStream? = null

        fun read():Flow<Tweet>{
            return flow {
                try {
                    if (!response.isSuccessful) {
                        throw NetworkException("Server response not successful: code  ${response.code()}",response.code())
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