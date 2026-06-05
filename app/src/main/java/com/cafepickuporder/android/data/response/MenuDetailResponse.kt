package com.cafepickuporder.android.data.response

data class MenuDetailResponse(
        val menuId: Long,
        val storeId: Long,
        val categoryId: Long,
        val name: String,
        val description: String?,
        val price: Int,
        val imageUrl: String?,
        val status: String,
        val optionGroups: List<MenuOptionGroupResponse>
)