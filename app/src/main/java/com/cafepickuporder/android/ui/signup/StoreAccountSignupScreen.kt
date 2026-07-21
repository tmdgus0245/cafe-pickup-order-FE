package com.cafepickuporder.android.ui.signup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.cafepickuporder.android.BuildConfig
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.remote.NaverLocalSearchClient
import com.cafepickuporder.android.data.request.StoreAccountSignupRequest
import com.cafepickuporder.android.data.response.NaverLocalSearchItem
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PassOrange
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

private val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val phonePattern = Regex("^0\\d{1,2}-?\\d{3,4}-?\\d{4}$")
private val timePattern = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
private const val SYMBOL_SEARCH_MATCH_RADIUS_METERS = 50f

@Composable
fun StoreAccountSignupScreen(
    onMoveToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var storeDescription by remember { mutableStateOf("") }
    var storeAddress by remember { mutableStateOf("") }
    var storeDetailAddress by remember { mutableStateOf("") }
    var storePhone by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var openTime by remember { mutableStateOf("09:00") }
    var closeTime by remember { mutableStateOf("18:00") }
    var appOrderAvailable by remember { mutableStateOf(true) }
    var dineInAvailable by remember { mutableStateOf(false) }
    var averagePreparationMinutes by remember { mutableStateOf("10") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = onMoveToLogin,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text("<", color = Ink, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "사장님 회원가입",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle("매장 찾기")
        Text(
            text = "매장을 선택하면 가입 정보에 자동으로 채워져요.",
            color = Muted,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        NaverCafeMapPicker { selectedName, selectedLatitude, selectedLongitude, selectedAddress, selectedPhone ->
            storeName = selectedName
            latitude = formatCoordinate(selectedLatitude)
            longitude = formatCoordinate(selectedLongitude)
            if (selectedAddress.isNotBlank()) {
                storeAddress = selectedAddress
            } else {
                storeAddress = ""
            }
            if (selectedPhone.isNotBlank()) {
                storePhone = selectedPhone
            } else {
                storePhone = ""
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("계정 정보")
        SignupField("이메일", email) { email = it }
        SignupField("비밀번호", password, isPassword = true) { password = it }
        SignupField("사장님 이름", ownerName) { ownerName = it }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("매장 정보")
        SignupField("매장명", storeName) { storeName = it }
        SignupField("매장 설명", storeDescription) { storeDescription = it }
        SignupField("매장 주소", storeAddress) { storeAddress = it }
        SignupField("상세 주소", storeDetailAddress) { storeDetailAddress = it }
        SignupField("매장 전화번호", storePhone) { storePhone = it }

        Spacer(modifier = Modifier.height(20.dp))

        SectionTitle("운영 정보")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SignupField(
                label = "오픈 시간",
                value = openTime,
                modifier = Modifier.weight(1f),
                onValueChange = { openTime = it }
            )
            SignupField(
                label = "마감 시간",
                value = closeTime,
                modifier = Modifier.weight(1f),
                onValueChange = { closeTime = it }
            )
        }
        SignupField("평균 준비 시간(분)", averagePreparationMinutes) {
            averagePreparationMinutes = it
        }

        ToggleRow(
            label = "앱 주문 가능",
            checked = appOrderAvailable,
            onCheckedChange = { appOrderAvailable = it }
        )
        ToggleRow(
            label = "매장 식사 가능",
            checked = dineInAvailable,
            onCheckedChange = { dineInAvailable = it }
        )

        Spacer(modifier = Modifier.height(22.dp))

        Button(
            onClick = {
                val validationMessage = validateStoreSignup(
                    email = email,
                    password = password,
                    ownerName = ownerName,
                    storeName = storeName,
                    storeAddress = storeAddress,
                    storePhone = storePhone,
                    openTime = openTime,
                    closeTime = closeTime,
                    averagePreparationMinutes = averagePreparationMinutes,
                    latitude = latitude,
                    longitude = longitude
                )

                if (validationMessage != null) {
                    message = validationMessage
                    return@Button
                }

                coroutineScope.launch {
                    isLoading = true
                    message = ""

                    try {
                        val response = ApiClient.storeAccountApi.signup(
                            StoreAccountSignupRequest(
                                email = email.trim(),
                                password = password,
                                name = ownerName.trim(),
                                storeName = storeName.trim(),
                                storeDescription = storeDescription.trim().ifBlank { null },
                                storeAddress = storeAddress.trim(),
                                storeDetailAddress = storeDetailAddress.trim().ifBlank { null },
                                storePhone = storePhone.trim(),
                                latitude = latitude.trim().toDoubleOrNull(),
                                longitude = longitude.trim().toDoubleOrNull(),
                                openTime = normalizeTime(openTime),
                                closeTime = normalizeTime(closeTime),
                                appOrderAvailable = appOrderAvailable,
                                dineInAvailable = dineInAvailable,
                                averagePreparationMinutes = averagePreparationMinutes.trim().toInt()
                            )
                        )

                        if (response.isSuccessful) {
                            message = "사장님 회원가입이 완료됐습니다. 로그인해 주세요."
                        } else {
                            message = "사장님 회원가입 실패: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        message = "서버 연결 실패: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "가입 중..." else "사장님 회원가입")
        }

        TextButton(
            onClick = onMoveToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이미 사장님 계정이 있나요? 로그인")
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = if (message.contains("완료")) PassOrange else MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun NaverCafeMapPicker(
    onCafeSelected: (String, Double, Double, String, String) -> Unit
) {
    if (BuildConfig.NAVER_MAP_NCP_KEY_ID.isBlank()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFF0EA)
        ) {
            Text(
                text = "네이버 지도 키가 필요해요. local.properties에 NAVER_MAP_NCP_KEY_ID를 추가하면 지도가 표시됩니다.",
                modifier = Modifier.padding(14.dp),
                color = PassOrange,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    val context = LocalContext.current
    val latestOnCafeSelected by rememberUpdatedState(onCafeSelected)
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var userLocationKeywords by remember { mutableStateOf<List<String>>(emptyList()) }
    var hasMovedToInitialLocation by remember { mutableStateOf(false) }
    var selectedCaption by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NaverLocalSearchItem>>(emptyList()) }
    var searchMessage by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var pendingSymbolSelection by remember { mutableStateOf<MapSymbolSelection?>(null) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            userLocation = findLastKnownLocation(context)
        }
    }
    val mapView = remember {
        MapView(context).apply {
            onCreate(Bundle())
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
                }
                false
            }
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
    }

    LaunchedEffect(naverMap, userLocation) {
        val map = naverMap ?: return@LaunchedEffect
        if (hasMovedToInitialLocation) return@LaunchedEffect

        val location = userLocation
        val target = if (location != null) {
            map.locationOverlay.isVisible = true
            map.locationOverlay.position = LatLng(location.latitude, location.longitude)
            LatLng(location.latitude, location.longitude)
        } else {
            LatLng(37.5665, 126.9780)
        }

        map.moveCamera(CameraUpdate.scrollTo(target))
        hasMovedToInitialLocation = true
    }

    LaunchedEffect(userLocation) {
        userLocationKeywords = resolveLocationKeywords(context, userLocation)
    }

    LaunchedEffect(searchQuery, userLocation, userLocationKeywords) {
        val query = searchQuery.trim()
        searchResults = emptyList()
        searchMessage = ""

        if (query.length < 2) return@LaunchedEffect

        if (BuildConfig.NAVER_SEARCH_CLIENT_ID.isBlank() ||
            BuildConfig.NAVER_SEARCH_CLIENT_SECRET.isBlank()
        ) {
            searchMessage = "검색 API 키를 설정하면 매장 목록이 표시됩니다."
            return@LaunchedEffect
        }

        delay(350)
        isSearching = true

        try {
            val searchOutcome = searchNaverLocalItems(
                queries = buildLocalCafeSearchQueries(
                    locationKeywords = userLocationKeywords,
                    keyword = query
                )
            )

            if (searchOutcome.isSuccessful) {
                searchResults = searchOutcome.items
                    .filter { it.latitude != null && it.longitude != null }
                    .filter { it.isCafeResult }
                    .distinctBy { it.searchIdentity }
                    .sortedBy { item ->
                        distanceMeters(
                            from = userLocation,
                            latitude = item.latitude,
                            longitude = item.longitude
                        ) ?: Float.MAX_VALUE
                    }
                    .take(12)
                searchMessage = if (searchResults.isEmpty()) {
                    "검색 결과가 없습니다."
                } else {
                    ""
                }
            } else {
                searchMessage = when (searchOutcome.errorCode) {
                    401 -> "검색 API Client ID와 Secret을 확인해 주세요."
                    403 -> "네이버 개발자 센터에서 검색 API 사용 설정을 확인해 주세요."
                    else -> searchOutcome.errorMessage ?: "검색 실패"
                }
            }
        } catch (e: Exception) {
            searchMessage = "검색 실패: ${e.message}"
        } finally {
            isSearching = false
        }
    }

    LaunchedEffect(pendingSymbolSelection) {
        val selection = pendingSymbolSelection ?: return@LaunchedEffect

        if (BuildConfig.NAVER_SEARCH_CLIENT_ID.isBlank() ||
            BuildConfig.NAVER_SEARCH_CLIENT_SECRET.isBlank()
        ) {
            latestOnCafeSelected(
                selection.name,
                selection.latitude,
                selection.longitude,
                "",
                ""
            )
            return@LaunchedEffect
        }

        try {
            val clickedLocationKeywords = resolveLocationKeywords(
                context = context,
                latitude = selection.latitude,
                longitude = selection.longitude
            )
            val symbolSearchOutcome = searchNaverLocalItems(
                queries = buildLocalCafeSearchQueries(
                    locationKeywords = clickedLocationKeywords.ifEmpty { userLocationKeywords },
                    keyword = selection.name
                )
            )

            val bestMatchWithDistance = if (symbolSearchOutcome.isSuccessful) {
                symbolSearchOutcome.items
                    .filter { it.latitude != null && it.longitude != null }
                    .filter { it.isCafeResult }
                    .mapNotNull { item ->
                        val distance = distanceBetweenMeters(
                            fromLatitude = selection.latitude,
                            fromLongitude = selection.longitude,
                            toLatitude = item.latitude,
                            toLongitude = item.longitude
                        ) ?: return@mapNotNull null
                        item to distance
                    }
                    .minByOrNull { it.second }
            } else {
                null
            }
            val bestMatch = bestMatchWithDistance
                ?.takeIf { it.second <= SYMBOL_SEARCH_MATCH_RADIUS_METERS }
                ?.first

            if (bestMatch != null) {
                val address = bestMatch.roadAddress.ifBlank { bestMatch.address }
                val phone = bestMatch.telephone.orEmpty()
                latestOnCafeSelected(
                    selection.name,
                    selection.latitude,
                    selection.longitude,
                    address,
                    phone
                )
            } else {
                latestOnCafeSelected(
                    selection.name,
                    selection.latitude,
                    selection.longitude,
                    "",
                    ""
                )
            }
        } catch (_: Exception) {
            latestOnCafeSelected(
                selection.name,
                selection.latitude,
                selection.longitude,
                "",
                ""
            )
        }
    }

    DisposableEffect(mapView) {
        mapView.onStart()
        mapView.onResume()

        mapView.getMapAsync { map ->
            naverMap = map
            map.uiSettings.isZoomControlEnabled = false
            map.uiSettings.isLocationButtonEnabled = true
            map.setOnSymbolClickListener { symbol ->
                val caption = symbol.caption.trim()
                if (caption.isBlank()) {
                    false
                } else {
                    val position = symbol.position
                    selectedCaption = caption
                    selectedMarker = showSelectedMarker(
                        map = map,
                        previousMarker = selectedMarker,
                        caption = caption,
                        position = position
                    )
                    map.moveCamera(CameraUpdate.scrollTo(position))
                    pendingSymbolSelection = MapSymbolSelection(
                        name = caption,
                        latitude = position.latitude,
                        longitude = position.longitude
                    )
                    true
                }
            }
        }

        onDispose {
            selectedMarker?.map = null
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF7F7F7)
    ) {
        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("가게 이름 검색") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = PassOrange,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .height(54.dp)
            )

            if (isSearching || searchResults.isNotEmpty() || searchMessage.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .height(286.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isSearching) {
                        SearchStateText("검색 중...")
                    }

                    searchResults.forEach { item ->
                        LocalSearchResultRow(
                            item = item,
                            distance = distanceMeters(
                                from = userLocation,
                                latitude = item.latitude,
                                longitude = item.longitude
                            ),
                            onClick = {
                                val latitude = item.latitude ?: return@LocalSearchResultRow
                                val longitude = item.longitude ?: return@LocalSearchResultRow
                                val name = item.cleanTitle
                                val address = item.roadAddress.ifBlank { item.address }
                                val phone = item.telephone.orEmpty()
                                val position = LatLng(latitude, longitude)
                                val map = naverMap

                                selectedCaption = name
                                if (map != null) {
                                    selectedMarker = showSelectedMarker(
                                        map = map,
                                        previousMarker = selectedMarker,
                                        caption = name,
                                        position = position
                                    )
                                    map.moveCamera(CameraUpdate.scrollTo(position))
                                }
                                latestOnCafeSelected(name, latitude, longitude, address, phone)
                            }
                        )
                    }

                    if (searchMessage.isNotBlank()) {
                        SearchStateText(searchMessage)
                    }
                }
            }

            Box {
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                )

                if (selectedCaption.isNotBlank()) {
                    Text(
                        text = "선택한 매장: $selectedCaption",
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .background(Color.White, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = PassOrange,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalSearchResultRow(
    item: NaverLocalSearchItem,
    distance: Float?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = item.cleanTitle,
                color = Ink,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            val address = item.roadAddress.ifBlank { item.address }
            val meta = listOfNotNull(
                formatSearchDistance(distance),
                address.ifBlank { null }
            ).joinToString(" · ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SearchStateText(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 8.dp),
        color = Muted,
        style = MaterialTheme.typography.bodySmall
    )
}

private fun showSelectedMarker(
    map: NaverMap,
    previousMarker: Marker?,
    caption: String,
    position: LatLng
): Marker {
    previousMarker?.map = null
    return Marker(position).apply {
        captionText = caption
        captionColor = AndroidColor.rgb(255, 107, 72)
        iconTintColor = AndroidColor.rgb(255, 107, 72)
        width = 72
        height = 96
        zIndex = 100
        this.map = map
    }
}

private val NaverLocalSearchItem.cleanTitle: String
    get() = title
        .replace("<b>", "")
        .replace("</b>", "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .trim()

private val NaverLocalSearchItem.longitude: Double?
    get() = mapx.toDoubleOrNull()?.div(10_000_000.0)

private val NaverLocalSearchItem.latitude: Double?
    get() = mapy.toDoubleOrNull()?.div(10_000_000.0)

private val NaverLocalSearchItem.searchIdentity: String
    get() = listOf(cleanTitle, roadAddress, address, mapx, mapy)
        .joinToString("|")

private val NaverLocalSearchItem.isCafeResult: Boolean
    get() {
        val text = "${cleanTitle} $category".lowercase(Locale.KOREA)
        return listOf(
            "카페",
            "커피",
            "디저트",
            "coffee",
            "cafe",
            "베이커리"
        ).any { text.contains(it) }
    }

private data class MapSymbolSelection(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

private data class NaverLocalSearchOutcome(
    val items: List<NaverLocalSearchItem> = emptyList(),
    val errorCode: Int? = null,
    val errorMessage: String? = null
) {
    val isSuccessful: Boolean
        get() = errorCode == null
}

private suspend fun searchNaverLocalItems(
    queries: List<String>
): NaverLocalSearchOutcome {
    val mergedItems = mutableListOf<NaverLocalSearchItem>()
    var firstErrorCode: Int? = null
    var firstErrorMessage: String? = null

    queries
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .forEach { query ->
            val response = NaverLocalSearchClient.api.searchLocal(
                clientId = BuildConfig.NAVER_SEARCH_CLIENT_ID,
                clientSecret = BuildConfig.NAVER_SEARCH_CLIENT_SECRET,
                query = query,
                display = 5
            )

            if (response.isSuccessful) {
                mergedItems += response.body()?.items.orEmpty()
            } else {
                if (firstErrorCode == null) {
                    firstErrorCode = response.code()
                    firstErrorMessage = "검색 실패: ${response.code()}"
                }
                if (response.code() == 401 || response.code() == 403) {
                    return NaverLocalSearchOutcome(
                        errorCode = response.code(),
                        errorMessage = firstErrorMessage
                    )
                }
            }
        }

    return if (mergedItems.isNotEmpty()) {
        NaverLocalSearchOutcome(items = mergedItems)
    } else {
        NaverLocalSearchOutcome(
            errorCode = firstErrorCode,
            errorMessage = firstErrorMessage
        )
    }
}

private fun distanceMeters(
    from: Location?,
    latitude: Double?,
    longitude: Double?
): Float? {
    from ?: return null
    latitude ?: return null
    longitude ?: return null
    val result = FloatArray(1)
    Location.distanceBetween(
        from.latitude,
        from.longitude,
        latitude,
        longitude,
        result
    )
    return result[0]
}

private fun distanceBetweenMeters(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double?,
    toLongitude: Double?
): Float? {
    toLatitude ?: return null
    toLongitude ?: return null
    val result = FloatArray(1)
    Location.distanceBetween(
        fromLatitude,
        fromLongitude,
        toLatitude,
        toLongitude,
        result
    )
    return result[0]
}

private fun formatSearchDistance(distance: Float?): String? {
    distance ?: return null
    return if (distance < 1000f) {
        String.format(Locale.KOREA, "%.0fm", distance)
    } else {
        String.format(Locale.KOREA, "%.1fkm", distance / 1000f)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = Ink,
        fontWeight = FontWeight.ExtraBold
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SignupField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    isPassword: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        modifier = modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Ink
        )
    }
}

private fun validateStoreSignup(
    email: String,
    password: String,
    ownerName: String,
    storeName: String,
    storeAddress: String,
    storePhone: String,
    openTime: String,
    closeTime: String,
    averagePreparationMinutes: String,
    latitude: String,
    longitude: String
): String? {
    if (!emailPattern.matches(email.trim())) return "이메일 형식에 맞게 입력해 주세요."
    if (password.length < 4) return "비밀번호는 4자 이상 입력해 주세요."
    if (ownerName.isBlank()) return "사장님 이름을 입력해 주세요."
    if (storeName.isBlank()) return "매장명을 입력해 주세요."
    if (storeAddress.isBlank()) return "매장 주소를 입력해 주세요."
    if (!phonePattern.matches(storePhone.trim())) return "매장 전화번호 형식에 맞게 입력해 주세요."
    if (!timePattern.matches(openTime.trim())) return "오픈 시간은 HH:mm 형식으로 입력해 주세요."
    if (!timePattern.matches(closeTime.trim())) return "마감 시간은 HH:mm 형식으로 입력해 주세요."
    if (averagePreparationMinutes.trim().toIntOrNull() == null) {
        return "평균 준비 시간은 숫자로 입력해 주세요."
    }
    if (latitude.isNotBlank() && latitude.trim().toDoubleOrNull() == null) {
        return "위도는 숫자로 입력해 주세요."
    }
    if (longitude.isNotBlank() && longitude.trim().toDoubleOrNull() == null) {
        return "경도는 숫자로 입력해 주세요."
    }
    return null
}

private fun normalizeTime(value: String): String {
    return value.trim()
}

private fun formatCoordinate(value: Double): String {
    return String.format(Locale.US, "%.7f", value)
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

private suspend fun resolveLocationKeywords(
    context: Context,
    location: Location?
): List<String> {
    location ?: return emptyList()
    return resolveLocationKeywords(
        context = context,
        latitude = location.latitude,
        longitude = location.longitude
    )
}

private suspend fun resolveLocationKeywords(
    context: Context,
    latitude: Double,
    longitude: Double
): List<String> {
    return withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            val address = Geocoder(context, Locale.KOREA)
                .getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()

            listOfNotNull(
                address?.subLocality,
                address?.subAdminArea,
                address?.locality
            )
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
        }.getOrDefault(emptyList())
    }
}

private fun buildLocalCafeSearchQueries(
    locationKeywords: List<String>,
    keyword: String
): List<String> {
    val trimmedKeyword = keyword.trim()
    if (trimmedKeyword.isBlank()) return emptyList()

    val cafeKeyword = if (trimmedKeyword.contains("카페")) {
        trimmedKeyword
    } else {
        "$trimmedKeyword 카페"
    }
    return (
        locationKeywords.flatMap { locationKeyword ->
            listOf(
                "$locationKeyword $trimmedKeyword",
                "$locationKeyword $cafeKeyword"
            )
        } +
            listOf(
                trimmedKeyword,
                cafeKeyword
            )
        )
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
}
