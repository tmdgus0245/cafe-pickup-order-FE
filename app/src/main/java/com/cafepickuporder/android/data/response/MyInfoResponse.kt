package com.cafepickuporder.android.data.response

data class MyInfoResponse(
    val customerId: Long,
    val email: String,
    val name: String,
    val phone: String?,
    val profileImageUrl: String?
)
