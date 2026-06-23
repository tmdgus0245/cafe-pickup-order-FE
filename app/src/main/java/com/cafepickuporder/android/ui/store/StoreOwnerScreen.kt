package com.cafepickuporder.android.ui.store

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange

private enum class OwnerTab {
    Orders,
    StoreManage
}

@Composable
fun StoreOwnerScreen(
    storeId: Long,
    accessToken: String,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(OwnerTab.Orders) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
        ) {
            OwnerTabButton(
                text = "주문관리",
                selected = selectedTab == OwnerTab.Orders,
                onClick = { selectedTab = OwnerTab.Orders },
                modifier = Modifier.weight(1f)
            )

            OwnerTabButton(
                text = "매장관리",
                selected = selectedTab == OwnerTab.StoreManage,
                onClick = { selectedTab = OwnerTab.StoreManage },
                modifier = Modifier.weight(1f)
            )
        }

        when (selectedTab) {
            OwnerTab.Orders -> StoreOrderManagementScreen(
                storeId = storeId,
                accessToken = accessToken,
                onLogout = onLogout
            )

            OwnerTab.StoreManage -> StoreMenuManagementScreen(
                storeId = storeId,
                accessToken = accessToken,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun OwnerTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PassOrange else PageGray,
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = if (selected) Color.White else Ink,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
