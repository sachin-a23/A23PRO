package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MarketRepository(private val dao: MarketDao, private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val marketMapUrls = mapOf(
        "SHRIDEVI" to "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
        "KALYAN" to "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
        "MILAN" to "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
        "TIME BAZAR" to "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json",
        "ALL" to "https://raw.githubusercontent.com/sachin-a23/A23site/main/data.json"
    )

    fun getEntriesForMarket(marketName: String): Flow<List<MarketEntry>> {
        return dao.getEntriesForMarket(marketName)
    }

    fun getAllEntries(): Flow<List<MarketEntry>> {
        return dao.getAllEntries()
    }

    suspend fun getEntryCount(): Int = withContext(Dispatchers.IO) {
        dao.getCount()
    }

    suspend fun insertEntry(entry: MarketEntry): Long = withContext(Dispatchers.IO) {
        val existing = dao.getEntryByMarketAndDate(entry.marketName, entry.date)
        val finalEntry = if (entry.id == 0L && existing != null) {
            entry.copy(id = existing.id)
        } else {
            entry
        }
        dao.insertEntry(finalEntry)
    }

    suspend fun clearAllEntries() = withContext(Dispatchers.IO) {
        dao.clearAllEntries()
    }

    suspend fun clearMarketEntries(marketName: String) = withContext(Dispatchers.IO) {
        dao.clearMarketEntries(marketName)
    }

    suspend fun deleteEntry(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteEntryById(id)
    }

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCount() > 0) return@withContext
        // Exact real tracker data from admin control panel (24-07-2026 to 07-08-2026)
        val realShridevi = listOf(
            MarketEntry(marketName = "SHRIDEVI", date = "24-07-2026", result = "256-38-558"),
            MarketEntry(marketName = "SHRIDEVI", date = "25-07-2026", result = "135-90-370"),
            MarketEntry(marketName = "SHRIDEVI", date = "26-07-2026", result = "689-33-157"),
            MarketEntry(marketName = "SHRIDEVI", date = "27-07-2026", result = "469-99-667"),
            MarketEntry(marketName = "SHRIDEVI", date = "28-07-2026", result = "140-51-489"),
            MarketEntry(marketName = "SHRIDEVI", date = "29-07-2026", result = "345-27-359"),
            MarketEntry(marketName = "SHRIDEVI", date = "30-07-2026", result = "126-90-488"),
            MarketEntry(marketName = "SHRIDEVI", date = "31-07-2026", result = "157-39-234"),
            MarketEntry(marketName = "SHRIDEVI", date = "01-08-2026", result = "245-16-367"),
            MarketEntry(marketName = "SHRIDEVI", date = "02-08-2026", result = "389-04-130"),
            MarketEntry(marketName = "SHRIDEVI", date = "03-08-2026", result = "557-76-349"),
            MarketEntry(marketName = "SHRIDEVI", date = "04-08-2026", result = "138-29-667"),
            MarketEntry(marketName = "SHRIDEVI", date = "05-08-2026", result = "278-79-478"),
            MarketEntry(marketName = "SHRIDEVI", date = "06-08-2026", result = "157-30-118"),
            MarketEntry(marketName = "SHRIDEVI", date = "07-08-2026", result = "457-66-178")
        )

        val realKalyan = listOf(
            MarketEntry(marketName = "KALYAN", date = "01-08-2026", result = "128-15-230"),
            MarketEntry(marketName = "KALYAN", date = "02-08-2026", result = "345-29-180"),
            MarketEntry(marketName = "KALYAN", date = "03-08-2026", result = "670-36-240"),
            MarketEntry(marketName = "KALYAN", date = "04-08-2026", result = "140-58-350"),
            MarketEntry(marketName = "KALYAN", date = "05-08-2026", result = "289-94-158"),
            MarketEntry(marketName = "KALYAN", date = "06-08-2026", result = "156-27-368"),
            MarketEntry(marketName = "KALYAN", date = "07-08-2026", result = "489-10-235")
        )

        val realMilan = listOf(
            MarketEntry(marketName = "MILAN", date = "01-08-2026", result = "136-04-248"),
            MarketEntry(marketName = "MILAN", date = "02-08-2026", result = "230-51-146"),
            MarketEntry(marketName = "MILAN", date = "03-08-2026", result = "189-82-345"),
            MarketEntry(marketName = "MILAN", date = "04-08-2026", result = "267-53-148"),
            MarketEntry(marketName = "MILAN", date = "05-08-2026", result = "129-27-368"),
            MarketEntry(marketName = "MILAN", date = "06-08-2026", result = "457-69-126"),
            MarketEntry(marketName = "MILAN", date = "07-08-2026", result = "238-34-158")
        )

        val realTimeBazar = listOf(
            MarketEntry(marketName = "TIME BAZAR", date = "01-08-2026", result = "147-28-230"),
            MarketEntry(marketName = "TIME BAZAR", date = "02-08-2026", result = "258-50-145"),
            MarketEntry(marketName = "TIME BAZAR", date = "03-08-2026", result = "139-31-245"),
            MarketEntry(marketName = "TIME BAZAR", date = "04-08-2026", result = "168-52-345"),
            MarketEntry(marketName = "TIME BAZAR", date = "05-08-2026", result = "389-06-123"),
            MarketEntry(marketName = "TIME BAZAR", date = "06-08-2026", result = "140-59-234"),
            MarketEntry(marketName = "TIME BAZAR", date = "07-08-2026", result = "267-55-120")
        )

        dao.insertAllEntries(realShridevi)
        dao.insertAllEntries(realKalyan)
        dao.insertAllEntries(realMilan)
        dao.insertAllEntries(realTimeBazar)
    }

    suspend fun syncDataFromUrl(marketName: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var syncedCount = 0
            var syncErr: String? = null
            val syncEngine = MarketDataSync(context, this@MarketRepository)
            syncEngine.fetchAndSyncData { success, msg ->
                if (!success) {
                    syncErr = msg
                }
            }
            val totalInDb = dao.getCount()
            if (syncErr != null && totalInDb == 0) {
                Result.failure(Exception(syncErr))
            } else {
                Result.success(totalInDb)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
