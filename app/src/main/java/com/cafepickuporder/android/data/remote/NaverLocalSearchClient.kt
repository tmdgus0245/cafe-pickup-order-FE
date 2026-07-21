package com.cafepickuporder.android.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NaverLocalSearchClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://openapi.naver.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: NaverLocalSearchApi by lazy {
        retrofit.create(NaverLocalSearchApi::class.java)
    }
}
