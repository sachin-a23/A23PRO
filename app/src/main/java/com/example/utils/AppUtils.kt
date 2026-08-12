package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

fun copyTextToClipboard(context: Context, text: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Jarvis Response", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text copy ho gaya!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun saveReportToPhoneStorage(context: Context, fileName: String, fileContent: String): Boolean {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { 
            it.write(fileContent.toByteArray(Charsets.UTF_8)) 
        }
        Toast.makeText(context, "✅ Report storage me save ho gaya: Downloads/$fileName", Toast.LENGTH_LONG).show()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun testApiConnection(baseUrl: String, apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
    try {
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val urlStr = if (cleanBaseUrl.endsWith("/models")) cleanBaseUrl else "$cleanBaseUrl/models"
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        
        val responseCode = conn.responseCode
        if (responseCode == 200) {
            Pair(true, "API Connected Successfully (Online)")
        } else {
            Pair(false, "Connection Error: HTTP Status $responseCode")
        }
    } catch (e: Exception) {
        Pair(false, "Network Error: ${e.localizedMessage ?: "Check your internet connection"}")
    }
}
