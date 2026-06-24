package com.cafepickuporder.android.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val storeApi: StoreApi by lazy {
        retrofit.create(StoreApi::class.java)
    }

    val favoriteStoreApi: FavoriteStoreApi by lazy {
        retrofit.create(FavoriteStoreApi::class.java)
    }

    val orderApi: OrderApi by lazy {
        retrofit.create(OrderApi::class.java)
    }

    val storeOrderApi: StoreOrderApi by lazy {
        retrofit.create(StoreOrderApi::class.java)
    }

    val storeAccountApi: StoreAccountApi by lazy {
        retrofit.create(StoreAccountApi::class.java)
    }

    val storeMenuManageApi: StoreMenuManageApi by lazy {
        retrofit.create(StoreMenuManageApi::class.java)
    }
}
