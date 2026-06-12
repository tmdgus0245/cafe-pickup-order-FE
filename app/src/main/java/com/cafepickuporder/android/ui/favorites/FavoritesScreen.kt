package com.cafepickuporder.android.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted

@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "자주가요",
            style = MaterialTheme.typography.headlineMedium,
            color = Ink
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "아직 자주 가는 매장이 없습니다.",
            style = MaterialTheme.typography.titleMedium,
            color = Ink
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "주문한 매장이 생기면 이곳에서 빠르게 다시 찾을 수 있게 준비할게요.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted
        )
    }
}
