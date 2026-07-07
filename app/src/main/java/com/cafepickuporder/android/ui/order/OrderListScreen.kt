package com.cafepickuporder.android.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.OrderCancelRequest
import com.cafepickuporder.android.data.response.OrderListResponse
import kotlinx.coroutines.launch

@Composable
fun OrderListScreen(
    modifier: Modifier = Modifier,
    customerId: Long,
    onBackClick: () -> Unit
) {
    var orders by remember { mutableStateOf<List<OrderListResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var cancelingOrderId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cancelTarget by remember { mutableStateOf<OrderListResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun loadOrders() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                orders = ApiClient.orderApi.getOrders(customerId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(customerId) {
        if (customerId > 0L) {
            loadOrders()
        } else {
            isLoading = false
            errorMessage = "로그인 정보가 없습니다. 다시 로그인해 주세요."
        }
    }

    cancelTarget?.let { order ->
        CancelOrderDialog(
            orderNumber = order.orderNumber,
            isCanceling = cancelingOrderId == order.orderId,
            onDismiss = {
                if (cancelingOrderId == null) {
                    cancelTarget = null
                }
            },
            onConfirm = { reason ->
                coroutineScope.launch {
                    cancelingOrderId = order.orderId
                    errorMessage = null

                    try {
                        ApiClient.orderApi.cancelOrder(
                            orderId = order.orderId,
                            customerId = customerId,
                            request = OrderCancelRequest(reason)
                        )
                        cancelTarget = null
                        loadOrders()
                    } catch (e: Exception) {
                        errorMessage = e.message
                    } finally {
                        cancelingOrderId = null
                    }
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "주문내역",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Text(
                    text = "주문내역을 불러오지 못했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            orders.isEmpty() -> {
                Text(
                    text = "아직 주문내역이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            isCanceling = cancelingOrderId == order.orderId,
                            onCancelClick = { cancelTarget = order }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderListResponse,
    isCanceling: Boolean,
    onCancelClick: () -> Unit
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
                text = "결제 금액 ${formatPrice(order.totalPrice)}",
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

            if (order.status == "REQUESTED") {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onCancelClick,
                    enabled = !isCanceling,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isCanceling) "취소 처리 중..." else "주문 취소")
                }
            }
        }
    }
}

@Composable
private fun CancelOrderDialog(
    orderNumber: String,
    isCanceling: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("고객 요청") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("주문을 취소할까요?")
        },
        text = {
            Column {
                Text("주문번호 $orderNumber")

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("취소 사유") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "고객 요청" })
                },
                enabled = !isCanceling
            ) {
                Text(if (isCanceling) "처리 중..." else "취소하기")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCanceling
            ) {
                Text("닫기")
            }
        }
    )
}

@Composable
private fun OrderStatusBadge(
    status: String
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = when (status) {
            "REQUESTED" -> MaterialTheme.colorScheme.secondaryContainer
            "ACCEPTED", "READY" -> MaterialTheme.colorScheme.primaryContainer
            "COMPLETED" -> MaterialTheme.colorScheme.tertiaryContainer
            "REJECTED", "CANCELED" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = displayOrderStatus(status),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            color = when (status) {
                "REJECTED", "CANCELED" -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

private fun displayOrderStatus(status: String): String {
    return when (status) {
        "REQUESTED" -> "주문 요청"
        "ACCEPTED" -> "주문 수락"
        "READY" -> "픽업 준비 완료"
        "COMPLETED" -> "픽업 완료"
        "REJECTED" -> "주문 거절"
        "CANCELED" -> "주문 취소"
        else -> status
    }
}

private fun formatDateTime(value: String?): String {
    if (value.isNullOrBlank()) return "-"

    return value
        .replace("T", " ")
        .take(16)
}

private fun formatPrice(value: Int): String {
    return "%,d원".format(value)
}
