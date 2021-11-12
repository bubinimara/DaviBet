package io.github.bubinimara.davibet.data

import kotlinx.coroutines.flow.Flow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


/**
 *
 * Created by Davide Parise on 12/11/21.
 */
interface ApiService {

    @GET("1.1/statuses/filter.json")
    @Streaming
    fun track(@Query("track") terms: String = "twitter"): Call<ResponseBody>
}