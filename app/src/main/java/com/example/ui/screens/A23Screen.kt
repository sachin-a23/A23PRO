package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.A23New1Prediction
import com.example.data.A23NewPrediction
import com.example.data.A23NewVariant
import com.example.data.DayReport
import com.example.data.FormulaEngine
import com.example.data.MarketEntry
import com.example.ui.theme.*
import com.example.util.PdfReportExporter
import com.example.utils.copyTextToClipboard

enum class A23OptionTab {
    NEW, NEW_1
}

@Composable
fun A23Screen(
    selectedMarket: String,
    availableMarkets: List<String> = listOf("KALYAN", "SHRIDEVI", "MILAN DAY", "TIME BAZAR", "KALYAN NIGHT", "MAIN BAZAR"),
    allEntries: List<MarketEntry> = emptyList(),
    onSelectMarket: (String) -> Unit = {},
    a23NewPrediction: A23NewPrediction,
    a23New1Prediction: A23New1Prediction,
    sevenDayReports: List<DayReport>
) {
    val context = LocalContext.current
    var selectedOptionTab by remember { mutableStateOf(A23OptionTab.NEW) }

    val prefs = remember(context) { context.getSharedPreferences("A23SavedFormulas", Context.MODE_PRIVATE) }
    
    var savedDivisor by remember(selectedMarket) {
        mutableStateOf(prefs.getInt("saved_divisor_$selectedMarket", 3))
    }
    var isFormulaSavedForMarket by remember(selectedMarket) {
        mutableStateOf(prefs.contains("saved_divisor_$selectedMarket"))
    }

    var selectedDivisor by remember(selectedMarket) {
        mutableStateOf(savedDivisor)
    }

    val availableDivisors = listOf(2, 3, 4, 5, 6, 7, 8, 9)

    // Calculate dynamic variant for the selected divisor
    val activeVariant = remember(selectedMarket, selectedDivisor, a23NewPrediction) {
        val lastJodi = a23NewPrediction.lastJodi
        val multVal = 30 * lastJodi
        val quot = if (selectedDivisor > 0) multVal.toLong() / selectedDivisor else multVal.toLong()
        val quotStr = quot.toString()

        val otcList = quotStr.mapNotNull { it.digitToIntOrNull() }.distinct().take(4).ifEmpty { listOf(1, 5, 2, 0) }
        val otc1 = otcList.getOrElse(0) { 1 }
        val otc2 = otcList.getOrElse(1) { 5 }
        val otc3 = otcList.getOrElse(2) { 2 }
        val otc4 = otcList.getOrElse(3) { 0 }

        val superJodis = listOf("${otc1}${otc2}", "${otc2}${otc1}", "${otc1}${otc3}", "${otc2}${otc4}").distinct()
        val openP = FormulaEngine.generatePannaForAnk(otc1, 1)
        val closeP = FormulaEngine.generatePannaForAnk(otc2, 3)

        A23NewVariant(
            title = "Option (÷$selectedDivisor)",
            divisor = selectedDivisor,
            formulaText = "(30 × $lastJodi = $multVal) ÷ $selectedDivisor = $quot",
            otcDigits = otcList,
            superJodi = superJodis,
            openPanna = openP,
            openAnk = otc1,
            closeAnk = otc2,
            closePanna = closeP,
            pannelJodiFormat = "$openP - ${superJodis.firstOrNull() ?: "${otc1}${otc2}"} - $closeP",
            quotient = quot
        )
    }

    // Historical Backtest calculation
    val backtestReport = remember(selectedMarket, selectedDivisor, allEntries) {
        evaluateFormulaAllDaysBacktest(
            marketName = selectedMarket,
            primaryRule = "(30 × Last Jodi) ÷ $selectedDivisor",
            constantVal = selectedDivisor,
            divFactor = selectedDivisor,
            isCutEnabled = true,
            allEntries = allEntries
        )
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Official App Banner Logo Card
        item {
            Image(
                painter = painterResource(id = R.drawable.img_a23_app_logo_1786420070249),
                contentDescription = "A23 Pro Official Banner Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.2.dp, CyberGoldPrimary.copy(alpha = 0.8f)), RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }

        // Top Header Title & Tab Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "👑 A23 FORMULA ENGINE",
                        color = CyberGoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "मार्केट: $selectedMarket",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Tab Selector Pill ( New | New-1 )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.70f))
                        .border(BorderStroke(1.dp, CyberGoldOutline), RoundedCornerShape(20.dp))
                        .padding(3.dp)
                ) {
                    Surface(
                        color = if (selectedOptionTab == A23OptionTab.NEW) CyberGoldPrimary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { selectedOptionTab = A23OptionTab.NEW }
                            .testTag("a23_tab_new")
                    ) {
                        Text(
                            text = "New",
                            color = if (selectedOptionTab == A23OptionTab.NEW) Color.Black else CyberTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        color = if (selectedOptionTab == A23OptionTab.NEW_1) CyberGoldPrimary else Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clickable { selectedOptionTab = A23OptionTab.NEW_1 }
                            .testTag("a23_tab_new_1")
                    ) {
                        Text(
                            text = "New-1",
                            color = if (selectedOptionTab == A23OptionTab.NEW_1) Color.Black else CyberTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Fast Market Selection Chips Row (फास्ट मार्केट चुनाव)
        item {
            Column {
                Text(
                    text = "⚡ फास्ट मार्केट चुनाव (Fast Market Selector):",
                    color = CyberGoldOutline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                val marketList = if (availableMarkets.isNotEmpty()) availableMarkets else listOf("KALYAN", "SHRIDEVI", "MILAN DAY", "TIME BAZAR", "KALYAN NIGHT", "MAIN BAZAR")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(marketList) { mkt ->
                        val isSelected = mkt.equals(selectedMarket, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) CyberGoldPrimary else Color(0xFF131D2E),
                            border = BorderStroke(
                                1.2.dp,
                                if (isSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable { onSelectMarket(mkt) }
                        ) {
                            Text(
                                text = mkt.uppercase(),
                                color = if (isSelected) Color.Black else CyberTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Formula Engine Active Live Status Banner
        item {
            Surface(
                color = Color(0xFF0D2818).copy(alpha = 0.75f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyberNeonGreen,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚡ LIVE FORMULA ENGINE ACTIVE",
                            color = CyberNeonGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Text(
                        text = "लास्ट एंट्री: ${a23NewPrediction.lastEntryResult}",
                        color = CyberGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (selectedOptionTab == A23OptionTab.NEW) {
            // Formula Selector & Customizer Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.2.dp, CyberGoldPrimary.copy(alpha = 0.8f)), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1B2B).copy(alpha = 0.82f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧮 फॉर्मूला चुनाव व कस्टमाइजेशन",
                                color = CyberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )

                            if (isFormulaSavedForMarket && savedDivisor == selectedDivisor) {
                                Surface(
                                    color = Color(0xFF162E21),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CyberNeonGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Saved", tint = CyberNeonGreen, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("सेव्ड फॉर्मूला सक्रीय", color = CyberNeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "फॉर्मूला सूत्र: (30 × Last Jodi [${a23NewPrediction.lastJodi}]) ÷ Divisor - 9",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "भाजक (Divisor) चुनें:",
                            color = CyberTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Divisor Selector Chips ( ÷2, ÷3, ÷4, ÷5, ÷6, ÷7, ÷8, ÷9 )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableDivisors) { div ->
                                val isSelected = (selectedDivisor == div)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CyberNeonGreen else Color(0xFF1B283B),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CyberNeonGreen else CyberGoldOutline.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier.clickable { selectedDivisor = div }
                                ) {
                                    Text(
                                        text = "÷ $div",
                                        color = if (isSelected) Color.Black else CyberTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save Formula Button
                        Button(
                            onClick = {
                                prefs.edit().putInt("saved_divisor_$selectedMarket", selectedDivisor).apply()
                                savedDivisor = selectedDivisor
                                isFormulaSavedForMarket = true
                                Toast.makeText(
                                    context,
                                    "✓ '$selectedMarket' के लिए फॉर्मूला (÷$selectedDivisor) सेव हो गया! अब आने वाले दिनों में यही लागू रहेगा।",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Save Formula", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💾 '$selectedMarket' के लिए यह फॉर्मूला सेव करें",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Prediction Output Card for Active Selected Formula
            item {
                A23NewVariantCard(
                    marketName = selectedMarket,
                    targetDate = a23NewPrediction.targetDate,
                    lastDate = a23NewPrediction.lastEntryDate,
                    lastResult = a23NewPrediction.lastEntryResult,
                    userName = a23NewPrediction.userName,
                    variant = activeVariant,
                    optionLabel = "Option (÷$selectedDivisor)"
                )
            }

            // Historical Backtest Summary Card (Pass / Fail Days Accuracy)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.8f)), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1826).copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "📊 $selectedMarket पास / फेल रिकॉर्ड समरी",
                                    color = CyberNeonGreen,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "कुल टेस्टेड: ${backtestReport.totalTestedDays} दिन | पास: ${backtestReport.passedDaysCount} ✓ | फेल: ${backtestReport.failedDaysCount} ❌",
                                    color = CyberTextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            Text(
                                text = backtestReport.passRatePercentage,
                                color = CyberGoldPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // Historical Pass / Fail Table (नीचे पिछले दिनों का रिकॉर्ड)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1420))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "📜 पिछले दिनों का बैकटेस्ट रिकॉर्ड ($selectedMarket):",
                            color = CyberGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Date • Prediction (OTC / Jodi / Panel) • Result • Pass/Fail Mark",
                            color = CyberTextSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Scrollable History Table
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF162032), RoundedCornerShape(6.dp))
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("दिनांक / वार", color = CyberGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                    Text("पूर्वानुमान (Pred)", color = CyberGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                                    Text("रिजल्ट", color = CyberGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))
                                    Text("स्टेटस", color = CyberGoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                backtestReport.records.forEach { rec ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text(rec.date, color = CyberTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Text(rec.dayOfWeek, color = CyberTextSecondary, fontSize = 8.sp)
                                        }

                                        Column(modifier = Modifier.weight(1.3f)) {
                                            Text("OTC: ${rec.predictedOtc.joinToString(",")}", color = CyberNeonGreen, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                            Text("Jodi: ${rec.predictedJodi.take(2).joinToString(",")}", color = CyberGoldPrimary, fontSize = 8.5.sp)
                                            Text("Pannel: ${rec.predictedOpenPanna}-${rec.predictedClosePanna}", color = Color(0xFF00E5FF), fontSize = 8.sp)
                                        }

                                        Text(rec.actualResult, color = CyberTextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.1f))

                                        if (rec.isOverallPass) {
                                            Text("PASS ✓", color = CyberNeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.9f))
                                        } else {
                                            Text("FAIL ❌", color = CyberFailRed, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(0.9f))
                                        }
                                    }
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PDF Report Download Button
                        Button(
                            onClick = {
                                PdfReportExporter.exportAllDaysFormulaBacktestToPdf(
                                    context = context,
                                    marketName = backtestReport.marketName,
                                    formulaName = backtestReport.formulaName,
                                    ruleDetails = backtestReport.ruleDetails,
                                    totalDays = backtestReport.totalTestedDays,
                                    passCount = backtestReport.passedDaysCount,
                                    failCount = backtestReport.failedDaysCount,
                                    passRate = backtestReport.passRatePercentage,
                                    bestDay = backtestReport.bestDay,
                                    records = backtestReport.records
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📄 ${selectedMarket} ऑल डेज PDF रिपोर्ट डाउनलोड करें",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

        } else {
            // New-1 Option Tab Content
            item {
                A23New1Card(
                    prediction = a23New1Prediction,
                    selectedMarket = selectedMarket
                )
            }
        }
    }
}

@Composable
fun A23NewVariantCard(
    marketName: String,
    targetDate: String,
    lastDate: String,
    lastResult: String,
    userName: String,
    variant: A23NewVariant,
    optionLabel: String
) {
    val context = LocalContext.current
    val formattedText = buildString {
        append("👑 A23 PRO - ${marketName.uppercase()} PREDICTION\n")
        append("📅 Target Date: $targetDate\n")
        append("🎯 OTC Digits: ${variant.otcDigits.joinToString(", ")}\n")
        append("🔥 Super Jodi: ${variant.superJodi.joinToString(", ")}\n")
        append("🎴 Panel Prediction: ${variant.pannelJodiFormat}\n")
        append("📌 Last Entry: $lastDate: $lastResult\n")
        append("📊 Formula: ${variant.formulaText}\n")
        append("👤 Name: $userName")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.2.dp, CyberGoldPrimary.copy(alpha = 0.7f)), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101726).copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👑 A23 PRO - ${marketName.uppercase()} PREDICTION",
                    color = CyberGoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, CyberGoldOutline)
                ) {
                    Text(
                        text = optionLabel,
                        color = CyberGoldVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "📅 Target Date: $targetDate", color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎯 OTC Digits: ${variant.otcDigits.joinToString(", ")}",
                color = Color(0xFF00E5FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🔥 Super Jodi: ${variant.superJodi.joinToString(", ")}",
                color = CyberNeonGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎴 Panel Prediction: ${variant.pannelJodiFormat}",
                color = CyberGoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📌 Last Entry: $lastDate: $lastResult", color = CyberTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📊 Formula: ${variant.formulaText}", color = CyberGoldVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "👤 Name: $userName", color = CyberTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Copy & WhatsApp Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        copyTextToClipboard(context, formattedText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberGoldPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberGoldPrimary)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Copy Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Prediction via"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "WhatsApp Share", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun A23New1Card(
    prediction: A23New1Prediction,
    selectedMarket: String
) {
    val context = LocalContext.current
    val formattedText = buildString {
        append("🎯 NEW-1 OTC PREDICTION (${selectedMarket.uppercase()})\n")
        append("Target Date: ${prediction.targetDate}\n")
        append("Formula: Jodi(i-1) & Jodi(i-4) [ADD_MUL] × 9\n")
        append("Jodi Inputs: [J1: ${prediction.j1Value} (${prediction.j1Date})] × [J2: ${prediction.j2Value} (${prediction.j2Date})]\n")
        append("Step 1: ${prediction.step1Text}\n")
        append("Step 2: ${prediction.step2Text}\n")
        append("-----------------------------\n")
        append("🔥 OTC DIGITS: ${prediction.otcDigits.joinToString(", ")}\n")
        append("💎 SUPER JODIS: ${prediction.superJodis.joinToString(", ")}\n")
        append("🎴 PANEL FORMAT: ${prediction.pannelJodiFormat}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(BorderStroke(1.2.dp, Color(0xFF00E5FF)), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1626).copy(alpha = 0.85f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🎯 NEW-1 OTC PREDICTION (${selectedMarket.uppercase()})",
                color = Color(0xFF00E5FF),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = "📅 Target Date: ${prediction.targetDate}", color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "📐 Formula: Jodi(i-1) & Jodi(i-4) [ADD_MUL] × 9", color = CyberGoldVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📌 Jodi Inputs: [J1: ${prediction.j1Value} (${prediction.j1Date})] × [J2: ${prediction.j2Value} (${prediction.j2Date})]",
                color = CyberTextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFF070B12),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Step 1: ${prediction.step1Text}", color = CyberGoldPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${prediction.step2Text}", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = CyberGoldOutline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "🔥 OTC DIGITS: ${prediction.otcDigits.joinToString(", ")}",
                color = CyberNeonGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "💎 SUPER JODIS: ${prediction.superJodis.joinToString(", ")}",
                color = CyberGoldPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎴 PANEL FORMAT: ${prediction.pannelJodiFormat}",
                color = Color(0xFF00E5FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { copyTextToClipboard(context, formattedText) },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF))
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Copy NEW-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share NEW-1 Prediction via"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "WhatsApp Share", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
