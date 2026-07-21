package com.cafepickuporder.android.data.request

data class StoreAccountSignupRequest(
    val email: String,
    val password: String,
    val name: String,
    val storeName: String,
    val storeDescription: String?,
    val storeAddress: String,
    val storeDetailAddress: String?,
    val storePhone: String,
    val latitude: Double?,
    val longitude: Double?,
    val openTime: String,
    val closeTime: String,
    val appOrderAvailable: Boolean,
    val dineInAvailable: Boolean,
    val averagePreparationMinutes: Int
)
