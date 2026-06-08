package com.cafepickuporder.android.data.response

data class OrderCreateResponse(
    val orderId: Long,
    val orderNumber: String,
    val status: String,
    val totalPrice: Int
)