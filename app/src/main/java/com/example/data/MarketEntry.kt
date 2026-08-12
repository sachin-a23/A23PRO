package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_entries")
data class MarketEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val marketName: String,
    val date: String,
    val result: String,
    val isHoliday: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
