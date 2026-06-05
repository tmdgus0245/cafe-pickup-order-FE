package com.cafepickuporder.android.data.request

data class SignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String
)