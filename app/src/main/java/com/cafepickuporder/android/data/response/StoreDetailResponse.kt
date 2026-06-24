package com.cafepickuporder.android.data.response

data class StoreDetailResponse(
    val storeId: Long,
    val name: String,
    val description: String?,
    val address: String?,
    val detailAddress: String?,
    val phone: String?,
    val latitude: Double?,
    val longitude: Double?,
    val openTime: String?,
    val closeTime: String?,
    val status: String,
    val appOrderAvailable: Boolean,
    val dineInAvailable: Boolean,
    val averagePreparationMinutes: Int?
)
