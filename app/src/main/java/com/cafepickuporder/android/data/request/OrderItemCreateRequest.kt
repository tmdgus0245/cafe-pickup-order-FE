package com.cafepickuporder.android.data.request

data class OrderItemCreateRequest(
    val menuId: Long,
    val quantity: Int,
    val optionIds: List<Long>
)