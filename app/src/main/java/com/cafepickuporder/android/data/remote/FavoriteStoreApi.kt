package com.cafepickuporder.android.data.remote

import com.cafepickuporder.android.data.response.StoreListResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoriteStoreApi {

    @GET("/api/customers/me/favorite-stores")
    suspend fun getFavoriteStores(
        @Header("Authorization") authorization: String
    ): List<StoreListResponse>

    @POST("/api/customers/me/favorite-stores/{storeId}")
    suspend fun addFavoriteStore(
        @Header("Authorization") authorization: String,
        @Path("storeId") storeId: Long
    ): Response<Unit>

    @DELETE("/api/customers/me/favorite-stores/{storeId}")
    suspend fun removeFavoriteStore(
        @Header("Authorization") authorization: String,
        @Path("storeId") storeId: Long
    ): Response<Unit>
}
