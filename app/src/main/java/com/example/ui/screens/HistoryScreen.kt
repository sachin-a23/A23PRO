package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormulaEngine
import com.example.data.FormulaType
import com.example.data.HistoryRecordItem
import com.example.data.MarketEntry
import com.example.ui.theme.*
import com.example.util.PdfReportExporter

@Composable
fun HistoryScreen(
    selectedMarket: String,
    allEntries: List<MarketEntry>
) {
    val context = LocalContext.current
    var activeMarket by remember(selectedMarket) { mutableStateOf(selectedMarket) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PASS", "FAIL", "PENDING"
    var selectedFormula by remember { mutableStateOf(FormulaType.STANDARD) }

    // Distinct list of available markets for top market selection chips
    val availableMarkets = remember(allEntries) {
        val names = allEntries.map { it.marketName.trim().uppercase() }.distinct().filter { it.isNotBlank() }
        val defaultList = listOf("SHRIDEVI", "KALYAN", "MILAN DAY", "TIME BAZAR", "KALYAN NIGHT", "MAIN BAZAR", "RAJDHANI NIGHT", "MADHUR DAY")
        (names + defaultList).distinct()
    }

    // Process and evaluate history entries using strict FormulaEngine Pass/Fail validation for selected formula
    val evaluatedRecords = remember(activeMarket, allEntries, selectedFormula) {
        val marketEntries = allEntries.filter { entry ->
            entry.marketName.equals(activeMarket, ignoreCase = true)
        }
        FormulaEngine.evaluateHistoryRecords(marketEntries, selectedFormula)
    }

    // Filter records based on search query and status filter
    val filteredRecords = remember(evaluatedRecords, searchQuery, selectedFilter) {
        evaluatedRecords.filter { record ->
            val isPending = record.result.contains("***") ||
                    record.result.contains("000-00-000") ||
                    record.result.uppercase().contains("PENDING") ||
                    record.result.uppercase().contains("WAIT")

            val matchesSearch = searchQuery.isBlank() ||
                    record.date.contains(searchQuery, ignoreCase = true) ||
                    record.result.contains(searchQuery, ignoreCase = true) ||
                    record.dayOfWeekHindi.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (selectedFilter) {
                "PASS" -> record.isPass && !isPending
                "FAIL" -> !record.isPass && !isPending
                "PENDING" -> isPending
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    // Calculate Weekly Analytics (Somvaar to Ravivaar) & Percentage (%)
    val totalRecordsCount = evaluatedRecords.size
    val passCount = evaluatedRecords.count { it.isPass }
    val failCount = totalRecordsCount - passCount
    val passPercentage = if (totalRecordsCount > 0) (passCount.toFloat() / totalRecordsCount * 100f) else 0f

    // Days of Week map for Somvaar to Ravivaar analysis
    val daysOrder = listOf(
        Pair("Monday", "Som (Mon)"),
        Pair("Tuesday", "Mangal (Tue)"),
        Pair("Wednesday", "Budh (Wed)"),
        Pair("Thursday", "Guru (Thu)"),
        Pair("Friday", "Shukra (Fri)"),
        Pair("Saturday", "Shani (Sat)"),
        Pair("Sunday", "Ravi (Sun)")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📊 HISTORICAL ANALYTICS & LOGS",
                        color = CyberGoldPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "$activeMarket • Strict Pattern Match Engine",
                        color = CyberTextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            PdfReportExporter.exportGeneralHistoryToPdf(
                                context = context,
                                marketName = activeMarket,
                                formulaName = selectedFormula.displayName,
                                records = filteredRecords
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberNeonGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "PDF डाउनलोड", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        color = CyberGoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberGoldPrimary)
                    ) {
                        Text(
                            text = "$totalRecordsCount RECS",
                            color = CyberGoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ==================== MARKET SELECTION CHIPS ====================
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                Text(
                    text = "🌐 MARKET CHUNAV (SELECT MARKET):",
                    color = CyberGoldOutline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableMarkets) { mkt ->
                        val isSelected = activeMarket.equals(mkt, ignoreCase = true)
                        Surface(
                            color = if (isSelected) CyberGoldPrimary else Color(0xFF0F172A),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (isSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .clickable { activeMarket = mkt }
                                .testTag("history_market_chip_$mkt")
                        ) {
                            Text(
                                text = mkt,
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

        // Formula Selection Tabs
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                Text(
                    text = "⚡ SELECT FORMULA ENGINE FOR HISTORY:",
                    color = CyberGoldOutline,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(FormulaType.entries.toTypedArray()) { formula ->
                        val isSelected = selectedFormula == formula
                        Surface(
                            color = if (isSelected) CyberGoldPrimary else Color(0xFF0F172A),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (isSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clickable { selectedFormula = formula }
                                .testTag("formula_tab_${formula.name}")
                        ) {
                            Text(
                                text = formula.displayName,
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

        // ==================== TODAY'S LIVE PREDICTION CARD ON TOP ====================
        item {
            val marketEntries = remember(activeMarket, allEntries) {
                allEntries.filter { it.marketName.equals(activeMarket, ignoreCase = true) }
            }
            val pred = remember(activeMarket, marketEntries, selectedFormula) {
                FormulaEngine.calculatePrediction(activeMarket, marketEntries, selectedFormula)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.5.dp, Color(0xFF00E5FF)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1626).copy(alpha = 0.90f))
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
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Prediction",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎯 TODAY'S LIVE PREDICTION ($activeMarket)",
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = Color(0xFFFFB703).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB703))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = "Pending",
                                    tint = Color(0xFFFFB703),
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RESULT PENDING",
                                    color = Color(0xFFFFB703),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // OTC Digits
                        Surface(
                            color = Color(0xFF142032),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OTC DIGITS", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pred.mainOtc.joinToString(", "),
                                    color = CyberGoldPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Super Jodi
                        Surface(
                            color = Color(0xFF142032),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("SUPER JODI", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = pred.superJodi.take(3).joinToString(", "),
                                    color = CyberGoldPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Open-Close Panna
                        Surface(
                            color = Color(0xFF142032),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OPEN-CLOSE PANNA", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pred.openPanna} - ${pred.openAnk}${pred.closeAnk} - ${pred.closePanna}",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==================== 1. WEEKLY ANALYSIS (SOMVAAR TO RAVIVAAR) & ACCURACY CARD ====================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.5.dp, CyberGoldPrimary), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1422).copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Title & Percentage Gauge Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Analytics",
                                tint = CyberGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WEEKLY ACCURACY",
                                color = CyberGoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Pass Percentage Badge
                        Surface(
                            color = if (passPercentage >= 70f) CyberNeonGreen.copy(alpha = 0.2f) else Color(0xFFFF3366).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.2.dp, if (passPercentage >= 70f) CyberNeonGreen else Color(0xFFFF3366))
                        ) {
                            Text(
                                text = "ACCURACY: ${String.format("%.1f", passPercentage)}%",
                                color = if (passPercentage >= 70f) CyberNeonGreen else Color(0xFFFF3366),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Success / Failure Stat Summary Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = CyberNeonGreen.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, CyberNeonGreen.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("PASS DAYS", color = CyberNeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$passCount Days", color = CyberNeonGreen, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Surface(
                            color = Color(0xFFFF3366).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFFF3366).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("FAIL DAYS", color = Color(0xFFFF3366), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$failCount Days", color = Color(0xFFFF3366), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Surface(
                            color = Color(0xFF162032),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("TOTAL EVAL", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text("$totalRecordsCount Days", color = CyberTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Linear Progress Bar for Pass Rate
                    LinearProgressIndicator(
                        progress = { if (totalRecordsCount > 0) passCount.toFloat() / totalRecordsCount else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyberNeonGreen,
                        trackColor = Color(0xFFFF3366).copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Somvaar to Ravivaar Horizontal Day Chips
                    Text(
                        text = "Weekly Breakdown (Somvaar → Ravivaar):",
                        color = CyberTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(daysOrder) { (enumName, hindiLabel) ->
                            val dayRecords = evaluatedRecords.filter { it.dayOfWeekEnumName.equals(enumName, ignoreCase = true) }
                            val dayPasses = dayRecords.count { it.isPass }
                            val dayTotal = dayRecords.size
                            val isDayPass = dayTotal > 0 && dayPasses > 0

                            Surface(
                                color = when {
                                    dayTotal == 0 -> Color(0xFF141C2A)
                                    isDayPass -> CyberNeonGreen.copy(alpha = 0.15f)
                                    else -> Color(0xFFFF3366).copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        dayTotal == 0 -> CyberGoldOutline.copy(alpha = 0.3f)
                                        isDayPass -> CyberNeonGreen
                                        else -> Color(0xFFFF3366)
                                    }
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = hindiLabel,
                                        color = CyberTextPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when {
                                            dayTotal == 0 -> "NO DATA"
                                            isDayPass -> "PASS ($dayPasses/$dayTotal)"
                                            else -> "FAIL (0/$dayTotal)"
                                        },
                                        color = when {
                                            dayTotal == 0 -> CyberTextSecondary
                                            isDayPass -> CyberNeonGreen
                                            else -> Color(0xFFFF3366)
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== 2. SEARCH & FILTER BAR ====================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by date, result or day (e.g. Somvaar)...", color = CyberTextSecondary, fontSize = 11.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = CyberGoldPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGoldPrimary,
                        unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Filter Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Filter:", color = CyberTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    listOf("ALL", "PASS", "FAIL", "PENDING").forEach { filterOpt ->
                        val isSelected = selectedFilter == filterOpt
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filterOpt },
                            label = { Text(filterOpt, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when(filterOpt) {
                                    "PASS" -> CyberNeonGreen
                                    "FAIL" -> Color(0xFFFF3366)
                                    "PENDING" -> Color(0xFFFFB703)
                                    else -> CyberGoldPrimary
                                },
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF141C2A),
                                labelColor = CyberTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // ==================== 3. COMPACT HISTORY RECORD CARDS ====================
        if (filteredRecords.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history records match '$searchQuery' ($selectedFilter filter) for $activeMarket",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(filteredRecords) { item ->
                val isPending = item.result.contains("***") ||
                        item.result.contains("000-00-000") ||
                        item.result.uppercase().contains("PENDING") ||
                        item.result.uppercase().contains("WAIT")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            BorderStroke(
                                1.2.dp,
                                when {
                                    isPending -> Color(0xFFFFB703).copy(alpha = 0.7f)
                                    item.isPass -> CyberNeonGreen.copy(alpha = 0.6f)
                                    else -> Color(0xFFFF3366).copy(alpha = 0.6f)
                                }
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1624).copy(alpha = 0.60f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Card Top Row: Date, Day of Week, Pass/Fail/Pending Badge & Copy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Date",
                                    tint = CyberGoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${item.dayOfWeekHindi} • ${item.date}",
                                    color = CyberGoldPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Status Badge (Pending / Pass / Fail)
                                Surface(
                                    color = when {
                                        isPending -> Color(0xFFFFB703).copy(alpha = 0.18f)
                                        item.isPass -> CyberNeonGreen.copy(alpha = 0.18f)
                                        else -> Color(0xFFFF3366).copy(alpha = 0.18f)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, when {
                                        isPending -> Color(0xFFFFB703)
                                        item.isPass -> CyberNeonGreen
                                        else -> Color(0xFFFF3366)
                                    })
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                isPending -> Icons.Default.HourglassTop
                                                item.isPass -> Icons.Default.CheckCircle
                                                else -> Icons.Default.Cancel
                                            },
                                            contentDescription = "Status",
                                            tint = when {
                                                isPending -> Color(0xFFFFB703)
                                                item.isPass -> CyberNeonGreen
                                                else -> Color(0xFFFF3366)
                                            },
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when {
                                                isPending -> "PENDING ⏳"
                                                item.isPass -> "PASS"
                                                else -> "FAIL"
                                            },
                                            color = when {
                                                isPending -> Color(0xFFFFB703)
                                                item.isPass -> CyberNeonGreen
                                                else -> Color(0xFFFF3366)
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                // Quick Copy Button
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val statusStr = when {
                                            isPending -> "PENDING"
                                            item.isPass -> "PASS"
                                            else -> "FAIL"
                                        }
                                        val textToCopy = "${item.marketName} (${item.date} ${item.dayOfWeekHindi}) | OTC: ${item.otc.joinToString(", ")} | Jodi: ${item.jodi} | Result: ${if (isPending) "PENDING" else item.result} | Status: $statusStr"
                                        val clip = ClipData.newPlainText("Market History", textToCopy)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied record to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Record",
                                        tint = CyberGoldVariant,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compact Boxes Grid: OTC | Jodi | Result
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Box 1: OTC Digits
                            Surface(
                                color = Color(0xFF141D2D),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "OTC DIGITS", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.otc.joinToString(", "),
                                        color = CyberGoldPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Box 2: Jodi
                            Surface(
                                color = Color(0xFF141D2D),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "SUPER JODI", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.jodi,
                                        color = CyberGoldPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Box 3: Actual Result
                            Surface(
                                color = Color(0xFF182234),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(0.8.dp, when {
                                    isPending -> Color(0xFFFFB703).copy(alpha = 0.6f)
                                    item.isPass -> CyberNeonGreen.copy(alpha = 0.5f)
                                    else -> Color(0xFFFF3366).copy(alpha = 0.5f)
                                }),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "DECLARATION RESULT", color = CyberTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isPending) "PENDING ⏳" else item.result,
                                        color = if (isPending) Color(0xFFFFB703) else CyberTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
