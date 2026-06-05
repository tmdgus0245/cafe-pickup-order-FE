package com.cafepickuporder.android.data.response

data class LoginResponse(
    val customerId: Long,
    val email: String,
    val name: String,
    val accessToken: String
)