package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PinLockManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("a23_pin_prefs", Context.MODE_PRIVATE)

    fun savePin(pin: String) {
        prefs.edit().putString("app_pin", pin).apply()
    }

    fun verifyPin(enteredPin: String): Boolean {
        val savedPin = prefs.getString("app_pin", null)
        return savedPin == enteredPin
    }

    fun isPinSet(): Boolean {
        return prefs.getString("app_pin", null) != null
    }

    fun clearPin() {
        prefs.edit().remove("app_pin").apply()
    }
}
