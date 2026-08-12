package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "market_configs")
data class MarketConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val marketName: String,
    val dataUrl: String = ""
)
