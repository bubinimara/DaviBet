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
import java.io.Closeable
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.Runnable


class DataRepositoryImpl(private val apiService:ApiService) : DataRepository {

    companion object{
        const val TAG = "DataRepository"
    }
    fun courinteCheck():Flow<String>{
        return flow {
            var i = 0
            while (currentCoroutineContext().isActive){
                delay(1000)
                Log.d(TAG, "courinteCheck: "+i++)
                emit(i.toString())
            }
        }
    }


    override fun streamTweets(track: String): Flow<Tweet> {
        val callback = StreamCallback()
        //return flow<Tweet> {
            Log.d(TAG, "streamTweets: $track")
            val call = apiService.track()
            call.enqueue(callback)
            return  callback.toFlow()
                .filterNotNull()
                .cancellable()
                .onEach {
                    currentCoroutineContext().ensureActive()
                }
                .catch { e->
                    Log.e(TAG, "streamTweets: ",e )
                    if(e is CancellationException)
                        callback.close()
                }
                .flowOn(Dispatchers.IO)

    }

    class StreamCallback:Callback<ResponseBody>,Closeable{
        private val stream:Flow<Tweet?>  = MutableStateFlow<Tweet?>(null)
        private var isRunning = true
        private var inputStream: InputStream? = null

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d(TAG, "onResponse: ")
            simualte()
            if(response.isSuccessful){
                //read(response)
            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e(TAG, "onFailure: ",t )
        }

        private fun  simualte(){
            val f:Flow<String>  = flow{
                while(currentCoroutineContext().isActive){

                }
            }
            while(isRunning){
                Thread.sleep(1000)
                Log.d(TAG, "simualte: ")
            }
        }
        private  fun read(response: Response<ResponseBody>){
            Log.d(TAG, "read: ")
            /*withContext(Dispatchers.IO) *///{
                try {
                    val body = response.body() ?: throw Exception("Body is null")
                    inputStream = body.byteStream()
                    val reader = JsonReader(InputStreamReader(inputStream))
                    val gson = GsonBuilder().create()
                    Log.d(TAG+"_Readed", "run: Read while")
                    var j = gson.fromJson<JsonObject>(reader, JsonObject::class.java)
                    // Tweet tweet = new Tweet(j.get("text").getAsString());
                    ///Tweet tweet = Tweet.fromJsonObject(j);
                    Log.d(TAG+"_Readed", "read: $j")
                    while (isRunning) {
                        // Several types of messages can be sent.
                        // Some are not Tweet objects.
                        // https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object.html
                        j = gson.fromJson(reader, JsonObject::class.java)
                        Log.d("_Readed", "run: $j")
                    }
                } catch (e: JsonSyntaxException) {
                    Log.v(TAG, "Stopped streaming.")
                }
                Log.d(TAG, "run: End")
            //}
        }

        override fun close() {
            isRunning = false
            if(inputStream!=null){
                try {
                    inputStream!!.close()
                } catch (e: Exception) {
                }
            }
        }

        fun toFlow(): Flow<Tweet> {
            return stream.filterNotNull()
        }

    }
}