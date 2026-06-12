package com.cafepickuporder.android

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cafepickuporder.android.ui.common.MainTab
import com.cafepickuporder.android.ui.common.PassOrderBottomBar
import com.cafepickuporder.android.ui.favorites.FavoritesScreen
import com.cafepickuporder.android.ui.login.LoginScreen
import com.cafepickuporder.android.ui.mypage.MyPageScreen
import com.cafepickuporder.android.ui.signup.SignupScreen
import com.cafepickuporder.android.ui.store.MenuDetailScreen
import com.cafepickuporder.android.ui.store.MenuListScreen
import com.cafepickuporder.android.ui.store.StoreListScreen
import com.cafepickuporder.android.ui.store.StoreOrderManagementScreen
import com.cafepickuporder.android.ui.theme.CafePickupOrderTheme
import com.cafepickuporder.android.ui.cart.CartScreen
import com.cafepickuporder.android.ui.order.OrderCompleteScreen
import com.cafepickuporder.android.ui.order.OrderListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CafePickupOrderTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    var screen by remember { mutableStateOf("login") }
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    var selectedStoreId by remember { mutableStateOf<Long?>(null) }
    var selectedMenuId by remember { mutableStateOf<Long?>(null) }
    var customerId by remember { mutableStateOf<Long?>(null) }
    var ownerStoreId by remember { mutableStateOf<Long?>(null) }
    var ownerAccessToken by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = screen != "login") {
        when (screen) {
            "signup" -> {
                screen = "login"
            }

            "main" -> {
                if (selectedTab == MainTab.Home) {
                    screen = "login"
                } else {
                    selectedTab = MainTab.Home
                }
            }

            "ownerOrders" -> {
                screen = "login"
            }

            "menuList" -> {
                screen = "main"
                selectedTab = MainTab.Home
            }

            "menuDetail" -> {
                screen = "menuList"
            }

            "cart" -> {
                screen = "menuList"
            }

            "orderList" -> {
                screen = "main"
                selectedTab = MainTab.Home
            }

            else -> {
                screen = "login"
            }
        }
    }

    when (screen) {
        "login" -> LoginScreen(
            onLoginSuccess = { loggedInCustomerId ->
                customerId = loggedInCustomerId
                selectedTab = MainTab.Home
                screen = "main"
            },
            onOwnerLoginSuccess = { storeId, accessToken ->
                ownerStoreId = storeId
                ownerAccessToken = accessToken
                screen = "ownerOrders"
            },
            onMoveToSignup = { screen = "signup" }
        )

        "ownerOrders" -> StoreOrderManagementScreen(
            storeId = ownerStoreId ?: 0L,
            accessToken = ownerAccessToken.orEmpty(),
            onLogout = {
                ownerStoreId = null
                ownerAccessToken = null
                screen = "login"
            }
        )

        "signup" -> SignupScreen(
            onMoveToLogin = { screen = "login" }
        )

        "main" -> MainTabs(
            selectedTab = selectedTab,
            customerId = customerId ?: 0L,
            onTabSelected = { selectedTab = it },
            onStoreClick = { storeId ->
                selectedStoreId = storeId
                screen = "menuList"
            },
            onLogout = {
                customerId = null
                screen = "login"
            }
        )

        "menuList" -> MenuListScreen(
            storeId = selectedStoreId ?: 0L,
            onBackClick = {
                screen = "main"
                selectedTab = MainTab.Home
            },
            onCartClick = {
                screen = "cart"
            },
            onMenuClick = { storeId, menuId ->
                selectedStoreId = storeId
                selectedMenuId = menuId
                screen = "menuDetail"
            }
        )

        "menuDetail" -> MenuDetailScreen(
            storeId = selectedStoreId ?: 0L,
            menuId = selectedMenuId ?: 0L,
            onBackClick = {
                screen = "menuList"
            },
            onAddedToCart = {
                screen = "menuList"
            }
        )

        "cart" -> CartScreen(
            storeId = selectedStoreId ?: 0L,
            customerId = customerId ?: 0L,
            onBackClick = {
                screen = "menuList"
            },
            onOrderSuccess = {
                screen = "orderComplete"
            }
        )

        "orderComplete" -> OrderCompleteScreen(
            onGoStoreClick = {
                selectedTab = MainTab.Home
                screen = "main"
            }
        )

        "orderList" -> OrderListScreen(
            customerId = customerId ?: 0L,
            onBackClick = {
                screen = "main"
                selectedTab = MainTab.Home
            }
        )
    }
}

@Composable
private fun MainTabs(
    selectedTab: MainTab,
    customerId: Long,
    onTabSelected: (MainTab) -> Unit,
    onStoreClick: (Long) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        bottomBar = {
            PassOrderBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.Home -> StoreListScreen(
                modifier = Modifier.padding(innerPadding),
                onStoreClick = onStoreClick
            )

            MainTab.Orders -> OrderListScreen(
                modifier = Modifier.padding(innerPadding),
                customerId = customerId,
                onBackClick = { onTabSelected(MainTab.Home) }
            )

            MainTab.Favorites -> FavoritesScreen(
                modifier = Modifier.padding(innerPadding)
            )

            MainTab.MyPage -> MyPageScreen(
                modifier = Modifier.padding(innerPadding),
                onLogout = onLogout
            )
        }
    }
}
