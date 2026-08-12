package com.example.data

import android.content.Context
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MarketDataSync(
    private val context: Context,
    private val repository: MarketRepository? = null
) {
    // List of candidate URLs to attempt syncing from GitHub portal
    private val candidateUrls = listOf(
        "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
        "https://sachin-a23.github.io/A23site/data.json",
        "https://raw.githubusercontent.com/sachin-a23/A23site/main/market_data.json",
        "https://sachin-a23.github.io/A23site/market_data.json",
        "https://raw.githubusercontent.com/sachin-a23/A23site/main/sridevi.txt",
        "https://sachin-a23.github.io/A23site/sridevi.txt",
        "https://sachin-a23.github.io/A23site/market-data.txt"
    )

    suspend fun fetchAndSyncData(onComplete: (Boolean, String) -> Unit) {
        try {
            var fetchedPayload: String? = null
            var successfulUrl: String? = null
            var lastError: String? = null

            withContext(Dispatchers.IO) {
                for (urlStr in candidateUrls) {
                    try {
                        val url = URL(urlStr)
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 10000
                        conn.readTimeout = 12000
                        conn.instanceFollowRedirects = true
                        
                        val code = conn.responseCode
                        if (code == 200) {
                            val text = conn.inputStream.bufferedReader().use { it.readText() }
                            if (text.isNotBlank()) {
                                fetchedPayload = text
                                successfulUrl = urlStr
                                break
                            }
                        } else {
                            lastError = "HTTP $code from $urlStr"
                        }
                    } catch (e: Exception) {
                        lastError = "${e.localizedMessage} ($urlStr)"
                    }
                }
            }

            if (fetchedPayload == null) {
                withContext(Dispatchers.Main) {
                    onComplete(false, "Sync failed: Unable to fetch data from GitHub ($lastError)")
                }
                return
            }

            val payload = fetchedPayload!!

            // Save payload into secure local storage
            val secureStorage = SecureStorageManager(context)
            secureStorage.saveSecureData("synced_market_payload", payload)

            // Also save payload into phone local app storage
            withContext(Dispatchers.IO) {
                try {
                    val localFile = File(context.filesDir, "data.json")
                    localFile.writeText(payload)

                    // Attempt saving to Downloads folder if accessible
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (downloadsDir != null && downloadsDir.exists()) {
                        File(downloadsDir, "a23_market_data.json").writeText(payload)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Parse and insert records into database
            var insertedCount = 0
            withContext(Dispatchers.IO) {
                try {
                    val entriesToInsert = mutableListOf<MarketEntry>()
                    val trimmed = payload.trim()

                    if (trimmed.startsWith("[")) {
                        val array = JSONArray(trimmed)
                        for (i in 0 until array.length()) {
                            val obj = array.optJSONObject(i)
                            if (obj != null) {
                                val mName = obj.optString("marketName", obj.optString("market", "SHRIDEVI"))
                                val dt = obj.optString("date", "")
                                val res = obj.optString("result", "")
                                val isH = obj.optBoolean("isHoliday", false)
                                if (dt.isNotBlank() && res.isNotBlank()) {
                                    entriesToInsert.add(
                                        MarketEntry(
                                            marketName = mName.uppercase(),
                                            date = dt,
                                            result = res.replace(" ", ""),
                                            isHoliday = isH,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                }
                            }
                        }
                    } else if (trimmed.startsWith("{")) {
                        val obj = JSONObject(trimmed)
                        if (obj.has("records") && obj.optJSONArray("records") != null) {
                            val recArray = obj.getJSONArray("records")
                            for (i in 0 until recArray.length()) {
                                val item = recArray.optJSONObject(i)
                                if (item != null) {
                                    val mName = item.optString("marketName", item.optString("market", "SHRIDEVI"))
                                    val dt = item.optString("date", "")
                                    val res = item.optString("result", "")
                                    val isH = item.optBoolean("isHoliday", false) || res.contains("***") || res.contains("**")
                                    if (dt.isNotBlank() && res.isNotBlank()) {
                                        entriesToInsert.add(
                                            MarketEntry(
                                                marketName = mName.uppercase(),
                                                date = dt,
                                                result = res.replace(" ", ""),
                                                isHoliday = isH,
                                                timestamp = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            val keys = obj.keys()
                            while (keys.hasNext()) {
                                val marketKey = keys.next()
                                if (marketKey.equals("version", ignoreCase = true) ||
                                    marketKey.equals("last_updated", ignoreCase = true) ||
                                    marketKey.equals("markets", ignoreCase = true)
                                ) continue

                                val marketNameUpper = marketKey.uppercase()
                                val valueObj = obj.opt(marketKey)

                                if (valueObj is String) {
                                    secureStorage.saveSecureData("a23_market_${marketKey.lowercase()}", valueObj)
                                    valueObj.lines().forEach { line ->
                                        val entry = FormulaEngine.parseMarketLine(line, defaultMarket = marketNameUpper)
                                        if (entry != null) {
                                            entriesToInsert.add(entry)
                                        }
                                    }
                                } else if (valueObj is JSONArray) {
                                    for (i in 0 until valueObj.length()) {
                                        val item = valueObj.optJSONObject(i)
                                        if (item != null) {
                                            val mName = item.optString("marketName", item.optString("market", marketNameUpper))
                                            val dt = item.optString("date", "")
                                            val res = item.optString("result", "")
                                            val isH = item.optBoolean("isHoliday", false) || res.contains("***") || res.contains("**")
                                            if (dt.isNotBlank() && res.isNotBlank()) {
                                                entriesToInsert.add(
                                                    MarketEntry(
                                                        marketName = mName.uppercase(),
                                                        date = dt,
                                                        result = res.replace(" ", ""),
                                                        isHoliday = isH,
                                                        timestamp = System.currentTimeMillis()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Plain text multiline
                        trimmed.lines().forEach { line ->
                            val entry = FormulaEngine.parseMarketLine(line, defaultMarket = "SHRIDEVI")
                            if (entry != null) {
                                entriesToInsert.add(entry)
                            }
                        }
                    }

                    if (entriesToInsert.isNotEmpty() && repository != null) {
                        // Clear old entries for distinct updated markets before inserting fresh entries
                        val distinctMarkets = entriesToInsert.map { it.marketName }.distinct()
                        for (mName in distinctMarkets) {
                            repository.clearMarketEntries(mName)
                        }
                        entriesToInsert.forEach { repository.insertEntry(it) }
                        insertedCount = entriesToInsert.size
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val msg = if (insertedCount > 0) {
                "Data successfully synced & saved to phone storage! ($insertedCount records updated from GitHub)"
            } else {
                "Data synced & saved to phone storage!"
            }

            withContext(Dispatchers.Main) {
                onComplete(true, msg)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onComplete(false, "Sync failed: ${e.localizedMessage}")
            }
        }
    }
}
