package com.cafepickuporder.android.data.response

data class MenuOptionGroupResponse(
    val optionGroupId: Long,
    val name: String,
    val required: Boolean,
    val minSelect: Int,
    val maxSelect: Int,
    val options: List<MenuOptionResponse>
)