package com.cafepickuporder.android.data.response

data class StoreAccountLoginResponse(
    val storeAccountId: Long,
    val storeId: Long,
    val email: String,
    val name: String,
    val token: String
)
