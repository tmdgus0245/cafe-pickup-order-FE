package com.cafepickuporder.android.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.request.OrderCancelRequest
import com.cafepickuporder.android.data.response.OrderListResponse
import com.cafepickuporder.android.ui.theme.Ink
import com.cafepickuporder.android.ui.theme.Muted
import com.cafepickuporder.android.ui.theme.PageGray
import com.cafepickuporder.android.ui.theme.PassOrange
import com.cafepickuporder.android.ui.theme.SoftOrange
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
            .background(Color(0xFFFFFBF8))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "주문내역",
                    color = Ink,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "주문 상태와 픽업 시간을 확인하세요.",
                    color = Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (orders.isNotEmpty()) {
                Surface(
                    color = SoftOrange,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "${orders.size}건",
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        color = PassOrange,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PassOrange)
                }
            }

            errorMessage != null -> {
                Surface(
                    modifier = Modifier.padding(20.dp),
                    color = Color(0xFFFFEEEE),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "주문내역을 불러오지 못했습니다.\n$errorMessage",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            orders.isEmpty() -> {
                EmptyOrders()
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.orderId }) { order ->
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        color = Ink,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "주문번호 ${order.orderNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted
                    )
                }

                OrderStatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(15.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SoftOrange,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "결제 금액",
                        color = Ink,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatPrice(order.totalPrice),
                        color = PassOrange,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            OrderTimeRow(
                label = "주문 시간",
                value = formatDateTime(order.createdAt),
                highlighted = false
            )

            if (!order.estimatedPickupTime.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(7.dp))
                OrderTimeRow(
                    label = "예상 픽업",
                    value = formatDateTime(order.estimatedPickupTime),
                    highlighted = true
                )
            }

            if (order.status == "REQUESTED") {
                Spacer(modifier = Modifier.height(15.dp))

                Button(
                    onClick = onCancelClick,
                    enabled = !isCanceling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PassOrange,
                        disabledContainerColor = Color(0xFFF0D7CB)
                    )
                ) {
                    Text(
                        text = if (isCanceling) "취소 처리 중..." else "주문 취소",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderTimeRow(
    label: String,
    value: String,
    highlighted: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (highlighted) PassOrange else Color(0xFFD8CCC6),
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(9.dp))
        Text(
            text = label,
            color = Muted,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = if (highlighted) PassOrange else Ink,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyOrders() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(88.dp),
            color = SoftOrange,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("☕", style = MaterialTheme.typography.displaySmall)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "아직 주문내역이 없어요",
            color = Ink,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "첫 픽업 주문을 시작해보세요.",
            color = Muted,
            style = MaterialTheme.typography.bodyMedium
        )
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
            "REQUESTED" -> Color(0xFFFFE9DE)
            "ACCEPTED" -> Color(0xFFFFF2D8)
            "READY" -> Color(0xFFE3F4EA)
            "COMPLETED" -> PageGray
            "REJECTED", "CANCELED" -> Color(0xFFFFE8E8)
            else -> PageGray
        }
    ) {
        Text(
            text = displayOrderStatus(status),
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = when (status) {
                "REQUESTED" -> PassOrange
                "ACCEPTED" -> Color(0xFFB66B00)
                "READY" -> Color(0xFF24834E)
                "REJECTED", "CANCELED" -> Color(0xFFC74343)
                else -> Muted
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
