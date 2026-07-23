package com.cafepickuporder.android.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.LoginRequest
import com.cafepickuporder.android.data.request.StoreAccountLoginRequest
import com.cafepickuporder.android.local.TokenManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import kotlinx.coroutines.launch

private enum class LoginMode {
    Customer,
    Owner
}

@Composable
fun LoginScreen(
    initialOwnerMode: Boolean = false,
    onLoginSuccess: (Long) -> Unit,
    onOwnerLoginSuccess: (Long, String) -> Unit,
    onMoveToSignup: () -> Unit,
    onMoveToOwnerSignup: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var mode by remember(initialOwnerMode) {
        mutableStateOf(
            if (initialOwnerMode) LoginMode.Owner else LoginMode.Customer
        )
    }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = if (mode == LoginMode.Customer) "로그인" else "사장님 로그인",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (mode == LoginMode.Customer) {
                "가까운 카페에 미리 주문하고 바로 픽업하세요."
            } else {
                "매장 주문을 접수하고 메뉴를 관리하세요."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoginModeToggle(
            mode = mode,
            onModeSelected = {
                mode = it
                message = ""
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    message = ""

                    try {
                        if (mode == LoginMode.Customer) {
                            val response = ApiClient.authApi.login(
                                LoginRequest(
                                    email = email,
                                    password = password
                                )
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val loginResponse = response.body()!!
                                tokenManager.saveAccessToken(loginResponse.accessToken)
                                tokenManager.saveCustomerId(loginResponse.customerId)
                                tokenManager.saveCustomerName(loginResponse.name)
                                onLoginSuccess(loginResponse.customerId)
                            } else {
                                message = "로그인 실패: ${response.code()}"
                            }
                        } else {
                            val response = ApiClient.storeAccountApi.login(
                                StoreAccountLoginRequest(
                                    email = email,
                                    password = password
                                )
                            )

                            if (response.isSuccessful && response.body() != null) {
                                val loginResponse = response.body()!!
                                tokenManager.saveStoreSession(
                                    token = loginResponse.token,
                                    storeId = loginResponse.storeId
                                )
                                onOwnerLoginSuccess(loginResponse.storeId, loginResponse.token)
                            } else {
                                message = "사장님 로그인 실패: ${response.code()}"
                            }
                        }
                    } catch (e: Exception) {
                        message = "서버 연결 실패: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when {
                    isLoading -> "로그인 중..."
                    mode == LoginMode.Customer -> "로그인"
                    else -> "사장님 로그인"
                }
            )
        }

        if (mode == LoginMode.Customer) {
            TextButton(onClick = onMoveToSignup) {
                Text("계정이 없나요? 회원가입")
            }
        } else {
            TextButton(onClick = onMoveToOwnerSignup) {
                Text("사장님 계정 만들기")
            }
        }

        if (message.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun LoginModeToggle(
    mode: LoginMode,
    onModeSelected: (LoginMode) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = PageGray
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            LoginModeChip(
                text = "고객",
                selected = mode == LoginMode.Customer,
                onClick = { onModeSelected(LoginMode.Customer) },
                modifier = Modifier.weight(1f)
            )

            LoginModeChip(
                text = "사장님",
                selected = mode == LoginMode.Owner,
                onClick = { onModeSelected(LoginMode.Owner) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun LoginModeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PassOrange else Color.Transparent
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
