package io.github.bubinimara.davibet.data.network

import io.github.bubinimara.davibet.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.SigningInterceptor
import java.util.concurrent.TimeUnit


/**
 *
 * Created by Davide Parise
 */

class NetworkServices {
    private companion object {
        val BASE_URL = "https://stream.twitter.com"
    }

    private val retrofit:Retrofit
    val apiService: ApiService

    init {

        val consumer = OkHttpOAuthConsumer(
            BuildConfig.API_KEY,
            BuildConfig.API_KEY_SECRET
        )

        consumer.setTokenWithSecret(
            BuildConfig.ACCESS_TOKEN,
            BuildConfig.ACCESS_TOKEN_SECRET
        )

        val client = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS) // every 20 seconds twitter send a ping
            .addInterceptor(SigningInterceptor(consumer))
            .build()

        retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

}