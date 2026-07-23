package com.cafepickuporder.android.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.R
import com.cafepickuporder.android.data.model.CartItem
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.OrderCreateRequest
import com.cafepickuporder.android.data.request.OrderItemCreateRequest
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange

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
                if (customerId <= 0L) {
                    errorMessage = "로그인 정보가 없습니다. 다시 로그인해 주세요."
                    return@LaunchedEffect
                }

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
            .background(Color(0xFFFFFBF8))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "‹",
                    color = Ink,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "장바구니",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )

            if (cartItems.isNotEmpty()) {
                Surface(
                    color = SoftOrange,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${cartItems.sumOf { it.quantity }}개",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        color = PassOrange,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (cartItems.isEmpty()) {
            EmptyCart(onBackClick = onBackClick)
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "주문할 메뉴",
                        color = Ink,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                items(cartItems, key = { it.cartItemId }) { item ->
                    CartItemCard(item = item)
                }
            }

            CartOrderSummary(
                totalPrice = totalPrice,
                isOrdering = isOrdering,
                errorMessage = errorMessage,
                onOrderClick = {
                    isOrdering = true
                    errorMessage = null
                }
            )
        }
    }
}

@Composable
private fun EmptyCart(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(92.dp),
            shape = CircleShape,
            color = SoftOrange
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.default_menu_coffee),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "장바구니가 비어 있어요",
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = "마음에 드는 메뉴를 담아보세요.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onBackClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PassOrange)
        ) {
            Text(
                text = "메뉴 보러가기",
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(58.dp),
                    color = SoftOrange,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.default_menu_coffee),
                            contentDescription = "${item.menuName} 기본 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(13.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.menuName,
                        color = Ink,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatPrice(item.basePrice + item.optionPrice)} / 1개",
                        color = Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = formatPrice(item.totalPrice),
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (item.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(13.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = PageGray,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                        item.options.forEach { option ->
                            val optionPrice = if (option.additionalPrice == 0) {
                                "추가금 없음"
                            } else {
                                "+${formatPrice(option.additionalPrice)}"
                            }
                            Text(
                                text = "${option.name}  ·  $optionPrice",
                                color = Muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = "기본 옵션",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(15.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(LineGray)
            )
            Spacer(modifier = Modifier.height(13.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.quantity == 1) {
                        "− 버튼을 누르면 메뉴가 삭제돼요"
                    } else {
                        "수량을 변경할 수 있어요"
                    },
                    color = Muted,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f)
                )
                QuantityControl(
                    quantity = item.quantity,
                    onDecrease = { CartManager.decreaseQuantity(item.cartItemId) },
                    onIncrease = { CartManager.increaseQuantity(item.cartItemId) }
                )
            }
        }
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Surface(
        color = PageGray,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            QuantityButton(label = "−", onClick = onDecrease)
            Text(
                text = quantity.toString(),
                color = Ink,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            QuantityButton(label = "+", onClick = onIncrease)
        }
    }
}

@Composable
private fun QuantityButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (label == "+") PassOrange else Ink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CartOrderSummary(
    totalPrice: Int,
    isOrdering: Boolean,
    errorMessage: String?,
    onOrderClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 10.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            if (errorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEEEE),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "주문에 실패했습니다.\n$errorMessage",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 결제 금액",
                    color = Ink,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatPrice(totalPrice),
                    color = PassOrange,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(13.dp))
            Button(
                onClick = onOrderClick,
                enabled = !isOrdering,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PassOrange,
                    disabledContainerColor = Color(0xFFF0D7CB)
                )
            ) {
                Text(
                    text = if (isOrdering) "주문 요청 중..." else "주문하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun formatPrice(value: Int): String {
    return "%,d원".format(value)
}
