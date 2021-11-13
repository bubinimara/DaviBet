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
import io.github.bubinimara.davibet.data.mapper.TweetMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader


class DataRepositoryImpl(private val apiService:ApiService) : DataRepository {
    companion object{
        const val TAG = "DataRepository"
    }

    override fun streamTweets(track: String): Flow<Tweet> {
        Log.d(TAG, "streamTweets: $track")
        return flow<Tweet> {
            val call = apiService.track()
            val responseBody = call.execute()
            val streamReader = StreamReaderFlow(responseBody)
            val tweetsFlow = streamReader.read()
            emitAll(tweetsFlow)

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
                        // Some are not Tweet objects.
                        val j = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
                        try {
                            emit(TweetMapper.createTweet(j))
                        } catch (e: JsonParseException) {
                            Log.e(TAG, "read: ",e )

                        }
/*
                        Log.d(TAG, "****************** OBJ *********************************** ")
                        Log.d("_Readed", "run: $j")
                        Log.d(TAG, "***************** END OBJ ********************************* ")
*/
                    }
                    close()
              } catch (e: JsonSyntaxException) {

                    Log.d(TAG, "********************** ERROR ******************************** ")
                    Log.v(TAG, "Stopped streaming.")
                    e.printStackTrace()
                    Log.d(TAG, "********************* END ERROR ******************************** ")
                }
                Log.d(TAG, "run: End")
            }
        }

        override fun close() {
            inputStream?.also {
                close()
            }
        }
    }
}