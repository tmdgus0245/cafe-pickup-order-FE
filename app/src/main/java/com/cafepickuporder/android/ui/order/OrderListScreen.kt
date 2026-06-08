package com.cafepickuporder.android.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.OrderListResponse

@Composable
fun OrderListScreen(
    customerId: Long,
    onBackClick: () -> Unit
) {
    var orders by remember { mutableStateOf<List<OrderListResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(customerId) {
        try {
            orders = ApiClient.orderApi.getOrders(customerId)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBackClick
            ) {
                Text("←")
            }

            Column {
                Text(
                    text = "주문 내역",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "최근 주문 내역을 확인할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = "주문 내역을 불러오지 못했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            orders.isEmpty() -> {
                Text(
                    text = "아직 주문 내역이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderListResponse
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = order.storeName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "주문번호 ${order.orderNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "결제 예정 금액 ${order.totalPrice}원",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "주문 시간 ${formatDateTime(order.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!order.estimatedPickupTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "예상 픽업 시간 ${formatDateTime(order.estimatedPickupTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OrderStatusBadge(
    status: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = displayOrderStatus(status),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun displayOrderStatus(status: String): String {
    return when (status) {
        "REQUESTED" -> "주문 완료"
        "ACCEPTED" -> "접수 완료"
        "READY" -> "픽업 준비 완료"
        "COMPLETED" -> "픽업 완료"
        "REJECTED" -> "거절됨"
        "CANCELED" -> "취소됨"
        else -> status
    }
}

private fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"

    return value
        .replace("T", " ")
        .take(16)
}