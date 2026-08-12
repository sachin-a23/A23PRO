package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_entries WHERE UPPER(marketName) = UPPER(:marketName) ORDER BY id DESC")
    fun getEntriesForMarket(marketName: String): Flow<List<MarketEntry>>

    @Query("SELECT * FROM market_entries ORDER BY id DESC")
    fun getAllEntries(): Flow<List<MarketEntry>>

    @Query("SELECT COUNT(*) FROM market_entries")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MarketEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEntries(entries: List<MarketEntry>)

    @Query("DELETE FROM market_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM market_entries WHERE UPPER(marketName) = UPPER(:marketName)")
    suspend fun clearMarketEntries(marketName: String)

    @Query("DELETE FROM market_entries")
    suspend fun clearAllEntries()

    @Query("DELETE FROM market_entries WHERE UPPER(marketName) = UPPER(:marketName) AND date = :date")
    suspend fun deleteEntryByMarketAndDate(marketName: String, date: String)

    @Query("SELECT * FROM market_entries WHERE UPPER(marketName) = UPPER(:marketName) AND date = :date LIMIT 1")
    suspend fun getEntryByMarketAndDate(marketName: String, date: String): MarketEntry?

    @Query("SELECT * FROM market_configs")
    fun getAllMarketConfigs(): Flow<List<MarketConfig>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketConfig(config: MarketConfig): Long

    @Query("DELETE FROM market_configs WHERE id = :id")
    suspend fun deleteMarketConfig(id: Long)
}
