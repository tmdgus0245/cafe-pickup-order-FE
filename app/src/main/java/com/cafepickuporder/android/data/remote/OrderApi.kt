package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.OrderCreateRequest
import com.cafepickuporder.android.data.response.OrderCreateResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderApi {

    @POST("/api/orders")
    suspend fun createOrder(
        @Query("customerId") customerId: Long,
        @Body request: OrderCreateRequest
    ): OrderCreateResponse
}