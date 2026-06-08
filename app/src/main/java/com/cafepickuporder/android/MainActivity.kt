package com.cafepickuporder.android

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.cafepickuporder.android.ui.login.LoginScreen
import com.cafepickuporder.android.ui.mypage.MyPageScreen
import com.cafepickuporder.android.ui.signup.SignupScreen
import com.cafepickuporder.android.ui.store.MenuDetailScreen
import com.cafepickuporder.android.ui.store.MenuListScreen
import com.cafepickuporder.android.ui.store.StoreListScreen
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
    var selectedStoreId by remember { mutableStateOf<Long?>(null) }
    var selectedMenuId by remember { mutableStateOf<Long?>(null) }

    BackHandler(enabled = screen != "login") {
        when (screen) {
            "signup" -> {
                screen = "login"
            }

            "mypage" -> {
                screen = "store"
            }

            "store" -> {
                screen = "mypage"
            }

            "menuList" -> {
                screen = "store"
            }

            "menuDetail" -> {
                screen = "menuList"
            }

            "cart" -> {
                screen = "menuList"
            }

            "orderList" -> {
                screen = "store"
            }

            else -> {
                screen = "login"
            }
        }
    }

    when (screen) {
        "login" -> LoginScreen(
            onLoginSuccess = { screen = "store" },
            onMoveToSignup = { screen = "signup" }
        )

        "signup" -> SignupScreen(
            onMoveToLogin = { screen = "login" }
        )

        "mypage" -> MyPageScreen(
            onLogout = { screen = "login" }
        )

        "store" -> StoreListScreen(
            onMyPageClick = {
                screen = "mypage"
            },
            onOrderListClick = {
                screen = "orderList"
            },
            onStoreClick = { storeId ->
                selectedStoreId = storeId
                screen = "menuList"
            }
        )

        "menuList" -> MenuListScreen(
            storeId = selectedStoreId ?: 0L,
            onBackClick = {
                screen = "store"
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
            customerId = 1L,
            onBackClick = {
                screen = "menuList"
            },
            onOrderSuccess = {
                screen = "orderComplete"
            }
        )

        "orderComplete" -> OrderCompleteScreen(
            onGoStoreClick = {
                screen = "store"
            }
        )

        "orderList" -> OrderListScreen(
            customerId = 1L,
            onBackClick = {
                screen = "store"
            }
        )
    }
}