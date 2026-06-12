package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.request.OrderRejectRequest
import com.cafepickuporder.android.data.response.StoreOrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

interface StoreOrderApi {
    @GET("/api/stores/{storeId}/orders")
    suspend fun getStoreOrders(
        @Path("storeId") storeId: Long,
        @Header("Authorization") authorization: String
    ): List<StoreOrderResponse>

    @PATCH("/api/stores/{storeId}/orders/{orderId}/accept")
    suspend fun acceptOrder(
        @Path("storeId") storeId: Long,
        @Path("orderId") orderId: Long,
        @Header("Authorization") authorization: String
    ): StoreOrderResponse

    @PATCH("/api/stores/{storeId}/orders/{orderId}/ready")
    suspend fun markReady(
        @Path("storeId") storeId: Long,
        @Path("orderId") orderId: Long,
        @Header("Authorization") authorization: String
    ): StoreOrderResponse

    @PATCH("/api/stores/{storeId}/orders/{orderId}/complete")
    suspend fun completeOrder(
        @Path("storeId") storeId: Long,
        @Path("orderId") orderId: Long,
        @Header("Authorization") authorization: String
    ): StoreOrderResponse

    @PATCH("/api/stores/{storeId}/orders/{orderId}/reject")
    suspend fun rejectOrder(
        @Path("storeId") storeId: Long,
        @Path("orderId") orderId: Long,
        @Header("Authorization") authorization: String,
        @Body request: OrderRejectRequest
    ): StoreOrderResponse
}
