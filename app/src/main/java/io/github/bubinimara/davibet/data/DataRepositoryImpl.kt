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
import com.google.gson.stream.JsonReader
import io.github.bubinimara.davibet.data.network.ReadExample
import kotlinx.coroutines.flow.*
import java.io.InputStreamReader
import java.io.Reader


class DataRepositoryImpl(private val apiService:ApiService) : DataRepository {

    companion object{
        const val TAG = "DataRepository"
    }

    fun justStream(){
        val call = apiService.track()
        call.enqueue(ReadExample.responseBodyCallback)
    }
    override fun streamTweets(track: String): Flow<Tweet> {
        return flow {
            Log.d(TAG, "streamTweets: $track")
            val gson: Gson = GsonBuilder().create()
            val call = apiService.track()
            //call.enqueue(ReadExample())

                call.enqueue(ReadExample.responseBodyCallback)


/*
            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    Log.d(TAG, "onResponse: getting byte stream ")
                    val inputStream = InputStreamReader(response.body()!!.byteStream(),"UTF-8")
                    val reader = JsonReader(inputStream)
                    reader.beginArray()
                    Log.d(TAG, "onResponse: starting loop")
                    while (reader.hasNext()) {

                        val obj: JsonObject = gson.fromJson(reader,JsonObject::class.java)
                        Log.d(TAG, "onResponse: " + obj.toString())

                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "onFailure: ",t )
                }

            })
            Log.d(TAG, "streamTweets: End")

 */
        }

    }

}