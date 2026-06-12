package com.cafepickuporder.android.data.response

data class StoreOrderResponse(
    val orderId: Long,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val status: String,
    val totalPrice: Int,
    val requestedPickupTime: String?,
    val estimatedPickupTime: String?,
    val createdAt: String
)
