package com.location.reminder.sound.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitUtil {

    private const val BASE_URL = "https://maps.googleapis.com"
    private lateinit var retrofit: Retrofit
    private const val connectTimeUnit = 10L
    private const val readTimeUnit = 40L
    private const val writeTimeUnit = 10L

    private var httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        .connectTimeout(connectTimeUnit, TimeUnit.SECONDS)
        .readTimeout(readTimeUnit, TimeUnit.SECONDS)
        .writeTimeout(writeTimeUnit, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        )

    private val gson = GsonBuilder().setLenient().create()

    fun getInstance(): Retrofit {
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
        return retrofit
    }

}