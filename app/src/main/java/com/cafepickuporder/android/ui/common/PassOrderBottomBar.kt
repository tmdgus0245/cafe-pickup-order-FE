package com.cafepickuporder.android.ui.common

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.SelectedNavy

enum class MainTab(
    val label: String,
    val iconText: String
) {
    Home("홈", "P"),
    Orders("주문내역", "≡"),
    Favorites("자주가요", "▱"),
    MyPage("내정보", "☺")
}

@Composable
fun PassOrderBottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White
    ) {
        MainTab.entries.forEach { tab ->
            val selected = selectedTab == tab

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Text(
                        text = tab.iconText,
                        color = if (selected) SelectedNavy else Muted
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = if (selected) SelectedNavy else Muted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
