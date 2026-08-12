package com.example.data

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("a23_auth_prefs", Context.MODE_PRIVATE)

    fun getCurrentUserEmail(): String? {
        return prefs.getString("user_email", null)
    }

    fun isLoggedIn(): Boolean {
        return getCurrentUserEmail() != null
    }

    fun loginUser(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (email.isNotBlank() && pass.length >= 4) {
            prefs.edit().putString("user_email", email).apply()
            onResult(true, "Login Successful!")
        } else {
            onResult(false, "Login Failed: Invalid Email or Password")
        }
    }

    fun registerUser(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (email.contains("@") && pass.length >= 4) {
            prefs.edit().putString("user_email", email).apply()
            onResult(true, "Registration Successful!")
        } else {
            onResult(false, "Registration Failed: Invalid Email or Password")
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
