package com.cafepickuporder.android.local

import android.content.Context

class TokenManager(context: Context) {

    private val sharedPreferences =
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveAccessToken(token: String) {
        sharedPreferences.edit()
            .putString("accessToken", token)
            .apply()
    }

    fun saveCustomerId(customerId: Long) {
        sharedPreferences.edit()
            .putLong("customerId", customerId)
            .apply()
    }

    fun saveStoreSession(token: String, storeId: Long) {
        sharedPreferences.edit()
            .putString("storeAccessToken", token)
            .putLong("storeId", storeId)
            .apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("accessToken", null)
    }

    fun getCustomerId(): Long? {
        val customerId = sharedPreferences.getLong("customerId", -1L)
        return if (customerId == -1L) null else customerId
    }

    fun getStoreAccessToken(): String? {
        return sharedPreferences.getString("storeAccessToken", null)
    }

    fun getStoreId(): Long? {
        val storeId = sharedPreferences.getLong("storeId", -1L)
        return if (storeId == -1L) null else storeId
    }

    fun clearAccessToken() {
        sharedPreferences.edit()
            .remove("accessToken")
            .remove("customerId")
            .remove("storeAccessToken")
            .remove("storeId")
            .apply()
    }
}
