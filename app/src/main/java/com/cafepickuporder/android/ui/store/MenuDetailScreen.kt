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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.model.CartItem
import com.cafepickuporder.android.data.model.CartOption
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.MenuDetailResponse
import com.cafepickuporder.android.data.response.MenuOptionGroupResponse
import com.cafepickuporder.android.data.response.MenuOptionResponse
import com.cafepickuporder.android.ui.cart.CartManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.LineGray
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange

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
            .background(Color.White)
    ) {
        DetailTopBar(
            title = menu?.name ?: "메뉴 상세",
            onBackClick = onBackClick
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PassOrange)
                }
            }

            errorMessage != null -> {
                Text(
                    text = "메뉴 상세 정보를 불러오지 못했습니다.\n$errorMessage",
                    modifier = Modifier.padding(20.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

            menu == null -> {
                Text(
                    text = "메뉴 정보가 없습니다.",
                    modifier = Modifier.padding(20.dp),
                    color = Muted
                )
            }

            else -> {
                MenuDetailContent(
                    menu = menu!!,
                    selectedOptions = selectedOptions,
                    onOptionSelected = { optionGroupId, option ->
                        selectedOptions[optionGroupId] = option
                    },
                    onAddedToCart = onAddedToCart
                )
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBackClick) {
            Text("‹", color = Ink, style = MaterialTheme.typography.headlineMedium)
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PageGray
        ) {
            Text(
                text = "같이주문",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                color = Muted,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun MenuDetailContent(
    menu: MenuDetailResponse,
    selectedOptions: MutableMap<Long, MenuOptionResponse>,
    onOptionSelected: (optionGroupId: Long, option: MenuOptionResponse) -> Unit,
    onAddedToCart: () -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    val requiredSatisfied = menu.optionGroups
        .filter { it.required }
        .all { selectedOptions[it.optionGroupId] != null }
    val unitPrice = menu.price + selectedOptions.values.sumOf { it.additionalPrice }
    val totalPrice = unitPrice * quantity

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            item {
                MenuHero(menu = menu)
            }

            item {
                QuantityRow(
                    quantity = quantity,
                    onDecrease = {
                        if (quantity > 1) quantity -= 1
                    },
                    onIncrease = {
                        quantity += 1
                    }
                )
            }

            items(menu.optionGroups) { optionGroup ->
                OptionGroupSection(
                    optionGroup = optionGroup,
                    selectedOption = selectedOptions[optionGroup.optionGroupId],
                    onOptionSelected = { option ->
                        onOptionSelected(optionGroup.optionGroupId, option)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

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
                    options = cartOptions,
                    quantity = quantity
                )

                CartManager.addItem(cartItem)
                onAddedToCart()
            },
            enabled = requiredSatisfied,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("${formatPrice(totalPrice)} 담기")
        }
    }
}

@Composable
private fun MenuHero(menu: MenuDetailResponse) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(PageGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MENU",
                    color = Muted,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = SoftOrange
        ) {
            Text(
                text = displayMenuStatus(menu.status),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = PassOrange,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = menu.name,
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = formatPrice(menu.price),
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold
        )

        if (!menu.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = menu.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Muted
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LineGray)
        )
    }
}

@Composable
private fun QuantityRow(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "수량",
            style = MaterialTheme.typography.headlineSmall,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = PageGray
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuantityButton("-", onDecrease)
                Text(
                    text = "$quantity",
                    modifier = Modifier.padding(horizontal = 22.dp),
                    color = Ink,
                    style = MaterialTheme.typography.titleLarge
                )
                QuantityButton("+", onIncrease)
            }
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
            .size(width = 48.dp, height = 42.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = Ink
        )
    }
}

@Composable
private fun OptionGroupSection(
    optionGroup: MenuOptionGroupResponse,
    selectedOption: MenuOptionResponse?,
    onOptionSelected: (MenuOptionResponse) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionGroup.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Ink,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )

            Surface(
                shape = RoundedCornerShape(50),
                color = if (optionGroup.required) SoftOrange else Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (optionGroup.required) PassOrange else LineGray)
            ) {
                Text(
                    text = if (optionGroup.required) "필수옵션" else "선택옵션",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = if (optionGroup.required) PassOrange else Muted,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        optionGroup.options.forEach { option ->
            OptionRow(
                option = option,
                selected = selectedOption?.optionId == option.optionId,
                onClick = { onOptionSelected(option) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(LineGray)
        )
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = option.name,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (option.additionalPrice == 0) {
                "0원"
            } else {
                "+${formatPrice(option.additionalPrice)}"
            },
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun displayMenuStatus(status: String): String {
    return when (status) {
        "ON_SALE" -> "매장적립"
        "SOLD_OUT" -> "품절"
        "HIDDEN" -> "숨김"
        else -> status
    }
}

private fun formatPrice(value: Int): String {
    return "%,d원".format(value)
}
