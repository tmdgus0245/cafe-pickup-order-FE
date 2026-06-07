package com.cafepickuporder.android.data.model

data class CartItem(
    val cartItemId: Long = System.currentTimeMillis(),
    val storeId: Long,
    val menuId: Long,
    val menuName: String,
    val basePrice: Int,
    val options: List<CartOption>,
    val quantity: Int = 1
) {
    val optionPrice: Int
        get() = options.sumOf { it.additionalPrice }

    val totalPrice: Int
        get() = (basePrice + optionPrice) * quantity
}