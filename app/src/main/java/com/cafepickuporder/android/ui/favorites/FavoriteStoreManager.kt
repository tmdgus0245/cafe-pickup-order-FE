package com.cafepickuporder.android.ui.favorites

import androidx.compose.runtime.mutableStateListOf
import com.cafepickuporder.android.data.remote.ApiClient
import com.cafepickuporder.android.data.response.StoreListResponse

object FavoriteStoreManager {
    val stores = mutableStateListOf<StoreListResponse>()

    fun contains(storeId: Long): Boolean {
        return stores.any { it.storeId == storeId }
    }

    suspend fun refresh(accessToken: String) {
        if (accessToken.isBlank()) return
        val loadedStores = ApiClient.favoriteStoreApi.getFavoriteStores(
            authorization = "Bearer $accessToken"
        )
        stores.clear()
        stores.addAll(loadedStores)
    }

    suspend fun toggle(accessToken: String, storeId: Long): Boolean {
        if (accessToken.isBlank()) return false
        val authorization = "Bearer $accessToken"
        val response = if (contains(storeId)) {
            ApiClient.favoriteStoreApi.removeFavoriteStore(authorization, storeId)
        } else {
            ApiClient.favoriteStoreApi.addFavoriteStore(authorization, storeId)
        }

        if (response.isSuccessful) {
            refresh(accessToken)
            return true
        }
        return false
    }

    fun clear() {
        stores.clear()
    }
}
