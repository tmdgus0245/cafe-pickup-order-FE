package com.cafepickuporder.android.ui.mypage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.local.TokenManager

@Composable
fun MyPageScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var message by remember { mutableStateOf("내 정보 불러오는 중...") }

    LaunchedEffect(Unit) {
        try {
            val token = tokenManager.getAccessToken()

            if (token == null) {
                message = "토큰이 없습니다."
                return@LaunchedEffect
            }

            val response = ApiClient.authApi.getMyInfo("Bearer $token")

            message = if (response.isSuccessful && response.body() != null) {
                "고객 ID: ${response.body()!!.customerId}"
            } else {
                "내 정보 조회 실패: ${response.code()}"
            }
        } catch (e: Exception) {
            message = "서버 연결 실패: ${e.message}"
        }
    }

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(text = "내 정보")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = message)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                tokenManager.clearAccessToken()
                onLogout()
            }
        ) {
            Text("로그아웃")
        }
    }
}