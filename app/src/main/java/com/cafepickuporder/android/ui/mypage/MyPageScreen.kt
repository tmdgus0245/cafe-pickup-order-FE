package com.cafepickuporder.android.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.local.TokenManager
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange

@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }

    var customerIdText by remember { mutableStateOf("불러오는 중") }

    LaunchedEffect(Unit) {
        try {
            val token = tokenManager.getAccessToken()

            if (token == null) {
                customerIdText = "로그인 정보 없음"
                return@LaunchedEffect
            }

            val response = ApiClient.authApi.getMyInfo("Bearer $token")

            customerIdText = if (response.isSuccessful && response.body() != null) {
                "고객 ID ${response.body()!!.customerId}"
            } else {
                "내 정보 조회 실패"
            }
        } catch (e: Exception) {
            customerIdText = "서버 연결 실패"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text(
            text = "내정보",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(34.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD6E7F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "나",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.size(18.dp))

            Column {
                Text(
                    text = "카페 픽업 고객님",
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = customerIdText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryBox("주문", "빠른 픽업", Modifier.weight(1f))
            SummaryBox("포인트", "준비 중", Modifier.weight(1f))
            SummaryBox("스탬프", "준비 중", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SoftOrange
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "자주 쓰는 주문을 더 빠르게",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "카드를 미리 등록하는 기능은 MVP 범위 밖이라 제외했습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "마이 페이지",
            style = MaterialTheme.typography.titleSmall,
            color = Muted,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        MyPageRow("내 주문내역", "주문내역 탭에서 확인")
        MyPageRow("자주 가는 매장", "자주가요 탭에서 확인")
        MyPageRow("알림 설정", "준비 중")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                tokenManager.clearAccessToken()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("로그아웃")
        }
    }
}

@Composable
private fun SummaryBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = PassOrange,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MyPageRow(
    title: String,
    trailing: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = trailing,
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )
    }
}
