package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.response.NaverLocalSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverLocalSearchApi {
    @GET("v1/search/local.json")
    suspend fun searchLocal(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 5
    ): Response<NaverLocalSearchResponse>
}
