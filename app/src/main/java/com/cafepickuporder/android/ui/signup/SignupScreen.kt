package com.cafepickuporder.android.ui.signup

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
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.SignupRequest
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    onMoveToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(24.dp)
    ) {
        Text(text = "회원가입")

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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("이름") }
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("전화번호") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val response = ApiClient.authApi.signup(
                            SignupRequest(
                                email = email,
                                password = password,
                                name = name,
                                phone = phone
                            )
                        )

                        message = if (response.isSuccessful) {
                            "회원가입 성공"
                        } else {
                            "회원가입 실패: ${response.code()}"
                        }
                    } catch (e: Exception) {
                        message = "서버 연결 실패: ${e.message}"
                    }
                }
            }
        ) {
            Text("회원가입")
        }

        TextButton(onClick = onMoveToLogin) {
            Text("이미 계정이 있나요? 로그인")
        }

        Text(text = message)
    }
}