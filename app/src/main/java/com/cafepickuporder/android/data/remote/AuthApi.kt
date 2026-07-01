package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.LoginRequest
import com.cafepickuporder.android.data.request.CustomerPhoneUpdateRequest
import com.cafepickuporder.android.data.request.CustomerProfileUpdateRequest
import com.cafepickuporder.android.data.request.SignupRequest
import com.cafepickuporder.android.data.response.LoginResponse
import com.cafepickuporder.android.data.response.MyInfoResponse
import com.cafepickuporder.android.data.response.SignupResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<SignupResponse>

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @GET("/api/customers/me")
    suspend fun getMyInfo(
        @Header("Authorization") authorization: String
    ): Response<MyInfoResponse>

    @PATCH("/api/customers/me/profile")
    suspend fun updateMyProfile(
        @Header("Authorization") authorization: String,
        @Body request: CustomerProfileUpdateRequest
    ): Response<MyInfoResponse>

    @PATCH("/api/customers/me/phone")
    suspend fun updateMyPhone(
        @Header("Authorization") authorization: String,
        @Body request: CustomerPhoneUpdateRequest
    ): Response<MyInfoResponse>
}
