package com.cafepickuporder.android.ui.cart

import androidx.compose.runtime.mutableStateListOf
import com.cafepickuporder.android.data.model.CartItem

object CartManager {

    val cartItems = mutableStateListOf<CartItem>()

    fun addItem(item: CartItem) {
        cartItems.add(item)
    }

    fun increaseQuantity(cartItemId: Long) {
        val index = cartItems.indexOfFirst { it.cartItemId == cartItemId }
        if (index != -1) {
            val item = cartItems[index]
            cartItems[index] = item.copy(quantity = item.quantity + 1)
        }
    }

    fun decreaseQuantity(cartItemId: Long) {
        val index = cartItems.indexOfFirst { it.cartItemId == cartItemId }
        if (index != -1) {
            val item = cartItems[index]

            if (item.quantity <= 1) {
                cartItems.removeAt(index)
            } else {
                cartItems[index] = item.copy(quantity = item.quantity - 1)
            }
        }
    }

    fun clear() {
        cartItems.clear()
    }

    fun getTotalPrice(): Int {
        return cartItems.sumOf { it.totalPrice }
    }
}