package com.cafepickuporder.android.data.response

data class StoreListResponse(
        val storeId: Long,
        val name: String,
        val description: String?,
        val address: String?,
        val status: String,
        val orderType: String,
        val averagePreparationMinutes: Int?
)