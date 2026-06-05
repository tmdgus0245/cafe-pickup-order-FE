package com.cafepickuporder.android.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.LoginRequest
import com.cafepickuporder.android.local.TokenManager
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onMoveToSignup: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(text = "로그인")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val response = ApiClient.authApi.login(
                            LoginRequest(
                                email = email,
                                password = password
                            )
                        )

                        if (response.isSuccessful && response.body() != null) {
                            val loginResponse = response.body()!!
                            tokenManager.saveAccessToken(loginResponse.accessToken)
                            message = "로그인 성공"
                            onLoginSuccess()
                        } else {
                            message = "로그인 실패: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        message = "서버 연결 실패: ${e.message}"
                    }
                }
            }
        ) {
            Text("로그인")
        }

        TextButton(onClick = onMoveToSignup) {
            Text("계정이 없나요? 회원가입")
        }

        Text(text = message)
    }
}