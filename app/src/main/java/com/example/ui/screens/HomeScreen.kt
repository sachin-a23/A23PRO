package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import com.example.data.*
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    selectedMarket: String,
    availableMarkets: List<String> = listOf("KALYAN", "SHRIDEVI", "MILAN", "TIME BAZAR"),
    allEntries: List<MarketEntry> = emptyList(),
    prediction: PredictionResult,
    sevenDayReport: List<DayReport>,
    isSyncing: Boolean,
    userName: String = "Sachin Solunke",
    onSelectMarket: (String) -> Unit = {},
    onDownloadClick: () -> Unit,
    onViewFullReportClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedFormula by remember { mutableStateOf(FormulaType.STANDARD) }

    // Helper data structure for market prediction summary card
    data class MarketFormulaSummary(
        val marketName: String,
        val otcDigits: List<Int>,
        val superJodi: List<String>,
        val openPanna: String,
        val openAnk: Int,
        val closeAnk: Int,
        val closePanna: String,
        val pannelJodiFormat: String,
        val subText: String,
        val formulaTag: String
    )

    // Compute predictions for all markets based on currently selected formula
    val allMarketSummaries = availableMarkets.map { mkt ->
        val mktEntries = allEntries.filter { it.marketName.equals(mkt, ignoreCase = true) }
        when (selectedFormula) {
            FormulaType.STANDARD -> {
                val pred = FormulaEngine.calculatePrediction(mkt, mktEntries)
                MarketFormulaSummary(
                    marketName = mkt,
                    otcDigits = pred.mainOtc,
                    superJodi = pred.superJodi,
                    openPanna = pred.openPanna,
                    openAnk = pred.openAnk,
                    closeAnk = pred.closeAnk,
                    closePanna = pred.closePanna,
                    pannelJodiFormat = pred.pannelJodiFormat,
                    subText = "Support OTC: ${pred.supportOtc.joinToString(",")}",
                    formulaTag = "STANDARD"
                )
            }
            FormulaType.A23_NEW_DIV3 -> {
                val a23 = FormulaEngine.calculateA23NewPrediction(mkt, mktEntries, userName)
                MarketFormulaSummary(
                    marketName = mkt,
                    otcDigits = a23.variant1.otcDigits,
                    superJodi = a23.variant1.superJodi,
                    openPanna = a23.variant1.openPanna,
                    openAnk = a23.variant1.openAnk,
                    closeAnk = a23.variant1.closeAnk,
                    closePanna = a23.variant1.closePanna,
                    pannelJodiFormat = a23.variant1.pannelJodiFormat,
                    subText = a23.variant1.formulaText,
                    formulaTag = "A23 (÷3)"
                )
            }
            FormulaType.A23_NEW_DIV8 -> {
                val a23 = FormulaEngine.calculateA23NewPrediction(mkt, mktEntries, userName)
                MarketFormulaSummary(
                    marketName = mkt,
                    otcDigits = a23.variant2.otcDigits,
                    superJodi = a23.variant2.superJodi,
                    openPanna = a23.variant2.openPanna,
                    openAnk = a23.variant2.openAnk,
                    closeAnk = a23.variant2.closeAnk,
                    closePanna = a23.variant2.closePanna,
                    pannelJodiFormat = a23.variant2.pannelJodiFormat,
                    subText = a23.variant2.formulaText,
                    formulaTag = "A23 (÷8)"
                )
            }
            FormulaType.A23_NEW_1 -> {
                val new1 = FormulaEngine.calculateA23New1Prediction(mkt, mktEntries)
                MarketFormulaSummary(
                    marketName = mkt,
                    otcDigits = new1.otcDigits,
                    superJodi = new1.superJodis,
                    openPanna = new1.openPanna,
                    openAnk = new1.openAnk,
                    closeAnk = new1.closeAnk,
                    closePanna = new1.closePanna,
                    pannelJodiFormat = new1.pannelJodiFormat,
                    subText = new1.step2Text,
                    formulaTag = "NEW-1"
                )
            }
        }
    }

    // Active market's active summary according to selected formula
    val activeSummary = allMarketSummaries.firstOrNull { it.marketName.equals(selectedMarket, ignoreCase = true) }
        ?: allMarketSummaries.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 👑 Official A23 Pro Formula Engine Banner Logo
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

        Spacer(modifier = Modifier.height(10.dp))

        // Market Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "👑 A23 PRO MARKET DASHBOARD",
                    color = CyberGoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Active Market: $selectedMarket",
                    color = CyberTextCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                color = CyberGoldPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CyberGoldPrimary)
            ) {
                Text(
                    text = prediction.date,
                    color = CyberGoldPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Formula Selector Bar
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                text = "⚡ SELECT FORMULA ENGINE FOR CARDS:",
                color = CyberTextAmber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FormulaType.entries.toTypedArray()) { formula ->
                    val isSelected = selectedFormula == formula
                    Surface(
                        color = if (isSelected) CyberGoldPrimary else Color(0xFF101622).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { selectedFormula = formula }
                            .testTag("home_formula_tab_${formula.name}")
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

        Spacer(modifier = Modifier.height(6.dp))

        // 1. ALL MARKET CARDS GRID/ROW
        Text(
            text = "📊 ALL MARKET CARDS (${selectedFormula.shortLabel})",
            color = CyberTextCyan,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            allMarketSummaries.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { summary ->
                        val isCurrentSelected = summary.marketName.equals(selectedMarket, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    BorderStroke(
                                        if (isCurrentSelected) 2.dp else 1.dp,
                                        if (isCurrentSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.4f)
                                    ),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectMarket(summary.marketName) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentSelected) Color(0xFF1A2234).copy(alpha = 0.7f) else Color(0xFF101622).copy(alpha = 0.40f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = summary.marketName,
                                        color = if (isCurrentSelected) CyberGoldPrimary else CyberTextCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (isCurrentSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Active",
                                            tint = CyberGoldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "OTC: ${summary.otcDigits.joinToString(", ")}",
                                    color = CyberGoldPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Panel: ${summary.pannelJodiFormat}",
                                    color = CyberTextCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Open: ${summary.openAnk} | Close: ${summary.closeAnk}",
                                    color = CyberTextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = summary.subText,
                                    color = CyberTextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. MAIN ACTIVE FEATURED OTC CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberGoldOutline.copy(alpha = 0.8f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "✨ $selectedMarket • ${selectedFormula.displayName}",
                        color = CyberGoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big OTC Numbers Display
                val displayOtc = activeSummary?.otcDigits ?: prediction.mainOtc
                Text(
                    text = displayOtc.joinToString(", "),
                    color = CyberGoldPrimary,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                val subTextDisplay = activeSummary?.subText ?: "Support OTC: ${prediction.supportOtc.joinToString(", ")}"
                Text(
                    text = subTextDisplay,
                    color = CyberTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Open Panna - Jodi - Close Panna Standard Display Box
                val displayPannelJodi = activeSummary?.pannelJodiFormat ?: prediction.pannelJodiFormat
                val displayOpenPanna = activeSummary?.openPanna ?: prediction.openPanna
                val displayOpenAnk = activeSummary?.openAnk ?: prediction.openAnk
                val displayCloseAnk = activeSummary?.closeAnk ?: prediction.closeAnk
                val displayClosePanna = activeSummary?.closePanna ?: prediction.closePanna

                Surface(
                    color = Color(0xFF162032).copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CyberGoldPrimary),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ PREDICTION FORMAT (PANEL - JODI - PANEL)",
                            color = CyberTextCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Gap & Total Logic Tag
                        val gapVal = (displayCloseAnk - displayOpenAnk + 10) % 10
                        val totVal = (displayOpenAnk + displayCloseAnk) % 10
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "🧠 OTC + GAP TRICK: Gap(अंतर) = $gapVal | Total(योग) = $totVal",
                                color = CyberNeonGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "OPEN PANEL", color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = displayOpenPanna, color = CyberGoldPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text(text = "(Open Ank: $displayOpenAnk)", color = CyberNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(text = "—", color = CyberGoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "JODI", color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${displayOpenAnk}${displayCloseAnk}", color = CyberGoldPrimary, fontSize = 26.sp, fontWeight = FontWeight.Black)
                                Text(text = "(Gap: $gapVal)", color = CyberTextCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Text(text = "—", color = CyberGoldPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "CLOSE PANEL", color = CyberTextMuted, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = displayClosePanna, color = CyberGoldPrimary, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                Text(text = "(Close Ank: $displayCloseAnk)", color = CyberNeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Full Format: $displayPannelJodi",
                            color = CyberGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Super Jodi Gold Pill Box
                val displayJodis = activeSummary?.superJodi ?: prediction.superJodi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFF101622).copy(alpha = 0.45f))
                        .border(BorderStroke(1.5.dp, CyberGoldOutline), RoundedCornerShape(30.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SUPER JODI: ${displayJodis.joinToString(", ")}",
                        color = CyberGoldPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Safe Day Green Pill Badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(CyberPassBg.copy(alpha = 0.6f))
                        .border(BorderStroke(1.dp, CyberNeonGreen), RoundedCornerShape(24.dp))
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛡️ SAFE DAY: ${prediction.safeDay} (${prediction.confidence})",
                        color = CyberNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons Row (WhatsApp & Download)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // WhatsApp Button
                    Button(
                        onClick = {
                            val shareMessage = "✨ A23 PRO PREDICTION ($selectedMarket) ✨\nFormula: ${selectedFormula.displayName}\nDate: ${prediction.date}\nOTC: ${displayOtc.joinToString(", ")}\nSuper Jodi: ${displayJodis.joinToString(", ")}\nSafe Day: ${prediction.safeDay}"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareMessage)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("whatsapp_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.5.dp, CyberNeonGreen)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = CyberNeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "WhatsApp",
                                color = CyberNeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Download Button
                    Button(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("download_button"),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.5.dp, CyberGoldOutline),
                        enabled = !isSyncing
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CyberGoldPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = CyberGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSyncing) "Syncing..." else "Download",
                                color = CyberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7-Day Accuracy Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
                .clickable { onViewFullReportClick() },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👑 A23 FORMULA ENGINE & REPORT",
                        color = CyberGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "TAP TO VIEW FULL >",
                        color = CyberGoldVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 7 Circular Check/Cross Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reports = if (sevenDayReport.isNotEmpty()) {
                        sevenDayReport.take(7)
                    } else {
                        List(7) { DayReport("26-07-2026", listOf(8, 9), "89", "689-33-157", true) }
                    }

                    reports.forEach { report ->
                        val isPass = report.isPass
                        val circleColor = if (isPass) CyberNeonGreen else CyberFailRed
                        val bg = if (isPass) CyberPassBg else CyberFailBg

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(bg)
                                .border(BorderStroke(1.5.dp, circleColor), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPass) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = if (isPass) "Pass" else "Fail",
                                tint = circleColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
