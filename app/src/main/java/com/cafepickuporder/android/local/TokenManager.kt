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

    fun getAccessToken(): String? {
        return sharedPreferences.getString("accessToken", null)
    }

    fun clearAccessToken() {
        sharedPreferences.edit()
            .remove("accessToken")
            .apply()
    }
}