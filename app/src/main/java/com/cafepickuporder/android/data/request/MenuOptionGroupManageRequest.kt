package com.cafepickuporder.android.data.request

data class MenuOptionGroupManageRequest(
    val name: String,
    val required: Boolean,
    val minSelect: Int?,
    val maxSelect: Int?
)
