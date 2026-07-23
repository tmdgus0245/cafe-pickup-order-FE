package com.cafepickuporder.android.ui.store

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.MenuCategoryManageRequest
import com.cafepickuporder.android.data.request.MenuManageRequest
import com.cafepickuporder.android.data.request.MenuOptionGroupManageRequest
import com.cafepickuporder.android.data.request.MenuOptionManageRequest
import com.cafepickuporder.android.data.response.MenuCategoryResponse
import com.cafepickuporder.android.data.response.MenuDetailResponse
import com.cafepickuporder.android.data.response.MenuOptionGroupResponse
import com.cafepickuporder.android.data.response.MenuOptionResponse
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import kotlinx.coroutines.launch

@Composable
fun StoreMenuManagementScreen(
    storeId: Long,
    accessToken: String,
    onLogout: () -> Unit
) {
    val authorization = "Bearer $accessToken"
    val scope = rememberCoroutineScope()

    var categories by remember { mutableStateOf<List<MenuCategoryResponse>>(emptyList()) }
    var menus by remember { mutableStateOf<List<MenuResponse>>(emptyList()) }
    var selectedMenu by remember { mutableStateOf<MenuDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var categoryNameDialog by remember { mutableStateOf<MenuCategoryResponse?>(null) }
    var showNewMenuDialogFor by remember { mutableStateOf<MenuCategoryResponse?>(null) }
    var menuNameDialog by remember { mutableStateOf<MenuResponse?>(null) }
    var menuEditDialog by remember { mutableStateOf<MenuResponse?>(null) }
    var showNewOptionGroupDialog by remember { mutableStateOf(false) }
    var optionGroupDialog by remember { mutableStateOf<MenuOptionGroupResponse?>(null) }
    var newOptionTarget by remember { mutableStateOf<MenuOptionGroupResponse?>(null) }
    var optionDialog by remember { mutableStateOf<Pair<MenuOptionGroupResponse, MenuOptionResponse>?>(null) }

    fun sortedCategories() = categories.sortedBy { it.displayOrder ?: Int.MAX_VALUE }
    fun sortedMenusFor(categoryId: Long) =
        menus.filter { it.categoryId == categoryId }
            .sortedBy { it.displayOrder ?: Int.MAX_VALUE }

    fun loadAll() {
        scope.launch {
            isLoading = true
            message = null
            try {
                categories = ApiClient.storeMenuManageApi.getCategories(storeId, authorization)
                menus = ApiClient.storeApi.getMenus(storeId)
                selectedMenu = selectedMenu?.let {
                    ApiClient.storeApi.getMenuDetail(storeId, it.menuId)
                }
            } catch (e: Exception) {
                message = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun loadMenu(menuId: Long) {
        scope.launch {
            try {
                selectedMenu = ApiClient.storeApi.getMenuDetail(storeId, menuId)
            } catch (e: Exception) {
                message = e.message
            }
        }
    }

    fun moveCategory(category: MenuCategoryResponse, direction: Int) {
        val ordered = sortedCategories()
        val index = ordered.indexOfFirst { it.categoryId == category.categoryId }
        val target = index + direction
        if (index == -1 || target !in ordered.indices) return

        val reordered = ordered.toMutableList().apply {
            add(target, removeAt(index))
        }.mapIndexed { order, item ->
            item.copy(displayOrder = order)
        }

        categories = reordered

        scope.launch {
            try {
                reordered.forEach { item ->
                    ApiClient.storeMenuManageApi.updateCategory(
                        storeId,
                        item.categoryId,
                        authorization,
                        MenuCategoryManageRequest(item.name, item.displayOrder)
                    )
                }
            } catch (e: Exception) {
                message = e.message
            }
        }
    }

    fun moveMenu(menu: MenuResponse, direction: Int) {
        val ordered = sortedMenusFor(menu.categoryId)
        val index = ordered.indexOfFirst { it.menuId == menu.menuId }
        val target = index + direction
        if (index == -1 || target !in ordered.indices) return

        val reorderedInCategory = ordered.toMutableList().apply {
            add(target, removeAt(index))
        }.mapIndexed { order, item ->
            item.copy(displayOrder = order)
        }

        menus = menus.filterNot { it.categoryId == menu.categoryId } + reorderedInCategory

        scope.launch {
            try {
                reorderedInCategory.forEach { item ->
                    ApiClient.storeMenuManageApi.updateMenu(
                        storeId,
                        item.menuId,
                        authorization,
                        item.toManageRequest(displayOrder = item.displayOrder)
                    )
                }
            } catch (e: Exception) {
                message = e.message
            }
        }
    }

    LaunchedEffect(storeId, accessToken) {
        loadAll()
    }

    if (showNewCategoryDialog || categoryNameDialog != null) {
        CategoryNameDialog(
            category = categoryNameDialog,
            onDismiss = {
                showNewCategoryDialog = false
                categoryNameDialog = null
            },
            onSave = { name ->
                scope.launch {
                    try {
                        val category = categoryNameDialog
                        if (category == null) {
                            ApiClient.storeMenuManageApi.createCategory(
                                storeId,
                                authorization,
                                MenuCategoryManageRequest(name, categories.size)
                            )
                        } else {
                            ApiClient.storeMenuManageApi.updateCategory(
                                storeId,
                                category.categoryId,
                                authorization,
                                MenuCategoryManageRequest(name, category.displayOrder)
                            )
                        }
                        showNewCategoryDialog = false
                        categoryNameDialog = null
                        loadAll()
                    } catch (e: Exception) {
                        message = e.message
                    }
                }
            }
        )
    }

    if (showNewMenuDialogFor != null || menuEditDialog != null) {
        MenuDialog(
            menu = menuEditDialog,
            categories = sortedCategories(),
            initialCategoryId = showNewMenuDialogFor?.categoryId,
            onDismiss = {
                showNewMenuDialogFor = null
                menuEditDialog = null
            },
            onSave = { request ->
                scope.launch {
                    try {
                        val menu = menuEditDialog
                        if (menu == null) {
                            ApiClient.storeMenuManageApi.createMenu(storeId, authorization, request)
                        } else {
                            ApiClient.storeMenuManageApi.updateMenu(
                                storeId,
                                menu.menuId,
                                authorization,
                                request
                            )
                        }
                        showNewMenuDialogFor = null
                        menuEditDialog = null
                        loadAll()
                    } catch (e: Exception) {
                        message = e.message
                    }
                }
            }
        )
    }

    if (menuNameDialog != null) {
        MenuNameDialog(
            menu = menuNameDialog!!,
            onDismiss = { menuNameDialog = null },
            onSave = { name ->
                val menu = menuNameDialog ?: return@MenuNameDialog
                scope.launch {
                    try {
                        ApiClient.storeMenuManageApi.updateMenu(
                            storeId,
                            menu.menuId,
                            authorization,
                            menu.toManageRequest(name = name)
                        )
                        menuNameDialog = null
                        loadAll()
                    } catch (e: Exception) {
                        message = e.message
                    }
                }
            }
        )
    }

    if (showNewOptionGroupDialog || optionGroupDialog != null) {
        val menu = selectedMenu
        if (menu != null) {
            OptionGroupDialog(
                optionGroup = optionGroupDialog,
                onDismiss = {
                    showNewOptionGroupDialog = false
                    optionGroupDialog = null
                },
                onSave = { request ->
                    scope.launch {
                        try {
                            selectedMenu = if (optionGroupDialog == null) {
                                ApiClient.storeMenuManageApi.createOptionGroup(
                                    storeId,
                                    menu.menuId,
                                    authorization,
                                    request
                                )
                            } else {
                                ApiClient.storeMenuManageApi.updateOptionGroup(
                                    storeId,
                                    menu.menuId,
                                    optionGroupDialog!!.optionGroupId,
                                    authorization,
                                    request
                                )
                            }
                            showNewOptionGroupDialog = false
                            optionGroupDialog = null
                        } catch (e: Exception) {
                            message = e.message
                        }
                    }
                }
            )
        }
    }

    if (newOptionTarget != null || optionDialog != null) {
        val menu = selectedMenu
        val group = newOptionTarget ?: optionDialog!!.first
        if (menu != null) {
            OptionDialog(
                option = optionDialog?.second,
                onDismiss = {
                    newOptionTarget = null
                    optionDialog = null
                },
                onSave = { request ->
                    scope.launch {
                        try {
                            selectedMenu = if (optionDialog == null) {
                                ApiClient.storeMenuManageApi.createOption(
                                    storeId,
                                    menu.menuId,
                                    group.optionGroupId,
                                    authorization,
                                    request
                                )
                            } else {
                                ApiClient.storeMenuManageApi.updateOption(
                                    storeId,
                                    menu.menuId,
                                    group.optionGroupId,
                                    optionDialog!!.second.optionId,
                                    authorization,
                                    request
                                )
                            }
                            newOptionTarget = null
                            optionDialog = null
                        } catch (e: Exception) {
                            message = e.message
                        }
                    }
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "매장 관리",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            TextButton(onClick = onLogout) {
                Text("로그아웃")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(color = PassOrange)
            return@Column
        }

        if (message != null) {
            Text(
                text = message.orEmpty(),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ManageSectionHeader(
                    title = "카테고리",
                    action = "카테고리 추가",
                    onAction = { showNewCategoryDialog = true }
                )
            }

            items(sortedCategories(), key = { it.categoryId }) { category ->
                CategoryBlock(
                    modifier = Modifier.animateItem(),
                    category = category,
                    menus = sortedMenusFor(category.categoryId),
                    expandedMenu = selectedMenu?.takeIf { expanded ->
                        sortedMenusFor(category.categoryId).any { it.menuId == expanded.menuId }
                    },
                    onMoveUp = { moveCategory(category, -1) },
                    onMoveDown = { moveCategory(category, 1) },
                    onNameClick = { categoryNameDialog = category },
                    onDelete = {
                        scope.launch {
                            try {
                                ApiClient.storeMenuManageApi.deleteCategory(
                                    storeId,
                                    category.categoryId,
                                    authorization
                                )
                                loadAll()
                            } catch (e: Exception) {
                                message = e.message
                            }
                        }
                    },
                    onAddMenu = { showNewMenuDialogFor = category },
                    onMenuClick = { menu -> loadMenu(menu.menuId) },
                    onMenuNameClick = { menu -> menuNameDialog = menu },
                    onMenuEdit = { menu -> menuEditDialog = menu },
                    onMenuDelete = { menu ->
                        scope.launch {
                            try {
                                ApiClient.storeMenuManageApi.deleteMenu(storeId, menu.menuId, authorization)
                                if (selectedMenu?.menuId == menu.menuId) selectedMenu = null
                                loadAll()
                            } catch (e: Exception) {
                                message = e.message
                            }
                        }
                    },
                    onMenuMoveUp = { menu -> moveMenu(menu, -1) },
                    onMenuMoveDown = { menu -> moveMenu(menu, 1) },
                    onHideOptions = { selectedMenu = null },
                    onAddGroup = { showNewOptionGroupDialog = true },
                    onEditGroup = { optionGroupDialog = it },
                    onDeleteGroup = { menu, group ->
                        scope.launch {
                            try {
                                ApiClient.storeMenuManageApi.deleteOptionGroup(
                                    storeId,
                                    menu.menuId,
                                    group.optionGroupId,
                                    authorization
                                )
                                selectedMenu = ApiClient.storeApi.getMenuDetail(storeId, menu.menuId)
                            } catch (e: Exception) {
                                message = e.message
                            }
                        }
                    },
                    onAddOption = { newOptionTarget = it },
                    onEditOption = { group, option -> optionDialog = group to option },
                    onDeleteOption = { menu, group, option ->
                        scope.launch {
                            try {
                                ApiClient.storeMenuManageApi.deleteOption(
                                    storeId,
                                    menu.menuId,
                                    group.optionGroupId,
                                    option.optionId,
                                    authorization
                                )
                                selectedMenu = ApiClient.storeApi.getMenuDetail(storeId, menu.menuId)
                            } catch (e: Exception) {
                                message = e.message
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ManageSectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onAction) {
            Text(action)
        }
    }
}

@Composable
private fun CategoryBlock(
    modifier: Modifier = Modifier,
    category: MenuCategoryResponse,
    menus: List<MenuResponse>,
    expandedMenu: MenuDetailResponse?,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onNameClick: () -> Unit,
    onDelete: () -> Unit,
    onAddMenu: () -> Unit,
    onMenuClick: (MenuResponse) -> Unit,
    onMenuNameClick: (MenuResponse) -> Unit,
    onMenuEdit: (MenuResponse) -> Unit,
    onMenuDelete: (MenuResponse) -> Unit,
    onMenuMoveUp: (MenuResponse) -> Unit,
    onMenuMoveDown: (MenuResponse) -> Unit,
    onHideOptions: () -> Unit,
    onAddGroup: () -> Unit,
    onEditGroup: (MenuOptionGroupResponse) -> Unit,
    onDeleteGroup: (MenuDetailResponse, MenuOptionGroupResponse) -> Unit,
    onAddOption: (MenuOptionGroupResponse) -> Unit,
    onEditOption: (MenuOptionGroupResponse, MenuOptionResponse) -> Unit,
    onDeleteOption: (MenuDetailResponse, MenuOptionGroupResponse, MenuOptionResponse) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) Color(0xFFFFF7F3) else PageGray
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 8.dp else 0.dp),
        modifier = modifier
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffset
                scaleX = if (isDragging) 1.01f else 1f
                scaleY = if (isDragging) 1.01f else 1f
            }
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.reorderDragTarget(
                    onMoveUp = onMoveUp,
                    onMoveDown = onMoveDown,
                    onDragStateChange = { isDragging = it },
                    onVisualOffsetChange = { dragOffset = it }
                )
            ) {
                ReorderHandle()
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category.name,
                    color = Ink,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNameClick() }
                )
                IconDeleteButton(onClick = onDelete)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (menus.isEmpty()) {
                Text(
                    text = "아직 메뉴가 없습니다.",
                    color = Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                menus.forEach { menu ->
                    key(menu.menuId) {
                        MenuManageRow(
                            menu = menu,
                            expanded = expandedMenu?.menuId == menu.menuId,
                            onClick = { onMenuClick(menu) },
                            onNameClick = { onMenuNameClick(menu) },
                            onEdit = { onMenuEdit(menu) },
                            onDelete = { onMenuDelete(menu) },
                            onMoveUp = { onMenuMoveUp(menu) },
                            onMoveDown = { onMenuMoveDown(menu) }
                        )
                    }

                    if (expandedMenu?.menuId == menu.menuId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OptionSection(
                            menu = expandedMenu,
                            onHide = onHideOptions,
                            onAddGroup = onAddGroup,
                            onEditGroup = onEditGroup,
                            onDeleteGroup = { group -> onDeleteGroup(expandedMenu, group) },
                            onAddOption = onAddOption,
                            onEditOption = onEditOption,
                            onDeleteOption = { group, option -> onDeleteOption(expandedMenu, group, option) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            OutlinedButton(
                onClick = onAddMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("메뉴 추가")
            }
        }
    }
}

@Composable
private fun MenuManageRow(
    menu: MenuResponse,
    expanded: Boolean,
    onClick: () -> Unit,
    onNameClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isDragging) Color(0xFFFFF7F3) else Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffset
                scaleX = if (isDragging) 1.01f else 1f
                scaleY = if (isDragging) 1.01f else 1f
            }
            .animateContentSize()
            .reorderDragTarget(
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onDragStateChange = { isDragging = it },
                onVisualOffsetChange = { dragOffset = it }
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReorderHandle()
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = menu.name,
                    color = if (expanded) PassOrange else Ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNameClick() }
                )
                Text(
                    text = "${formatPrice(menu.price)} · ${displayMenuStatus(menu.status)}",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onEdit) {
                Text("수정")
            }
            IconDeleteButton(onClick = onDelete)
        }
    }
}

@Composable
private fun ReorderHandle() {
    Column(
        modifier = Modifier.size(width = 18.dp, height = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "▴",
            color = PassOrange.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "▾",
            color = PassOrange.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun Modifier.reorderDragTarget(
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragStateChange: (Boolean) -> Unit,
    onVisualOffsetChange: (Float) -> Unit
): Modifier {
    val stepPx = 44f
    var reorderOffset = 0f
    var visualOffset = 0f

    return pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                reorderOffset = 0f
                visualOffset = 0f
                onVisualOffsetChange(0f)
                onDragStateChange(true)
            },
            onDragEnd = {
                reorderOffset = 0f
                visualOffset = 0f
                onVisualOffsetChange(0f)
                onDragStateChange(false)
            },
            onDragCancel = {
                reorderOffset = 0f
                visualOffset = 0f
                onVisualOffsetChange(0f)
                onDragStateChange(false)
            },
            onDrag = { change, dragAmount ->
                change.consume()
                reorderOffset += dragAmount.y
                visualOffset += dragAmount.y
                onVisualOffsetChange(visualOffset.coerceIn(-96f, 96f))

                while (reorderOffset <= -stepPx) {
                    onMoveUp()
                    reorderOffset += stepPx
                }

                while (reorderOffset >= stepPx) {
                    onMoveDown()
                    reorderOffset -= stepPx
                }
            }
        )
    }
}

@Composable
private fun IconDeleteButton(onClick: () -> Unit) {
    Text(
        text = "×",
        color = Muted,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp)
    )
}

@Composable
private fun OptionSection(
    menu: MenuDetailResponse,
    onHide: () -> Unit,
    onAddGroup: () -> Unit,
    onEditGroup: (MenuOptionGroupResponse) -> Unit,
    onDeleteGroup: (MenuOptionGroupResponse) -> Unit,
    onAddOption: (MenuOptionGroupResponse) -> Unit,
    onEditOption: (MenuOptionGroupResponse, MenuOptionResponse) -> Unit,
    onDeleteOption: (MenuOptionGroupResponse, MenuOptionResponse) -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F3))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${menu.name} 옵션 설정",
                        color = Ink,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Button(onClick = onAddGroup) {
                    Text("그룹 추가")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "-",
                    color = Muted,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clickable { onHide() }
                        .padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (menu.optionGroups.isEmpty()) {
                Text("등록된 옵션 그룹이 없습니다.", color = Muted)
            }

            menu.optionGroups.forEach { group ->
                OptionGroupCard(
                    group = group,
                    onEditGroup = { onEditGroup(group) },
                    onDeleteGroup = { onDeleteGroup(group) },
                    onAddOption = { onAddOption(group) },
                    onEditOption = { option -> onEditOption(group, option) },
                    onDeleteOption = { option -> onDeleteOption(group, option) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun OptionGroupCard(
    group: MenuOptionGroupResponse,
    onEditGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    onAddOption: () -> Unit,
    onEditOption: (MenuOptionResponse) -> Unit,
    onDeleteOption: (MenuOptionResponse) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${group.name} (${if (group.required) "필수" else "선택"})",
                    color = Ink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEditGroup) { Text("수정") }
                IconDeleteButton(onClick = onDeleteGroup)
            }

            group.options.forEach { option ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${option.name} · ${formatPrice(option.additionalPrice)}",
                        color = Ink,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onEditOption(option) }) { Text("수정") }
                    IconDeleteButton(onClick = { onDeleteOption(option) })
                }
            }

            OutlinedButton(onClick = onAddOption, modifier = Modifier.fillMaxWidth()) {
                Text("옵션 추가")
            }
        }
    }
}

@Composable
private fun CategoryNameDialog(
    category: MenuCategoryResponse?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }

    ManageDialog(
        title = if (category == null) "카테고리 추가" else "카테고리 이름 수정",
        subtitle = if (category == null) {
            "메뉴를 보기 좋게 묶을 새 분류를 만들어보세요."
        } else {
            "고객에게 표시되는 카테고리 이름을 변경해요."
        },
        onDismiss = onDismiss,
        onSave = { onSave(name.trim()) },
        saveEnabled = name.isNotBlank()
    ) {
        DialogSectionLabel("카테고리 정보")
        ManageTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("카테고리 이름") },
            placeholder = "예: 시즌 메뉴",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MenuNameDialog(
    menu: MenuResponse,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(menu.name) }

    ManageDialog(
        title = "메뉴 이름 수정",
        subtitle = "고객에게 표시되는 메뉴 이름을 변경해요.",
        onDismiss = onDismiss,
        onSave = { onSave(name.trim()) },
        saveEnabled = name.isNotBlank()
    ) {
        DialogSectionLabel("메뉴 정보")
        ManageTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("메뉴명") },
            placeholder = "메뉴 이름을 입력해주세요",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MenuDialog(
    menu: MenuResponse?,
    categories: List<MenuCategoryResponse>,
    initialCategoryId: Long?,
    onDismiss: () -> Unit,
    onSave: (MenuManageRequest) -> Unit
) {
    var selectedCategoryId by remember {
        mutableStateOf(menu?.categoryId ?: initialCategoryId ?: categories.firstOrNull()?.categoryId ?: 0L)
    }
    var name by remember { mutableStateOf(menu?.name ?: "") }
    var description by remember { mutableStateOf(menu?.description ?: "") }
    var price by remember { mutableStateOf(menu?.price?.toString() ?: "0") }
    var status by remember { mutableStateOf(menu?.status ?: "ON_SALE") }

    ManageDialog(
        title = if (menu == null) "메뉴 추가" else "메뉴 수정",
        subtitle = if (menu == null) {
            "새 메뉴의 기본 정보와 판매 상태를 설정해요."
        } else {
            "메뉴 정보를 확인하고 필요한 내용을 바꿔주세요."
        },
        onDismiss = onDismiss,
        onSave = {
            onSave(
                MenuManageRequest(
                    categoryId = selectedCategoryId,
                    name = name,
                    description = description.ifBlank { null },
                    price = price.toIntOrNull() ?: 0,
                    imageUrl = menu?.imageUrl,
                    status = status,
                    displayOrder = menu?.displayOrder ?: 0
                )
            )
        },
        saveEnabled = name.isNotBlank() &&
            selectedCategoryId != 0L &&
            (price.toIntOrNull() ?: -1) >= 0
    ) {
        DialogSectionLabel("카테고리")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            categories.forEach { category ->
                SelectChip(
                    text = category.name,
                    selected = selectedCategoryId == category.categoryId,
                    onClick = { selectedCategoryId = category.categoryId }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))
        DialogSectionLabel("기본 정보")
        ManageTextField(
            name,
            { name = it },
            label = { Text("메뉴명") },
            placeholder = "예: 바닐라 라떼",
            modifier = Modifier.fillMaxWidth()
        )
        ManageTextField(
            description,
            { description = it },
            label = { Text("메뉴 설명") },
            placeholder = "맛과 특징을 간단히 소개해주세요",
            singleLine = false,
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        ManageTextField(
            price,
            { price = it.filter(Char::isDigit) },
            label = { Text("가격") },
            placeholder = "0",
            suffix = "원",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))
        DialogSectionLabel("판매 상태")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ON_SALE", "SOLD_OUT", "HIDDEN").forEach { value ->
                SelectChip(
                    text = displayMenuStatus(value),
                    selected = status == value,
                    onClick = { status = value }
                )
            }
        }
    }
}

@Composable
private fun OptionGroupDialog(
    optionGroup: MenuOptionGroupResponse?,
    onDismiss: () -> Unit,
    onSave: (MenuOptionGroupManageRequest) -> Unit
) {
    var name by remember { mutableStateOf(optionGroup?.name ?: "") }
    var required by remember { mutableStateOf(optionGroup?.required ?: false) }
    var minSelect by remember { mutableStateOf(optionGroup?.minSelect?.toString() ?: "0") }
    var maxSelect by remember { mutableStateOf(optionGroup?.maxSelect?.toString() ?: "1") }

    ManageDialog(
        title = if (optionGroup == null) "옵션그룹 추가" else "옵션그룹 수정",
        onDismiss = onDismiss,
        onSave = {
            onSave(
                MenuOptionGroupManageRequest(
                    name = name,
                    required = required,
                    minSelect = minSelect.toIntOrNull(),
                    maxSelect = maxSelect.toIntOrNull()
                )
            )
        }
    ) {
        ManageTextField(name, { name = it }, label = { Text("옵션그룹명") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectChip("필수", required, onClick = { required = true })
            SelectChip("선택", !required, onClick = { required = false })
        }
        ManageTextField(minSelect, { minSelect = it }, label = { Text("최소 선택") }, modifier = Modifier.fillMaxWidth())
        ManageTextField(maxSelect, { maxSelect = it }, label = { Text("최대 선택") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OptionDialog(
    option: MenuOptionResponse?,
    onDismiss: () -> Unit,
    onSave: (MenuOptionManageRequest) -> Unit
) {
    var name by remember { mutableStateOf(option?.name ?: "") }
    var additionalPrice by remember { mutableStateOf(option?.additionalPrice?.toString() ?: "0") }

    ManageDialog(
        title = if (option == null) "옵션 추가" else "옵션 수정",
        onDismiss = onDismiss,
        onSave = {
            onSave(
                MenuOptionManageRequest(
                    name = name,
                    additionalPrice = additionalPrice.toIntOrNull() ?: 0,
                    displayOrder = option?.displayOrder ?: 0
                )
            )
        }
    ) {
        ManageTextField(name, { name = it }, label = { Text("옵션명") }, modifier = Modifier.fillMaxWidth())
        ManageTextField(additionalPrice, { additionalPrice = it }, label = { Text("추가 금액") }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SelectChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) PassOrange else Color(0xFFFFF8F4),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) PassOrange else Color(0xFFF1DED4)
        ),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            color = if (selected) Color.White else Color(0xFF6D4C3D),
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ManageDialog(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFFBF8),
        tonalElevation = 0.dp,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Surface(
                    color = Color(0xFFFFE9DE),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (title.contains("추가")) "새로 만들기" else "정보 변경",
                        color = Color(0xFFD84D28),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Text(
                    text = title,
                    color = Ink,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                content()
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = saveEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PassOrange,
                    disabledContainerColor = Color(0xFFF0D7CB),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "저장",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "닫기",
                    color = Color(0xFF7A655B),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Composable
private fun ManageTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable (() -> Unit),
    placeholder: String? = null,
    suffix: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder?.let {
            { Text(text = it, color = Color(0xFFB3A49C)) }
        },
        suffix = suffix?.let {
            { Text(text = it, color = Muted, fontWeight = FontWeight.SemiBold) }
        },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PassOrange,
            unfocusedBorderColor = Color(0xFFEADFD9),
            focusedLabelColor = PassOrange,
            unfocusedLabelColor = Muted,
            cursorColor = PassOrange,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFFFEFD)
        ),
        modifier = modifier
    )
}

@Composable
private fun DialogSectionLabel(text: String) {
    Text(
        text = text,
        color = Color(0xFF725B50),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold
    )
}

private fun MenuResponse.toManageRequest(
    name: String = this.name,
    displayOrder: Int? = this.displayOrder
): MenuManageRequest {
    return MenuManageRequest(
        categoryId = categoryId,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        status = status,
        displayOrder = displayOrder
    )
}

private fun displayMenuStatus(status: String): String {
    return when (status) {
        "ON_SALE" -> "판매중"
        "SOLD_OUT" -> "품절"
        "HIDDEN" -> "숨김"
        else -> status
    }
}

private fun formatPrice(value: Int): String {
    return "%,d원".format(value)
}
