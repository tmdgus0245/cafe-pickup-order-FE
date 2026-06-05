package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.response.MenuDetailResponse
import com.cafepickuporder.android.data.response.MenuResponse
import com.cafepickuporder.android.data.response.StoreListResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface StoreApi {

    @GET("/api/stores")
    suspend fun getStores(): List<StoreListResponse>

    @GET("/api/stores/{storeId}/menus")
    suspend fun getMenus(
        @Path("storeId") storeId: Long
    ): List<MenuResponse>

    @GET("/api/stores/{storeId}/menus/{menuId}")
    suspend fun getMenuDetail(
        @Path("storeId") storeId: Long,
        @Path("menuId") menuId: Long
    ): MenuDetailResponse
}