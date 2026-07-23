package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.cafepickuporder.android.R
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuCategoryResponse
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.ui.cart.CartManager
import com.cafepickuporder.android.ui.favorites.FavoriteStoreManager
import com.cafepickuporder.android.local.TokenManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange
import kotlinx.coroutines.launch

@Composable
fun MenuListScreen(
    storeId: Long,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: (storeId: Long, menuId: Long) -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var categories by remember { mutableStateOf<List<MenuCategoryResponse>>(emptyList()) }
    var menus by remember { mutableStateOf<List<MenuResponse>>(emptyList()) }
    var storeName by remember { mutableStateOf("매장") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(storeId) {
        isLoading = true
        errorMessage = null

        try {
            runCatching {
                FavoriteStoreManager.refresh(tokenManager.getAccessToken().orEmpty())
            }
            storeName = runCatching {
                ApiClient.storeApi.getStoreDetail(storeId).name
            }.getOrDefault("매장")
            menus = ApiClient.storeApi.getMenus(storeId)
            categories = try {
                ApiClient.storeApi.getCategories(storeId)
            } catch (_: Exception) {
                emptyList()
            }
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    val orderedCategories = remember(categories, menus) {
        val categoryIds = categories.map { it.categoryId }.toSet()
        val fallbackCategories = menus
            .map { it.categoryId }
            .distinct()
            .filterNot { it in categoryIds }
            .mapIndexed { index, categoryId ->
                MenuCategoryResponse(
                    categoryId = categoryId,
                    name = "카테고리 ${categories.size + index + 1}",
                    displayOrder = categories.size + index
                )
            }

        (categories + fallbackCategories).sortedBy { it.displayOrder ?: Int.MAX_VALUE }
    }
    val menuSections = remember(orderedCategories, menus) {
        orderedCategories.mapNotNull { category ->
            val sectionMenus = menus
                .filter { it.categoryId == category.categoryId }
                .sortedBy { it.displayOrder ?: Int.MAX_VALUE }

            if (sectionMenus.isEmpty()) null else MenuSection(category, sectionMenus)
        }
    }
    val sectionStartIndexes = remember(menuSections) {
        buildSectionStartIndexes(menuSections)
    }

    LaunchedEffect(menuSections) {
        if (selectedCategoryId == null) {
            selectedCategoryId = menuSections.firstOrNull()?.category?.categoryId
        }
    }

    LaunchedEffect(listState.firstVisibleItemIndex, menuSections) {
        val visibleCategoryId = findVisibleCategoryId(
            firstVisibleItemIndex = listState.firstVisibleItemIndex,
            sections = menuSections,
            sectionStartIndexes = sectionStartIndexes
        )

        if (visibleCategoryId != null && visibleCategoryId != selectedCategoryId) {
            selectedCategoryId = visibleCategoryId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MenuTopBar(
            title = storeName,
            isFavorite = FavoriteStoreManager.contains(storeId),
            onBackClick = onBackClick,
            onFavoriteClick = {
                coroutineScope.launch {
                    runCatching {
                        FavoriteStoreManager.toggle(
                            accessToken = tokenManager.getAccessToken().orEmpty(),
                            storeId = storeId
                        )
                    }
                }
            }
        )

        NoticeBox()

        Spacer(modifier = Modifier.height(18.dp))

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "메뉴 리스트",
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            CategoryTabs(
                categories = menuSections.map { it.category },
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { categoryId ->
                    selectedCategoryId = categoryId
                    sectionStartIndexes[categoryId]?.let { targetIndex ->
                        coroutineScope.launch {
                            listState.animateScrollToItem(targetIndex)
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
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

                menuSections.isEmpty() -> {
                    item {
                        Text(
                            text = "등록된 메뉴가 없습니다.",
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = Muted
                        )
                    }
                }

                else -> {
                    menuSections.forEach { section ->
                        item(key = "category-${section.category.categoryId}") {
                            CategorySectionTitle(title = section.category.name)
                        }

                        items(section.menus, key = { it.menuId }) { menu ->
                            MenuCard(
                                menu = menu,
                                onClick = { onMenuClick(storeId, menu.menuId) }
                            )
                        }
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
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 4.dp, top = 8.dp, end = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBackClick) {
            Text(
                text = "<",
                color = Ink,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        Surface(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onFavoriteClick),
            shape = CircleShape,
            color = if (isFavorite) SoftOrange else Color(0xFFFFF8F4),
            border = BorderStroke(
                width = 1.dp,
                color = if (isFavorite) Color.Transparent else Color(0xFFFFD8C8)
            )
        ) {
            Box(contentAlignment = Alignment.Center) {
                FavoriteHeartIcon(selected = isFavorite)
            }
        }
    }
}

@Composable
private fun FavoriteHeartIcon(selected: Boolean) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val heart = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.88f)
            cubicTo(
                size.width * 0.20f,
                size.height * 0.68f,
                size.width * 0.05f,
                size.height * 0.47f,
                size.width * 0.16f,
                size.height * 0.27f
            )
            cubicTo(
                size.width * 0.27f,
                size.height * 0.08f,
                size.width * 0.44f,
                size.height * 0.12f,
                size.width * 0.50f,
                size.height * 0.28f
            )
            cubicTo(
                size.width * 0.56f,
                size.height * 0.12f,
                size.width * 0.73f,
                size.height * 0.08f,
                size.width * 0.84f,
                size.height * 0.27f
            )
            cubicTo(
                size.width * 0.95f,
                size.height * 0.47f,
                size.width * 0.80f,
                size.height * 0.68f,
                size.width * 0.50f,
                size.height * 0.88f
            )
            close()
        }

        if (selected) {
            drawPath(path = heart, color = PassOrange)
        } else {
            drawPath(
                path = heart,
                color = PassOrange,
                style = Stroke(width = 2.dp.toPx())
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
    categories: List<MenuCategoryResponse>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit
) {
    val categoryListState = rememberLazyListState()

    LaunchedEffect(selectedCategoryId, categories) {
        val selectedIndex = categories.indexOfFirst { it.categoryId == selectedCategoryId }
        if (selectedIndex >= 0) {
            categoryListState.animateScrollToItem(selectedIndex)
        }
    }

    LazyRow(
        state = categoryListState,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(categories, key = { _, category -> category.categoryId }) { _, category ->
            CategoryChip(
                label = category.name,
                selected = selectedCategoryId == category.categoryId,
                onClick = { onCategorySelected(category.categoryId) }
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
private fun CategorySectionTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
        style = MaterialTheme.typography.titleLarge,
        color = Ink,
        fontWeight = FontWeight.ExtraBold
    )
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
            Image(
                painter = painterResource(R.drawable.default_menu_coffee),
                contentDescription = "${menu.name} 기본 이미지",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (menu.status != "ON_SALE") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.78f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayMenuStatus(menu.status),
                        color = PassOrange,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
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

private data class MenuSection(
    val category: MenuCategoryResponse,
    val menus: List<MenuResponse>
)

private fun buildSectionStartIndexes(sections: List<MenuSection>): Map<Long, Int> {
    var index = 0
    return sections.associate { section ->
        val startIndex = index
        index += 1 + section.menus.size
        section.category.categoryId to startIndex
    }
}

private fun findVisibleCategoryId(
    firstVisibleItemIndex: Int,
    sections: List<MenuSection>,
    sectionStartIndexes: Map<Long, Int>
): Long? {
    return sections
        .mapNotNull { section ->
            sectionStartIndexes[section.category.categoryId]?.let { startIndex ->
                section.category.categoryId to startIndex
            }
        }
        .lastOrNull { (_, startIndex) -> startIndex <= firstVisibleItemIndex }
        ?.first
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
