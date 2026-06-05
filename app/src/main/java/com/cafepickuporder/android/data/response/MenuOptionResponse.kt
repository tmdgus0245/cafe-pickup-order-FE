package com.cafepickuporder.android.data.response

data class MenuOptionResponse(
    val optionId: Long,
    val name: String,
    val additionalPrice: Int,
    val displayOrder: Int?
)