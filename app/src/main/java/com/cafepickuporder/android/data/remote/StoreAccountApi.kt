package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.StoreAccountLoginRequest
import com.cafepickuporder.android.data.response.StoreAccountLoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface StoreAccountApi {

    @POST("/api/store-accounts/login")
    suspend fun login(
        @Body request: StoreAccountLoginRequest
    ): Response<StoreAccountLoginResponse>
}
