package io.github.bubinimara.davibet.data

import io.github.bubinimara.davibet.data.model.Tweet
import io.github.bubinimara.davibet.data.network.NetworkServices
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.network.ReadExample
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.Dispatcher
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.Runnable


class DataRepositoryImpl(private val apiService:ApiService) : DataRepository {

    companion object{
        const val TAG = "DataRepository"
    }

    fun justStream(){
        val call = apiService.track()
        call.enqueue(ReadExample.responseBodyCallback)
    }
    override fun streamTweets(track: String): Flow<Tweet> {
        return flow<Tweet> {
            Log.d(TAG, "streamTweets: $track")
            emit(Tweet("1"))
            val call = apiService.track()
            call.enqueue(StreamCallback())
        }.flowOn(Dispatchers.IO)
    }

    class StreamCallback:Callback<ResponseBody>{
        private val stream  = MutableStateFlow<Tweet?>(null)
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d(TAG, "onResponse: ")
            if(response.isSuccessful){
                Thread(Runnable {
                    Log.d(TAG, "running: ")
                    read(response)
                }).start()

            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e(TAG, "onFailure: ",t )
        }

        private fun read(response: Response<ResponseBody>){
            Log.d(TAG, "read: ")
            try {
                val body = response.body() ?: return
                val inputStream = body.byteStream()
                val reader = JsonReader(InputStreamReader(inputStream))
                val gson = GsonBuilder().create()
                Log.d(TAG, "run: Read while")
                var j = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
                // Tweet tweet = new Tweet(j.get("text").getAsString());
                ///Tweet tweet = Tweet.fromJsonObject(j);
                Log.d(TAG, "read: $j")
                while (true) {
                    // Several types of messages can be sent.
                    // Some are not Tweet objects.
                    // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
                    j = gson.fromJson(reader, JsonObject::class.java)
                    Log.d(TAG, "run: $j")
                }
            } catch (e: JsonSyntaxException) {
                Log.v(TAG, "Stopped streaming.")
            }
            Log.d(TAG, "run: End")
        }
    }
}