package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.ui.cart.CartManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange

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
    var selectedCategoryId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(storeId) {
        try {
            menus = ApiClient.storeApi.getMenus(storeId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val categories = menus.map { it.categoryId }.distinct()
    val filteredMenus = if (selectedCategoryId == -1L) {
        menus
    } else {
        menus.filter { it.categoryId == selectedCategoryId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MenuTopBar(
            title = "메뉴 선택",
            cartCount = CartManager.cartItems.sumOf { it.quantity },
            onBackClick = onBackClick,
            onCartClick = onCartClick
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                NoticeBox()
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "메뉴 리스트",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SearchPlaceholder()

                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryTabs(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { selectedCategoryId = it }
                    )
                }
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PassOrange)
                        }
                    }
                }

                errorMessage != null -> {
                    item {
                        Text(
                            text = "메뉴 목록을 불러오지 못했습니다.\n$errorMessage",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                filteredMenus.isEmpty() -> {
                    item {
                        Text(
                            text = "등록된 메뉴가 없습니다.",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Muted
                        )
                    }
                }

                else -> {
                    items(filteredMenus) { menu ->
                        MenuCard(
                            menu = menu,
                            onClick = { onMenuClick(storeId, menu.menuId) }
                        )
                    }
                }
            }
        }

        if (CartManager.cartItems.isNotEmpty()) {
            Button(
                onClick = onCartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text("${CartManager.cartItems.sumOf { it.quantity }}개 담긴 장바구니 보기")
            }
        }
    }
}

@Composable
private fun MenuTopBar(
    title: String,
    cartCount: Int,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBackClick) {
            Text("닫기", color = Ink)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onCartClick) {
            Text(
                text = if (cartCount > 0) "장바구니 $cartCount" else "장바구니",
                color = PassOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NoticeBox() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        color = SoftOrange
    ) {
        Text(
            text = "정확한 메뉴 및 가격 정보는 매장 상황에 따라 달라질 수 있습니다.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = PassOrange
        )
    }
}

@Composable
private fun SearchPlaceholder() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PageGray
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⌕",
                style = MaterialTheme.typography.titleLarge,
                color = Muted
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "먹고 싶은 메뉴를 검색해 보세요",
                style = MaterialTheme.typography.bodyLarge,
                color = Muted
            )
        }
    }
}

@Composable
private fun CategoryTabs(
    categories: List<Long>,
    selectedCategoryId: Long,
    onCategorySelected: (Long) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CategoryChip(
            label = "전체",
            selected = selectedCategoryId == -1L,
            onClick = { onCategorySelected(-1L) }
        )

        categories.forEachIndexed { index, categoryId ->
            CategoryChip(
                label = "카테고리 ${index + 1}",
                selected = selectedCategoryId == categoryId,
                onClick = { onCategorySelected(categoryId) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Ink else Color.White,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
            color = if (selected) Color.White else Ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun MenuCard(
    menu: MenuResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = menu.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!menu.description.isNullOrBlank()) {
                Text(
                    text = menu.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = formatPrice(menu.price),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PageGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayMenuStatus(menu.status),
                color = if (menu.status == "ON_SALE") Muted else PassOrange,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 20.dp)
            .background(LineGray)
    )
}

private fun displayMenuStatus(status: String): String {
    return when (status) {
        "ON_SALE" -> "주문 가능"
        "SOLD_OUT" -> "품절"
        "HIDDEN" -> "숨김"
        else -> status
    }
}

private fun formatPrice(value: Int): String {
    return "%,d원".format(value)
}
