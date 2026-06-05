package com.cafepickuporder.android

import android.os.Bundle
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
import com.cafepickuporder.android.ui.theme.CafePickupOrderTheme

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

    when (screen) {
        "login" -> LoginScreen(
            onLoginSuccess = { screen = "mypage" },
            onMoveToSignup = { screen = "signup" }
        )

        "signup" -> SignupScreen(
            onMoveToLogin = { screen = "login" }
        )

        "mypage" -> MyPageScreen(
            onLogout = { screen = "login" }
        )
    }
}