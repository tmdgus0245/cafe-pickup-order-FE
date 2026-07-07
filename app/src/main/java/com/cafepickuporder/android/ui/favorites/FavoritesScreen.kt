package com.cafepickuporder.android.ui.favorites

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.response.StoreListResponse
import com.cafepickuporder.android.local.TokenManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SelectedNavy

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    onStoreClick: (Long) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadError = runCatching {
            FavoriteStoreManager.refresh(tokenManager.getAccessToken().orEmpty())
        }.isFailure
        isLoading = false
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 20.dp, end = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "자주가요",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        when {
            isLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PassOrange)
                }
            }

            loadError -> item {
                Text(
                    text = "자주 가는 매장을 불러오지 못했습니다.",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Muted
                )
            }

            FavoriteStoreManager.stores.isEmpty() -> item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "아직 등록한 매장이 없습니다.",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "매장 메뉴 화면에서 자주가는 매장 버튼을 눌러 등록해보세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted
                    )
                }
            }

            else -> items(
                items = FavoriteStoreManager.stores,
                key = { it.storeId }
            ) { store ->
                FavoriteStoreCard(
                    store = store,
                    onClick = { onStoreClick(store.storeId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FavoriteStoreCard(
    store: StoreListResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clickable(onClick = onClick),
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
                    .size(92.dp)
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

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayStoreStatus(store.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )

                    displayStoreServiceLabel(store)?.let { label ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = PassOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

private fun displayStoreStatus(status: String): String {
    return when (status) {
        "OPEN" -> "영업 중"
        "CLOSED" -> "영업 종료"
        "TEMPORARILY_CLOSED" -> "임시 휴무"
        else -> status
    }
}

private fun displayStoreServiceLabel(store: StoreListResponse): String? {
    return when {
        !store.appOrderAvailable -> "전화 ☎"
        store.dineInAvailable -> "먹고가요 🍴"
        else -> null
    }
}
