package com.cafepickuporder.android.data.response

data class StoreAccountSignupResponse(
    val storeAccountId: Long,
    val storeId: Long,
    val email: String,
    val name: String
)
