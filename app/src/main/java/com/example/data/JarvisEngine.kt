package com.example.data

import android.content.Context

class JarvisEngine(private val context: Context) {

    // Web portal ya local storage se saved market data uthana
    private fun getWebsiteMarketContext(): String {
        val secureStorage = SecureStorageManager(context)
        val sridevi = secureStorage.getSecureData("a23_market_sridevi") ?: "No data"
        val kalyan = secureStorage.getSecureData("a23_market_kalyan") ?: "No data"
        val syncedPayload = secureStorage.getSecureData("synced_market_payload") ?: "No data"
        
        return """
            [WEB PORTAL LIVE MARKET DATA CONTEXT]
            - Sridevi Record: $sridevi
            - Kalyan Record: $kalyan
            - Synced Payload: ${syncedPayload.take(250)}
        """.trimIndent()
    }

    suspend fun askJarvis(userQuery: String, apiKey: String, baseUrl: String = "https://api.openai.com/v1", model: String = "gpt-4o-mini"): String {
        val systemContext = "Aap A23 PRO ke autonomous AI core (Jarvis) hain. Neeche diye gaye website data aur user request ke aadhar par answer dein:"
        val websiteData = getWebsiteMarketContext()
        
        val enhancedPrompt = "$systemContext\n$websiteData\n\nUser Query: $userQuery"

        return GeminiApiClient.generateContentCustom(
            prompt = enhancedPrompt,
            baseUrl = baseUrl,
            apiKey = apiKey,
            model = model
        )
    }
}
