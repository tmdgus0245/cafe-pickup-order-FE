package com.cafepickuporder.android.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.model.CartItem
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.OrderCreateRequest
import com.cafepickuporder.android.data.request.OrderItemCreateRequest

@Composable
fun CartScreen(
    storeId: Long,
    customerId: Long,
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val cartItems = CartManager.cartItems
    val totalPrice = CartManager.getTotalPrice()
    var isOrdering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isOrdering) {
        if (isOrdering) {
            try {
                val request = OrderCreateRequest(
                    storeId = storeId,
                    requestedPickupTime = null,
                    items = CartManager.cartItems.map { cartItem ->
                        OrderItemCreateRequest(
                            menuId = cartItem.menuId,
                            quantity = cartItem.quantity,
                            optionIds = cartItem.options.map { option ->
                                option.optionId
                            }
                        )
                    }
                )

                ApiClient.orderApi.createOrder(
                    customerId = customerId,
                    request = request
                )

                CartManager.clear()
                onOrderSuccess()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isOrdering = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Text(
                text = "장바구니",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (cartItems.isEmpty()) {
            Text(
                text = "장바구니가 비어 있습니다.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartItems) { item ->
                    CartItemCard(item = item)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "총 결제 금액 ${totalPrice}원",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (errorMessage != null) {
                Text(
                    text = "주문에 실패했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    isOrdering = true
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isOrdering && cartItems.isNotEmpty()
            ) {
                Text(
                    text = if (isOrdering) {
                        "주문 중..."
                    } else {
                        "주문하기"
                    }
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = item.menuName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (item.options.isEmpty()) {
                Text(
                    text = "선택한 옵션 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                item.options.forEach { option ->
                    Text(
                        text = "- ${option.name} +${option.additionalPrice}원",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        CartManager.decreaseQuantity(item.cartItemId)
                    }
                ) {
                    Text("-")
                }

                Text(
                    text = "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = {
                        CartManager.increaseQuantity(item.cartItemId)
                    }
                ) {
                    Text("+")
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "${item.totalPrice}원",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}