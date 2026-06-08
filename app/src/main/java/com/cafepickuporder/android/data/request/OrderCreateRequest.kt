package com.cafepickuporder.android.data.request

data class OrderCreateRequest(
    val storeId: Long,
    val requestedPickupTime: String?,
    val items: List<OrderItemCreateRequest>
)