package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiApiClient {

    data class ApiTestResult(
        val isSuccess: Boolean,
        val statusCode: Int,
        val latencyMs: Long,
        val report: String,
        val hindiDiagnostic: String
    )

    // Existing Gemini-specific function (supports Gemini Flash/Pro)
    suspend fun generateContent(
        prompt: String,
        activeApiKey: String?,
        base64Image: String? = null,
        mimeType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val rawKey = when {
            !activeApiKey.isNullOrBlank() -> activeApiKey
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> null
        }

        if (rawKey.isNullOrBlank()) {
            return@withContext "NO_KEY"
        }

        val cleanKey = rawKey.trim().removeSurrounding("\"").removeSurrounding("'")
        if (cleanKey.isBlank() || cleanKey == "MY_GEMINI_API_KEY" || cleanKey.contains("Sample")) {
            return@withContext "NO_KEY"
        }

        // If key is OpenAI, Groq or DeepSeek format, route automatically
        if (cleanKey.startsWith("sk-") || cleanKey.startsWith("gsk_") || cleanKey.contains("sk_live")) {
            val endpoint = when {
                cleanKey.startsWith("gsk_") -> "https://api.groq.com/openai/v1"
                cleanKey.startsWith("sk-ds-") -> "https://api.deepseek.com/v1"
                else -> "https://api.openai.com/v1"
            }
            val modelName = when {
                cleanKey.startsWith("gsk_") -> "llama3-70b-8192"
                cleanKey.startsWith("sk-ds-") -> "deepseek-chat"
                else -> "gpt-4o-mini"
            }
            return@withContext generateContentCustom(
                prompt = prompt,
                baseUrl = endpoint,
                apiKey = cleanKey,
                model = modelName
            )
        }

        // Dynamic model discovery for Gemini API to avoid 404 errors
        val discoveredModels = fetchActiveGeminiModels(cleanKey)
        val modelsToTry = if (discoveredModels.isNotEmpty()) {
            discoveredModels
        } else {
            listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-flash-8b")
        }
        var lastError = ""

        for (model in modelsToTry) {
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.setRequestProperty("x-goog-api-key", cleanKey)
                conn.connectTimeout = 15000
                conn.readTimeout = 25000
                conn.doOutput = true

                val systemContext = "You are AI-Jarvis, a powerful, highly intelligent AI assistant built into A23 PRO. Answer user queries accurately, clearly, and naturally in Hindi, English, or Hinglish as requested. Follow the user's instructions strictly."

                val partsArray = JSONArray()

                // Add text prompt part
                val fullTextPrompt = "$systemContext\n\nUser Question: $prompt"
                partsArray.put(JSONObject().put("text", fullTextPrompt))

                // Add image inline data part if present
                if (!base64Image.isNullOrBlank()) {
                    val inlineData = JSONObject().apply {
                        put("mime_type", mimeType ?: "image/jpeg")
                        put("data", base64Image)
                    }
                    partsArray.put(JSONObject().put("inline_data", inlineData))
                }

                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", partsArray)
                        })
                    })
                }

                val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
                writer.write(jsonBody.toString())
                writer.flush()
                writer.close()

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val jsonResp = JSONObject(sb.toString())
                    val candidates = jsonResp.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val responseSb = StringBuilder()
                            for (i in 0 until parts.length()) {
                                val pText = parts.getJSONObject(i).optString("text", "")
                                if (pText.isNotBlank()) {
                                    responseSb.append(pText)
                                }
                            }
                            if (responseSb.isNotEmpty()) {
                                return@withContext responseSb.toString().trim()
                            }
                        }
                    }
                    return@withContext "Empty response received from Gemini AI."
                } else {
                    val errorDetail = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    lastError = "API Error (Http $responseCode): $errorDetail"
                }
            } catch (e: Exception) {
                lastError = "Network Exception: ${e.localizedMessage}"
            }
        }

        // If Gemini failed but key exists, try custom OpenAI format as secondary fallback
        if (lastError.contains("API Error") || lastError.contains("400")) {
            val customResp = generateContentCustom(
                prompt = prompt,
                baseUrl = "https://api.openai.com/v1",
                apiKey = cleanKey,
                model = "gpt-4o-mini"
            )
            if (!customResp.startsWith("API Error") && !customResp.startsWith("Network Exception")) {
                return@withContext customResp
            }
        }

        return@withContext lastError
    }

    // Generic function for any OpenAI-compatible API (OpenAI, Groq, DeepSeek, Custom REST)
    suspend fun generateContentCustom(
        prompt: String,
        baseUrl: String = "https://api.openai.com/v1",
        apiKey: String,
        model: String = "gpt-4o-mini"
    ): String = withContext(Dispatchers.IO) {
        try {
            val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
            val urlStr = if (cleanBaseUrl.endsWith("/chat/completions")) cleanBaseUrl else "$cleanBaseUrl/chat/completions"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
            conn.connectTimeout = 15000
            conn.readTimeout = 25000
            conn.doOutput = true

            val systemInstruction = "You are AI-Jarvis built into A23 PRO. Answer the user accurately and clearly in Hindi, English, or Hinglish as requested."

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemInstruction)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            val jsonBody = JSONObject().apply {
                put("model", model)
                put("messages", messagesArray)
            }

            val writer = OutputStreamWriter(conn.outputStream, "UTF-8")
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val jsonResp = JSONObject(sb.toString())
                val choices = jsonResp.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    val content = message?.optString("content", "") ?: ""
                    if (content.isNotBlank()) return@withContext content.trim()
                }
                return@withContext "Empty response received."
            } else {
                val errorDetail = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                return@withContext "API Error (Http $responseCode): $errorDetail"
            }
        } catch (e: Exception) {
            return@withContext "Network Exception: ${e.localizedMessage}"
        }
    }

    suspend fun testApiKeyAdvanced(
        apiKey: String,
        provider: String = "Google AI Studio",
        baseUrl: String = "",
        modelName: String = ""
    ): ApiTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val cleanKey = apiKey.trim().removeSurrounding("\"").removeSurrounding("'")

        if (cleanKey.isBlank() || cleanKey == "MY_GEMINI_API_KEY" || cleanKey.contains("Sample")) {
            return@withContext ApiTestResult(
                isSuccess = false,
                statusCode = 400,
                latencyMs = 0L,
                report = "🔴 400 Bad Request • Empty or Placeholder Key",
                hindiDiagnostic = "Aapne API Key enter nahi ki hai ya sample key lagi hai. Kripya real API Key enter karein."
            )
        }

        // Test 1: If Google AI Studio / Gemini
        if (provider.contains("Gemini") || provider.contains("Google") || (!cleanKey.startsWith("sk-") && !cleanKey.startsWith("gsk_") && baseUrl.isBlank())) {
            try {
                val urlString = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("x-goog-api-key", cleanKey)
                conn.connectTimeout = 8000
                conn.readTimeout = 10000

                val responseCode = conn.responseCode
                val latency = System.currentTimeMillis() - startTime

                if (responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line)
                    }
                    reader.close()

                    val jsonResp = JSONObject(sb.toString())
                    val modelsArray = jsonResp.optJSONArray("models")
                    val count = modelsArray?.length() ?: 0

                    return@withContext ApiTestResult(
                        isSuccess = true,
                        statusCode = 200,
                        latencyMs = latency,
                        report = "🟢 200 OK (${latency}ms) • Google AI Studio Connected ($count Models)",
                        hindiDiagnostic = "✅ Sahi hai! Google AI Studio Gemini API bilkul mast kaam kar rahi hai. Latency: ${latency}ms."
                    )
                } else if (responseCode == 400 || responseCode == 403 || responseCode == 401) {
                    return@withContext ApiTestResult(
                        isSuccess = false,
                        statusCode = responseCode,
                        latencyMs = latency,
                        report = "🔴 $responseCode Unauthorized / Invalid Key",
                        hindiDiagnostic = "❌ API Key Galat Hai! Kripya Google AI Studio (aistudio.google.com) se nayi key generate karke paste karein."
                    )
                } else if (responseCode == 429) {
                    return@withContext ApiTestResult(
                        isSuccess = false,
                        statusCode = 429,
                        latencyMs = latency,
                        report = "🔴 429 Quota Exceeded / Rate Limit",
                        hindiDiagnostic = "⚠️ Free Quota Finish! Aapki API Key ki daily limit khatam ho gayi hai. Thodi der baad try karein ya doosri key dalein."
                    )
                } else {
                    return@withContext ApiTestResult(
                        isSuccess = false,
                        statusCode = responseCode,
                        latencyMs = latency,
                        report = "🔴 Http $responseCode Error",
                        hindiDiagnostic = "⚠️ Google AI Studio Server Error (Code $responseCode). Thodi der baad wapas test karein."
                    )
                }
            } catch (e: Exception) {
                val latency = System.currentTimeMillis() - startTime
                return@withContext ApiTestResult(
                    isSuccess = false,
                    statusCode = 503,
                    latencyMs = latency,
                    report = "🔴 Network Exception: ${e.localizedMessage}",
                    hindiDiagnostic = "🌐 Network Timeout! Kripya apna mobile internet/Wi-Fi connection check karein."
                )
            }
        }

        // Test 2: OpenAI, Groq, DeepSeek, or Custom REST API
        val targetBaseUrl = when {
            baseUrl.isNotBlank() -> baseUrl.trim()
            cleanKey.startsWith("gsk_") || provider.contains("Groq") -> "https://api.groq.com/openai/v1"
            cleanKey.startsWith("sk-ds-") || provider.contains("DeepSeek") -> "https://api.deepseek.com/v1"
            else -> "https://api.openai.com/v1"
        }
        val targetModel = when {
            modelName.isNotBlank() -> modelName.trim()
            cleanKey.startsWith("gsk_") -> "llama3-8b-8192"
            cleanKey.startsWith("sk-ds-") -> "deepseek-chat"
            else -> "gpt-4o-mini"
        }

        try {
            val res = generateContentCustom("Hello test ping", targetBaseUrl, cleanKey, targetModel)
            val latency = System.currentTimeMillis() - startTime

            if (!res.startsWith("API Error") && !res.startsWith("Network Exception")) {
                return@withContext ApiTestResult(
                    isSuccess = true,
                    statusCode = 200,
                    latencyMs = latency,
                    report = "🟢 200 OK (${latency}ms) • $provider REST Endpoint Active",
                    hindiDiagnostic = "✅ Connection Successful! $provider API response mila (${latency}ms)."
                )
            } else if (res.contains("401") || res.contains("Unauthorized")) {
                return@withContext ApiTestResult(
                    isSuccess = false,
                    statusCode = 401,
                    latencyMs = latency,
                    report = "🔴 401 Unauthorized API Key",
                    hindiDiagnostic = "❌ $provider ki API Key galat ya expired hai. $provider Dashboard se sahi key lekar aayein."
                )
            } else if (res.contains("429")) {
                return@withContext ApiTestResult(
                    isSuccess = false,
                    statusCode = 429,
                    latencyMs = latency,
                    report = "🔴 429 Rate Limit / No Credits",
                    hindiDiagnostic = "⚠️ $provider account me credits ya rate limit exceed ho chuka hai."
                )
            } else {
                return@withContext ApiTestResult(
                    isSuccess = false,
                    statusCode = 400,
                    latencyMs = latency,
                    report = "🔴 API Test Failed: $res",
                    hindiDiagnostic = "⚠️ Connection Failed: Endpoint '$targetBaseUrl' par model '$targetModel' se response nahi mila."
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            return@withContext ApiTestResult(
                isSuccess = false,
                statusCode = 503,
                latencyMs = latency,
                report = "🔴 Connection Error: ${e.localizedMessage}",
                hindiDiagnostic = "🌐 Network Error! URL '$targetBaseUrl' tak pahunch nahi paye."
            )
        }
    }

    suspend fun testApiKey(apiKey: String): Pair<Boolean, String> {
        val result = testApiKeyAdvanced(apiKey)
        return Pair(result.isSuccess, result.report)
    }

    private fun fetchActiveGeminiModels(apiKey: String): List<String> {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("x-goog-api-key", apiKey)
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val jsonResp = JSONObject(sb.toString())
                val modelsArray = jsonResp.optJSONArray("models")
                val list = mutableListOf<String>()
                if (modelsArray != null) {
                    for (i in 0 until modelsArray.length()) {
                        val m = modelsArray.optJSONObject(i)
                        val name = m?.optString("name", "") ?: ""
                        val methods = m?.optJSONArray("supportedGenerationMethods")
                        var supportsGenerate = false
                        if (methods != null) {
                            for (j in 0 until methods.length()) {
                                if (methods.optString(j) == "generateContent") {
                                    supportsGenerate = true
                                    break
                                }
                            }
                        }
                        if (supportsGenerate && name.contains("gemini")) {
                            list.add(name.replace("models/", ""))
                        }
                    }
                }
                if (list.isNotEmpty()) {
                    return list.sortedByDescending { it.contains("flash") }
                }
            }
        } catch (_: Exception) {}
        return emptyList()
    }
}



