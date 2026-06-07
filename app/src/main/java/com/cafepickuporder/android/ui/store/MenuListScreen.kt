package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.ui.cart.CartManager

@Composable
fun MenuListScreen(
    storeId: Long,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: (storeId: Long, menuId: Long) -> Unit
) {
    var menus by remember { mutableStateOf<List<MenuResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storeId) {
        try {
            menus = ApiClient.storeApi.getMenus(storeId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
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

            Spacer(modifier = Modifier.width(4.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "메뉴 선택",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "주문할 메뉴를 선택해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = onCartClick
            ) {
                val cartCount = CartManager.cartItems.sumOf { it.quantity }

                Text(
                    text = if (cartCount > 0) {
                        "장바구니 $cartCount"
                    } else {
                        "장바구니"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = "메뉴 목록을 불러오지 못했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            menus.isEmpty() -> {
                Text("등록된 메뉴가 없습니다.")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(menus) { menu ->
                        MenuCard(
                            menu = menu,
                            onClick = {
                                onMenuClick(storeId, menu.menuId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuCard(
    menu: MenuResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = menu.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = menu.description ?: "메뉴 설명이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "${menu.price}원",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AssistChip(
                onClick = {},
                label = {
                    Text(displayMenuStatus(menu.status))
                }
            )
        }
    }
}

private fun displayMenuStatus(status: String): String {
    return when (status) {
        "ON_SALE" -> "판매중"
        "SOLD_OUT" -> "품절"
        "HIDDEN" -> "숨김"
        else -> status
    }
}