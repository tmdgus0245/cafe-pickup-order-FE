package com.cafepickuporder.android.data.response

data class OrderListResponse(
    val orderId: Long,
    val orderNumber: String,
    val storeName: String,
    val status: String,
    val totalPrice: Int,
    val requestedPickupTime: String?,
    val estimatedPickupTime: String?,
    val createdAt: String
)