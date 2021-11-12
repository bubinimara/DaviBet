package io.github.bubinimara.davibet.data.network

import io.github.bubinimara.davibet.data.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;



/**
 *
 * Created by Davide Parise
 */
private const val contentType = "Content-Type"
private const val contentTypeValue = "application/json;charset=utf-8"

class NetworkServices {
    private companion object {
        val BASE_URL = "https://stream.twitter.com"
        //val BASE_URL = "https://api.twitter.com/"
    }


    private val retrofit:Retrofit
    val apiService: ApiService

    val oauthInterceptor = Oauth1SigningInterceptor.Builder()
        .consumerKey("c5FjVSnAvXKbQoDGCjuPfI1DW")
        .consumerSecret("MScmUMUsITT2dLJ5jBcicybXQ95Nik8WLmr2oNITM3TZV7HETD")
        .accessToken("1158702590-u7y8KvDH0fy1deKURiumS3LmbYFuJkVIK5koy4F")
        .accessSecret("AUdy3EyV6Nf1UNErFu7tcID1a2LQE8L3OBP3XALBC4mOn")
        .build()

    init {
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val consumer = OkHttpOAuthConsumer(
            "c5FjVSnAvXKbQoDGCjuPfI1DW",
            "MScmUMUsITT2dLJ5jBcicybXQ95Nik8WLmr2oNITM3TZV7HETD"
        )

        consumer.setTokenWithSecret(
            "1158702590-u7y8KvDH0fy1deKURiumS3LmbYFuJkVIK5koy4F",
            "AUdy3EyV6Nf1UNErFu7tcID1a2LQE8L3OBP3XALBC4mOn"
        )

        val client = OkHttpClient.Builder()
            //.addInterceptor(interceptor)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
         //   .addInterceptor(logInterceptor)
            .addInterceptor(SigningInterceptor(consumer))
    //        .addInterceptor(oauthInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

}