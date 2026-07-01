package com.cafepickuporder.android.data.request

data class CustomerProfileUpdateRequest(
    val name: String,
    val email: String,
    val profileImageUrl: String?
)
