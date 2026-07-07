package com.cafepickuporder.android.ui.store

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.data.response.StoreDetailResponse
import com.cafepickuporder.android.data.response.StoreListResponse
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SelectedNavy
import java.util.Locale

private const val LIST_MODE = "LIST"
private const val MAP_MODE = "MAP"

private data class StoreWithDistance(
    val store: StoreListResponse,
    val distanceMeters: Float?
)

private data class MenuSearchResult(
    val store: StoreListResponse,
    val menu: MenuResponse,
    val distanceMeters: Float?
)

@Composable
fun StoreListScreen(
    modifier: Modifier = Modifier,
    onStoreClick: (Long) -> Unit,
    onMenuClick: (Long, Long) -> Unit
) {
    val context = LocalContext.current
    var stores by remember { mutableStateOf<List<StoreListResponse>>(emptyList()) }
    var storeDetails by remember { mutableStateOf<Map<Long, StoreDetailResponse>>(emptyMap()) }
    var menusByStore by remember { mutableStateOf<Map<Long, List<MenuResponse>>>(emptyMap()) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var orderMode by remember { mutableStateOf(LIST_MODE) }
    var showPhoneOnlyStores by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            userLocation = findLastKnownLocation(context)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) {
            userLocation = findLastKnownLocation(context)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        try {
            val loadedStores = ApiClient.storeApi.getStores()
                .filterNot { it.status.equals("INACTIVE", ignoreCase = true) }
            stores = loadedStores

            val details = mutableMapOf<Long, StoreDetailResponse>()
            val menus = mutableMapOf<Long, List<MenuResponse>>()
            loadedStores.forEach { store ->
                runCatching { ApiClient.storeApi.getStoreDetail(store.storeId) }
                    .getOrNull()
                    ?.let { details[store.storeId] = it }
                menus[store.storeId] = runCatching {
                    ApiClient.storeApi.getMenus(store.storeId)
                }.getOrDefault(emptyList())
            }
            storeDetails = details
            menusByStore = menus
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val storesWithDistance = remember(stores, storeDetails, userLocation) {
        stores.map { store ->
            StoreWithDistance(
                store = store,
                distanceMeters = calculateDistance(
                    location = userLocation,
                    detail = storeDetails[store.storeId]
                )
            )
        }.sortedWith(compareBy(nullsLast()) { it.distanceMeters })
    }

    val visibleStores = remember(storesWithDistance, showPhoneOnlyStores) {
        if (showPhoneOnlyStores) {
            storesWithDistance
        } else {
            storesWithDistance.filter { it.store.appOrderAvailable }
        }
    }

    val normalizedQuery = searchQuery.trim()
    val storeSearchResults = remember(normalizedQuery, storesWithDistance) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            storesWithDistance.filter {
                it.store.name.startsWith(normalizedQuery, ignoreCase = true)
            }
        }
    }
    val menuSearchResults = remember(normalizedQuery, storesWithDistance, menusByStore) {
        if (normalizedQuery.isBlank()) {
            emptyList()
        } else {
            storesWithDistance.flatMap { storeWithDistance ->
                menusByStore[storeWithDistance.store.storeId]
                    .orEmpty()
                    .filter { it.name.startsWith(normalizedQuery, ignoreCase = true) }
                    .map {
                        MenuSearchResult(
                            store = storeWithDistance.store,
                            menu = it,
                            distanceMeters = storeWithDistance.distanceMeters
                        )
                    }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SearchField(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        if (normalizedQuery.isBlank()) {
            OrderModeTabs(
                selectedMode = orderMode,
                onModeSelected = { orderMode = it }
            )

            StoreListContent(
                orderMode = orderMode,
                showPhoneOnlyStores = showPhoneOnlyStores,
                onPhoneFilterChange = { showPhoneOnlyStores = it },
                stores = visibleStores,
                allStoresEmpty = stores.isEmpty(),
                isLoading = isLoading,
                errorMessage = errorMessage,
                onListModeClick = { orderMode = LIST_MODE },
                onStoreClick = onStoreClick
            )
        } else {
            SearchResults(
                query = normalizedQuery,
                stores = storeSearchResults,
                menus = menuSearchResults,
                isLoading = isLoading,
                onStoreClick = onStoreClick,
                onMenuClick = onMenuClick
            )
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 12.dp),
        placeholder = {
            Text("매장 또는 메뉴를 검색해보세요", color = Muted)
        },
        leadingIcon = {
            Text("⌕", style = MaterialTheme.typography.headlineSmall, color = Ink)
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PageGray,
            unfocusedContainerColor = PageGray,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = PassOrange
        )
    )
}

@Composable
private fun OrderModeTabs(
    selectedMode: String,
    onModeSelected: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        OrderModeTab(
            label = "리스트로 주문",
            selected = selectedMode == LIST_MODE,
            onClick = { onModeSelected(LIST_MODE) },
            modifier = Modifier.weight(1f)
        )
        OrderModeTab(
            label = "지도로 주문",
            selected = selectedMode == MAP_MODE,
            onClick = { onModeSelected(MAP_MODE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StoreListContent(
    orderMode: String,
    showPhoneOnlyStores: Boolean,
    onPhoneFilterChange: (Boolean) -> Unit,
    stores: List<StoreWithDistance>,
    allStoresEmpty: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onListModeClick: () -> Unit,
    onStoreClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = if (orderMode == MAP_MODE) "지도로 주문" else "나와 가까운 매장",
                action = if (orderMode == MAP_MODE) "리스트 보기" else "전체보기",
                onActionClick = onListModeClick
            )
        }
        item {
            PhoneOrderFilter(
                checked = showPhoneOnlyStores,
                onCheckedChange = onPhoneFilterChange
            )
        }

        when {
            isLoading -> item { LoadingBox() }
            errorMessage != null -> item {
                Text(
                    text = "매장 정보를 불러오지 못했습니다.\n$errorMessage",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            orderMode == MAP_MODE -> item { MapPlaceholder() }
            stores.isEmpty() -> item {
                Text(
                    text = if (allStoresEmpty) {
                        "등록된 매장이 없습니다."
                    } else {
                        "앱 주문이 가능한 매장이 없습니다."
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                    color = Muted
                )
            }
            else -> items(stores, key = { it.store.storeId }) {
                StoreCard(
                    store = it.store,
                    distanceMeters = it.distanceMeters,
                    onClick = { onStoreClick(it.store.storeId) }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun SearchResults(
    query: String,
    stores: List<StoreWithDistance>,
    menus: List<MenuSearchResult>,
    isLoading: Boolean,
    onStoreClick: (Long) -> Unit,
    onMenuClick: (Long, Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (isLoading) {
            item { LoadingBox() }
        } else if (stores.isEmpty() && menus.isEmpty()) {
            item {
                Text(
                    text = "'$query'로 시작하는 매장이나 메뉴가 없습니다.",
                    modifier = Modifier.padding(20.dp),
                    color = Muted
                )
            }
        } else {
            if (stores.isNotEmpty()) {
                items(stores, key = { "store-${it.store.storeId}" }) {
                    StoreSearchRow(
                        store = it.store,
                        onClick = { onStoreClick(it.store.storeId) }
                    )
                }
            }
            if (menus.isNotEmpty()) {
                items(menus, key = { "menu-${it.store.storeId}-${it.menu.menuId}" }) {
                    MenuSearchRow(
                        result = it,
                        onClick = {
                            onMenuClick(it.store.storeId, it.menu.menuId)
                        }
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun StoreSearchRow(
    store: StoreListResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchResultIcon(isStore = true)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = store.name,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MenuSearchRow(
    result: MenuSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchResultIcon(isStore = false)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = result.menu.name,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SearchResultIcon(isStore: Boolean) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx())
        if (isStore) {
            val house = Path().apply {
                moveTo(size.width * 0.14f, size.height * 0.48f)
                lineTo(size.width * 0.5f, size.height * 0.16f)
                lineTo(size.width * 0.86f, size.height * 0.48f)
                lineTo(size.width * 0.78f, size.height * 0.48f)
                lineTo(size.width * 0.78f, size.height * 0.84f)
                lineTo(size.width * 0.22f, size.height * 0.84f)
                lineTo(size.width * 0.22f, size.height * 0.48f)
                close()
            }
            drawPath(path = house, color = Muted, style = stroke)
            drawLine(
                color = Muted,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.44f, size.height * 0.84f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.44f, size.height * 0.62f),
                strokeWidth = 1.8.dp.toPx()
            )
        } else {
            drawRoundRect(
                color = Muted,
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.14f, size.height * 0.32f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.58f, size.height * 0.43f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()),
                style = stroke
            )
            drawArc(
                color = Muted,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.62f, size.height * 0.39f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.25f, size.height * 0.27f),
                style = stroke
            )
            drawLine(
                color = Muted,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.84f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.84f),
                strokeWidth = 1.8.dp.toPx()
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
            .clickable(onClick = onClick)
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
            modifier = Modifier.clickable(onClick = onActionClick)
        )
    }
}

@Composable
private fun PhoneOrderFilter(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "전화 주문 매장",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (checked) {
                    "전화 주문 전용 매장 포함"
                } else {
                    "앱 주문 가능 매장만 표시"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Muted
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PassOrange,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = LineGray,
                uncheckedBorderColor = LineGray
            )
        )
    }
}

@Composable
private fun StoreCard(
    store: StoreListResponse,
    distanceMeters: Float?,
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
            StoreThumbnail()
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                val serviceLabel = displayStoreServiceLabel(store)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayStoreStatus(store.status),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                    if (serviceLabel != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = serviceLabel,
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
                    text = listOfNotNull(
                        formatDistance(distanceMeters),
                        store.address
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(6.dp), color = PageGray) {
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
private fun StoreThumbnail() {
    Box(
        modifier = Modifier
            .size(92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PageGray),
        contentAlignment = Alignment.Center
    ) {
        Text("COFFEE", color = Muted, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LoadingBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = PassOrange)
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
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "지도 주문은 준비 중입니다.",
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

private fun findLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.getProviders(true)
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull { it.time }
}

private fun calculateDistance(
    location: Location?,
    detail: StoreDetailResponse?
): Float? {
    val latitude = detail?.latitude ?: return null
    val longitude = detail.longitude ?: return null
    location ?: return null
    val result = FloatArray(1)
    Location.distanceBetween(
        location.latitude,
        location.longitude,
        latitude,
        longitude,
        result
    )
    return result[0]
}

private fun formatDistance(distanceMeters: Float?): String? {
    distanceMeters ?: return null
    return if (distanceMeters < 1000f) {
        String.format(Locale.KOREA, "%.0fm", distanceMeters)
    } else {
        String.format(Locale.KOREA, "%.1fkm", distanceMeters / 1000f)
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

private fun displayMenuStatus(status: String): String {
    return when (status) {
        "ON_SALE" -> "판매 중"
        "SOLD_OUT" -> "품절"
        "HIDDEN" -> "숨김"
        else -> status
    }
}

private fun formatPrice(value: Int): String {
    return String.format(Locale.KOREA, "%,d원", value)
}
