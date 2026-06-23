package com.cafepickuporder.android.data.request

data class MenuManageRequest(
    val categoryId: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val imageUrl: String?,
    val status: String,
    val displayOrder: Int?
)
