package com.cafepickuporder.android.data.response

data class MenuResponse(
        val menuId: Long,
        val categoryId: Long,
        val name: String,
        val description: String?,
        val price: Int,
        val imageUrl: String?,
        val status: String,
        val displayOrder: Int?
)