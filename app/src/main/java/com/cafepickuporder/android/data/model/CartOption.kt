package com.cafepickuporder.android.data.model

data class CartOption(
    val optionGroupId: Long,
    val optionId: Long,
    val name: String,
    val additionalPrice: Int
)