package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuDetailResponse
import com.cafepickuporder.android.data.response.MenuOptionGroupResponse
import com.cafepickuporder.android.data.response.MenuOptionResponse
import com.cafepickuporder.android.data.model.CartItem
import com.cafepickuporder.android.data.model.CartOption
import com.cafepickuporder.android.ui.cart.CartManager

@Composable
fun MenuDetailScreen(
    storeId: Long,
    menuId: Long,
    onBackClick: () -> Unit,
    onAddedToCart: () -> Unit
) {
    var menu by remember { mutableStateOf<MenuDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedOptions = remember {
        mutableStateMapOf<Long, MenuOptionResponse>()
    }

    LaunchedEffect(storeId, menuId) {
        try {
            menu = ApiClient.storeApi.getMenuDetail(
                storeId = storeId,
                menuId = menuId
            )
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

            Text(
                text = "메뉴 상세",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = "메뉴 상세 정보를 불러오지 못했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            menu == null -> {
                Text("메뉴 정보가 없습니다.")
            }

            else -> {
                MenuDetailContent(
                    menu = menu!!,
                    onBackClick = onBackClick,
                    onAddedToCart = onAddedToCart,
                    selectedOptions = selectedOptions,
                    onOptionSelected = { optionGroupId, option ->
                        selectedOptions[optionGroupId] = option
                    }
                )
            }
        }
    }
}

@Composable
private fun MenuDetailContent(
    menu: MenuDetailResponse,
    selectedOptions: MutableMap<Long, MenuOptionResponse>,
    onBackClick: () -> Unit,
    onAddedToCart: () -> Unit,
    onOptionSelected: (optionGroupId: Long, option: MenuOptionResponse) -> Unit
) {
    val totalPrice = menu.price + selectedOptions.values.sumOf { it.additionalPrice }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = menu.name,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = menu.description ?: "메뉴 설명이 없습니다.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "기본 가격 ${menu.price}원",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Divider()

        Spacer(modifier = Modifier.height(20.dp))

        if (menu.optionGroups.isEmpty()) {
            Text(
                text = "선택 가능한 옵션이 없습니다.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                menu.optionGroups.forEach { optionGroup ->
                    OptionGroupSection(
                        optionGroup = optionGroup,
                        selectedOption = selectedOptions[optionGroup.optionGroupId],
                        onOptionSelected = { option ->
                            onOptionSelected(optionGroup.optionGroupId, option)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val cartOptions = selectedOptions.map { entry ->
                    val optionGroupId = entry.key
                    val option = entry.value

                    CartOption(
                        optionGroupId = optionGroupId,
                        optionId = option.optionId,
                        name = option.name,
                        additionalPrice = option.additionalPrice
                    )
                }

                val cartItem = CartItem(
                    storeId = menu.storeId,
                    menuId = menu.menuId,
                    menuName = menu.name,
                    basePrice = menu.price,
                    options = cartOptions
                )

                CartManager.addItem(cartItem)
                onAddedToCart()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("${totalPrice}원 담기")
        }
    }
}

@Composable
private fun OptionGroupSection(
    optionGroup: MenuOptionGroupResponse,
    selectedOption: MenuOptionResponse?,
    onOptionSelected: (MenuOptionResponse) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = optionGroup.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (optionGroup.required) {
                    Text(
                        text = "필수",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Text(
                        text = "선택",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "최소 ${optionGroup.minSelect}개, 최대 ${optionGroup.maxSelect}개 선택",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            optionGroup.options.forEach { option ->
                OptionRow(
                    option = option,
                    selected = selectedOption?.optionId == option.optionId,
                    onClick = {
                        onOptionSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun OptionRow(
    option: MenuOptionResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = option.name,
                style = MaterialTheme.typography.bodyMedium
            )

            if (option.additionalPrice > 0) {
                Text(
                    text = "+${option.additionalPrice}원",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}