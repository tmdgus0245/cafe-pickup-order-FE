package com.cafepickuporder.android

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import com.cafepickuporder.android.ui.common.MainTab
import com.cafepickuporder.android.ui.common.PassOrderBottomBar
import com.cafepickuporder.android.ui.favorites.FavoritesScreen
import com.cafepickuporder.android.ui.favorites.FavoriteStoreManager
import com.cafepickuporder.android.ui.login.LoginScreen
import com.cafepickuporder.android.ui.mypage.MyPageScreen
import com.cafepickuporder.android.ui.signup.SignupScreen
import com.cafepickuporder.android.ui.signup.StoreAccountSignupScreen
import com.cafepickuporder.android.ui.store.MenuDetailScreen
import com.cafepickuporder.android.ui.store.MenuListScreen
import com.cafepickuporder.android.ui.store.StoreListScreen
import com.cafepickuporder.android.ui.store.StoreOwnerScreen
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
    val context = LocalContext.current
    val activity = context as? Activity
    var screen by remember { mutableStateOf("login") }
    var selectedTab by remember { mutableStateOf(MainTab.Home) }
    var selectedStoreId by remember { mutableStateOf<Long?>(null) }
    var selectedMenuId by remember { mutableStateOf<Long?>(null) }
    var customerId by remember { mutableStateOf<Long?>(null) }
    var ownerStoreId by remember { mutableStateOf<Long?>(null) }
    var ownerAccessToken by remember { mutableStateOf<String?>(null) }
    var loginAsOwner by remember { mutableStateOf(false) }
    var homeMapMode by remember { mutableStateOf(false) }
    var lastBackPressedAt by remember { mutableStateOf(0L) }

    fun requestAppExit() {
        val now = System.currentTimeMillis()
        if (now - lastBackPressedAt <= 2000L) {
            activity?.finish()
        } else {
            lastBackPressedAt = now
            Toast.makeText(
                context,
                "한 번 더 누르면 앱이 종료됩니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    BackHandler(enabled = screen != "login") {
        when (screen) {
            "signup", "ownerSignup" -> {
                screen = "login"
            }

            "main" -> {
                requestAppExit()
            }

            "ownerOrders" -> {
                requestAppExit()
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
            initialOwnerMode = loginAsOwner,
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
            onMoveToSignup = {
                loginAsOwner = false
                screen = "signup"
            },
            onMoveToOwnerSignup = {
                loginAsOwner = true
                screen = "ownerSignup"
            }
        )

        "ownerOrders" -> StoreOwnerScreen(
            storeId = ownerStoreId ?: 0L,
            accessToken = ownerAccessToken.orEmpty(),
            onLogout = {
                ownerStoreId = null
                ownerAccessToken = null
                screen = "login"
            }
        )

        "signup" -> SignupScreen(
            onMoveToLogin = {
                loginAsOwner = false
                screen = "login"
            }
        )

        "ownerSignup" -> StoreAccountSignupScreen(
            onMoveToLogin = {
                loginAsOwner = true
                screen = "login"
            }
        )

        "main" -> MainTabs(
            selectedTab = selectedTab,
            customerId = customerId ?: 0L,
            homeMapMode = homeMapMode,
            onHomeMapModeChanged = { homeMapMode = it },
            onTabSelected = { selectedTab = it },
            onStoreClick = { storeId ->
                selectedStoreId = storeId
                screen = "menuList"
            },
            onMenuClick = { storeId, menuId ->
                selectedStoreId = storeId
                selectedMenuId = menuId
                screen = "menuDetail"
            },
            onLogout = {
                customerId = null
                FavoriteStoreManager.clear()
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
    homeMapMode: Boolean,
    onHomeMapModeChanged: (Boolean) -> Unit,
    onTabSelected: (MainTab) -> Unit,
    onStoreClick: (Long) -> Unit,
    onMenuClick: (Long, Long) -> Unit,
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
                initialMapMode = homeMapMode,
                onMapModeChanged = onHomeMapModeChanged,
                onStoreClick = onStoreClick,
                onMenuClick = onMenuClick
            )

            MainTab.Orders -> OrderListScreen(
                modifier = Modifier.padding(innerPadding),
                customerId = customerId,
                onBackClick = { onTabSelected(MainTab.Home) }
            )

            MainTab.Favorites -> FavoritesScreen(
                modifier = Modifier.padding(innerPadding),
                onStoreClick = onStoreClick
            )

            MainTab.MyPage -> MyPageScreen(
                modifier = Modifier.padding(innerPadding),
                onLogout = onLogout
            )
        }
    }
}
