package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.data.A23New1Prediction
import com.example.data.A23NewPrediction
import com.example.data.DayReport
import com.example.data.FormulaEngine
import com.example.data.MarketEntry

@Composable
fun SevenDayReportScreen(
    selectedMarket: String,
    availableMarkets: List<String> = listOf("KALYAN", "SHRIDEVI", "MILAN DAY", "TIME BAZAR", "KALYAN NIGHT", "MAIN BAZAR"),
    allEntries: List<MarketEntry> = emptyList(),
    onSelectMarket: (String) -> Unit = {},
    sevenDayReports: List<DayReport>,
    a23NewPrediction: A23NewPrediction = FormulaEngine.calculateA23NewPrediction(selectedMarket, emptyList(), "Sachin Solunke"),
    a23New1Prediction: A23New1Prediction = FormulaEngine.calculateA23New1Prediction(selectedMarket, emptyList())
) {
    A23Screen(
        selectedMarket = selectedMarket,
        availableMarkets = availableMarkets,
        allEntries = allEntries,
        onSelectMarket = onSelectMarket,
        a23NewPrediction = a23NewPrediction,
        a23New1Prediction = a23New1Prediction,
        sevenDayReports = sevenDayReports
    )
}
