package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.OrderCreateRequest
import com.cafepickuporder.android.data.request.OrderCancelRequest
import com.cafepickuporder.android.data.response.OrderCreateResponse
import com.cafepickuporder.android.data.response.OrderListResponse
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface OrderApi {

    @POST("/api/orders")
    suspend fun createOrder(
        @Query("customerId") customerId: Long,
        @Body request: OrderCreateRequest
    ): OrderCreateResponse

    @PATCH("/api/orders/{orderId}/cancel")
    suspend fun cancelOrder(
        @Path("orderId") orderId: Long,
        @Query("customerId") customerId: Long,
        @Body request: OrderCancelRequest
    ): OrderCreateResponse

    @GET("/api/orders")
    suspend fun getOrders(
        @Query("customerId") customerId: Long
    ): List<OrderListResponse>
}
