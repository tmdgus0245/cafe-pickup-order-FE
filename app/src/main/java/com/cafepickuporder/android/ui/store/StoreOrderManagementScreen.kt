package com.cafepickuporder.android.ui.store

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.OrderRejectRequest
import com.cafepickuporder.android.data.response.StoreOrderResponse
import kotlinx.coroutines.launch

@Composable
fun StoreOrderManagementScreen(
    storeId: Long,
    accessToken: String,
    onLogout: () -> Unit
) {
    var orders by remember { mutableStateOf<List<StoreOrderResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var processingOrderId by remember { mutableStateOf<Long?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rejectTarget by remember { mutableStateOf<StoreOrderResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val authorization = "Bearer $accessToken"

    fun loadOrders() {
        coroutineScope.launch {
            isLoading = true
            errorMessage = null

            try {
                orders = ApiClient.storeOrderApi.getStoreOrders(storeId, authorization)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    fun changeOrderStatus(
        orderId: Long,
        action: suspend () -> StoreOrderResponse
    ) {
        coroutineScope.launch {
            processingOrderId = orderId
            errorMessage = null

            try {
                action()
                loadOrders()
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                processingOrderId = null
            }
        }
    }

    LaunchedEffect(storeId, accessToken) {
        if (storeId > 0L && accessToken.isNotBlank()) {
            loadOrders()
        } else {
            isLoading = false
            errorMessage = "매장 로그인 정보가 없습니다."
        }
    }

    rejectTarget?.let { order ->
        RejectOrderDialog(
            orderNumber = order.orderNumber,
            isProcessing = processingOrderId == order.orderId,
            onDismiss = {
                if (processingOrderId == null) {
                    rejectTarget = null
                }
            },
            onConfirm = { reason ->
                changeOrderStatus(order.orderId) {
                    ApiClient.storeOrderApi.rejectOrder(
                        storeId = storeId,
                        orderId = order.orderId,
                        authorization = authorization,
                        request = OrderRejectRequest(reason)
                    )
                }
                rejectTarget = null
            }
        )
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "매장 주문 관리",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "접수부터 픽업 완료까지 주문 상태를 처리하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onLogout) {
                Text("로그아웃")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = "매장 주문을 불러오지 못했습니다.\n$errorMessage",
                    color = MaterialTheme.colorScheme.error
                )
            }

            orders.isEmpty() -> {
                Text("처리할 주문이 없습니다.")
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        StoreOrderCard(
                            order = order,
                            isProcessing = processingOrderId == order.orderId,
                            onAccept = {
                                changeOrderStatus(order.orderId) {
                                    ApiClient.storeOrderApi.acceptOrder(storeId, order.orderId, authorization)
                                }
                            },
                            onReady = {
                                changeOrderStatus(order.orderId) {
                                    ApiClient.storeOrderApi.markReady(storeId, order.orderId, authorization)
                                }
                            },
                            onComplete = {
                                changeOrderStatus(order.orderId) {
                                    ApiClient.storeOrderApi.completeOrder(storeId, order.orderId, authorization)
                                }
                            },
                            onReject = {
                                rejectTarget = order
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoreOrderCard(
    order: StoreOrderResponse,
    isProcessing: Boolean,
    onAccept: () -> Unit,
    onReady: () -> Unit,
    onComplete: () -> Unit,
    onReject: () -> Unit
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
                        text = order.customerName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "주문번호 ${order.orderNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StoreOrderStatusBadge(status = order.status)
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

            Spacer(modifier = Modifier.height(12.dp))

            when (order.status) {
                "REQUESTED" -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onAccept,
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (isProcessing) "처리 중..." else "수락")
                        }

                        OutlinedButton(
                            onClick = onReject,
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("거절")
                        }
                    }
                }

                "ACCEPTED" -> {
                    Button(
                        onClick = onReady,
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isProcessing) "처리 중..." else "준비 완료")
                    }
                }

                "READY" -> {
                    Button(
                        onClick = onComplete,
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isProcessing) "처리 중..." else "픽업 완료")
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectOrderDialog(
    orderNumber: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("매장 사정") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("주문을 거절할까요?")
        },
        text = {
            Column {
                Text("주문번호 $orderNumber")

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("거절 사유") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.ifBlank { "매장 사정" })
                },
                enabled = !isProcessing
            ) {
                Text(if (isProcessing) "처리 중..." else "거절하기")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isProcessing
            ) {
                Text("닫기")
            }
        }
    )
}

@Composable
private fun StoreOrderStatusBadge(status: String) {
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
