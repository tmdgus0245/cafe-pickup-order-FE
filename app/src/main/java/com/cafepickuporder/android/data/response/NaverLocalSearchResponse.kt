package com.cafepickuporder.android.data.response

data class NaverLocalSearchResponse(
    val total: Int,
    val items: List<NaverLocalSearchItem>
)

data class NaverLocalSearchItem(
    val title: String,
    val category: String,
    val telephone: String?,
    val address: String,
    val roadAddress: String,
    val mapx: String,
    val mapy: String
)
