package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64

class SecureStorageManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("a23_secure_prefs", Context.MODE_PRIVATE)

    fun saveSecureData(key: String, value: String) {
        val encoded = Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        prefs.edit().putString(key, encoded).apply()
    }

    fun getSecureData(key: String): String? {
        val encoded = prefs.getString(key, null) ?: return null
        return try {
            String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (e: Exception) {
            encoded
        }
    }

    fun clearData(key: String) {
        prefs.edit().remove(key).apply()
    }
}
