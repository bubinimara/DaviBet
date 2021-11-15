package io.github.bubinimara.davibet.data.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Streaming


/**
 *
 * Created by Davide Parise
 */
interface ApiService {

    @GET("1.1/statuses/filter.json")
    @Streaming
    fun track(@Query("track") terms: String): Call<ResponseBody>
}