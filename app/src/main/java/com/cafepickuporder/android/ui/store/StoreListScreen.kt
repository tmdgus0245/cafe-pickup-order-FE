package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.cafepickuporder.android.data.response.StoreListResponse
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SelectedNavy
import com.cafepickuporder.android.ui.theme.SoftOrange

@Composable
fun StoreListScreen(
    modifier: Modifier = Modifier,
    onStoreClick: (Long) -> Unit
) {
    var stores by remember { mutableStateOf<List<StoreListResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orderMode by remember { mutableStateOf("LIST") }

    LaunchedEffect(Unit) {
        try {
            stores = ApiClient.storeApi.getStores()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HomeHeader(
                selectedMode = orderMode,
                onModeSelected = { orderMode = it }
            )
        }

        item {
            HomeQuickNotice()
        }

        item {
            SectionHeader(
                title = if (orderMode == "MAP") "지도로 주문" else "나와 가까운 매장",
                action = if (orderMode == "MAP") "리스트 보기" else "전체보기",
                onActionClick = {
                    orderMode = if (orderMode == "MAP") "LIST" else orderMode
                }
            )
        }

        item {
            PhoneOrderFilter()
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
                        text = "매장 정보를 불러오지 못했습니다.\n$errorMessage",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            orderMode == "MAP" -> {
                item {
                    MapPlaceholder()
                }
            }

            stores.isEmpty() -> {
                item {
                    Text(
                        text = "등록된 매장이 없습니다.",
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = Muted
                    )
                }
            }

            else -> {
                items(stores) { store ->
                    StoreCard(
                        store = store,
                        onClick = { onStoreClick(store.storeId) }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                color = PageGray
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⌕",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Ink
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "매장 또는 메뉴를 검색해보세요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "현재 위치⌄",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OrderModeTab(
                label = "리스트로 주문",
                selected = selectedMode == "LIST",
                onClick = { onModeSelected("LIST") },
                modifier = Modifier.weight(1f)
            )

            OrderModeTab(
                label = "지도로 주문",
                selected = selectedMode == "MAP",
                onClick = { onModeSelected("MAP") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OrderModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) Ink else Muted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selected) 3.dp else 1.dp)
                .background(if (selected) Ink else LineGray)
        )
    }
}

@Composable
private fun HomeQuickNotice() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        color = SoftOrange
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "빠른 픽업 주문",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "주문 후 매장에서 바로 픽업",
                style = MaterialTheme.typography.bodyMedium,
                color = PassOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$action ›",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun PhoneOrderFilter() {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(PassOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "전화 매장",
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StoreCard(
    store: StoreListResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PageGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "COFFEE",
                    color = Muted,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayStoreStatus(store.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = displayOrderType(store.orderType),
                        style = MaterialTheme.typography.bodySmall,
                        color = PassOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = store.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = store.address ?: "주소 정보 없음",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PageGray
                ) {
                    Text(
                        text = "준비 ${store.averagePreparationMinutes ?: 0}분",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = SelectedNavy,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MapPlaceholder() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(420.dp),
        shape = RoundedCornerShape(16.dp),
        color = PageGray
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "지도 주문은 준비 중입니다.",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "매장 위치 데이터와 지도 SDK가 연결되면 이곳에서 가까운 매장을 지도 위에 보여줄게요.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
        }
    }
}

private fun displayStoreStatus(status: String): String {
    return when (status) {
        "OPEN" -> "영업 중"
        "CLOSED" -> "영업 종료"
        "TEMPORARILY_CLOSED" -> "임시 휴무"
        else -> status
    }
}

private fun displayOrderType(orderType: String): String {
    return when (orderType) {
        "APP" -> "앱 주문"
        "PHONE" -> "전화"
        "BOTH" -> "앱/전화"
        else -> orderType
    }
}
