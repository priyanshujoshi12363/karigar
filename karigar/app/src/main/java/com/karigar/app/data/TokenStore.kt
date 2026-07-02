package com.karigar.app.data

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("karigar_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    var onboardingSeen: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()
        }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_ONBOARDED = "onboarding_seen"
    }
}
