package io.github.bubinimara.davibet.data.network

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
            "c5FjVSnAvXKbQoDGCjuPfI1DW",
            "MScmUMUsITT2dLJ5jBcicybXQ95Nik8WLmr2oNITM3TZV7HETD"
        )

        consumer.setTokenWithSecret(
            "1158702590-u7y8KvDH0fy1deKURiumS3LmbYFuJkVIK5koy4F",
            "AUdy3EyV6Nf1UNErFu7tcID1a2LQE8L3OBP3XALBC4mOn"
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