package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MarketEntry
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DataMarketScreen(
    selectedMarket: String,
    availableMarkets: List<String>,
    entries: List<MarketEntry>,
    onSelectMarket: (String) -> Unit,
    onSaveEntry: (market: String, date: String, result: String, isHoliday: Boolean, entryId: Long) -> Unit,
    onDeleteEntry: (entryId: Long) -> Unit,
    onSyncDataFromUrl: () -> Unit,
    onClearAndResetData: () -> Unit = {}
) {
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(1) } // 1: Add/Edit Entry, 2: Add/Edit Market

    var inputMarket by remember(selectedMarket) { mutableStateOf(selectedMarket) }
    var inputDate by remember {
        mutableStateOf(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
    }
    var isHoliday by remember { mutableStateOf(false) }
    var inputResult by remember { mutableStateOf("149-45-140") }
    var editingEntryId by remember { mutableStateOf<Long>(0) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Sub-Tab Switcher Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { activeSubTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == 1) CyberGoldPrimary else Color(0xFF141A24).copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        text = "1. Add/Edit Entry",
                        color = if (activeSubTab == 1) Color.Black else CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { activeSubTab = 2 },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeSubTab == 2) CyberGoldPrimary else Color(0xFF141A24).copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        text = "2. Add/Edit Market",
                        color = if (activeSubTab == 2) Color.Black else CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeSubTab == 1) {
            // Manual Data Entry Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.2.dp, CyberGoldOutline.copy(alpha = 0.7f)), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Manual Data Entry",
                            color = CyberGoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Select Market:",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Market Choice Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            availableMarkets.forEach { mkt ->
                                val isSelected = mkt.equals(inputMarket, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) CyberGoldPrimary else Color(0xFF1A2230).copy(alpha = 0.45f))
                                        .clickable {
                                            inputMarket = mkt
                                            onSelectMarket(mkt)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mkt,
                                        color = if (isSelected) Color.Black else CyberTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Date Input Field
                        OutlinedTextField(
                            value = inputDate,
                            onValueChange = { inputDate = it },
                            label = { Text("Date (DD-MM-YYYY)", color = CyberTextSecondary, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Holiday Checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isHoliday = !isHoliday }
                        ) {
                            Checkbox(
                                checked = isHoliday,
                                onCheckedChange = { isHoliday = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberGoldPrimary)
                            )
                            Text(
                                text = "Holiday / Chutti Day ( ***-**-*** )",
                                color = CyberTextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Result Input Field
                        if (!isHoliday) {
                            OutlinedTextField(
                                value = inputResult,
                                onValueChange = { inputResult = it },
                                label = { Text("Result (e.g. 149-45-140 or 445-36-260)", color = CyberTextSecondary, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGoldPrimary,
                                    unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                    focusedTextColor = CyberTextPrimary,
                                    unfocusedTextColor = CyberTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Save / Update Entry Button
                        Button(
                            onClick = {
                                if (inputDate.isNotBlank() && (isHoliday || inputResult.isNotBlank())) {
                                    onSaveEntry(inputMarket, inputDate, inputResult, isHoliday, editingEntryId)
                                    editingEntryId = 0
                                    Toast.makeText(context, "Entry Saved!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter valid date & result", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_entry_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                        ) {
                            Text(
                                text = if (editingEntryId == 0L) "Save / Update Entry" else "Update Existing Entry",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fetch/Sync Data from GitHub Link Button
                        OutlinedButton(
                            onClick = onSyncDataFromUrl,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Sync",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sync Data from GitHub Server",
                                    color = Color(0xFF00E5FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clear Old Data & Fresh Sync Button
                        OutlinedButton(
                            onClick = {
                                onClearAndResetData()
                                Toast.makeText(context, "Purana sample data hata kar naya sync shuru...", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CyberFailRed)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear & Reset",
                                    tint = CyberFailRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Clear Old Data & Fresh Sync from Server",
                                    color = CyberFailRed,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Section Title for List
            item {
                Text(
                    text = "Entries for $inputMarket (Edit/Delete)",
                    color = CyberGoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Entries List
            items(entries) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.4f)), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = entry.date,
                                color = CyberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Result: ${entry.result}",
                                color = CyberTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Edit
                            IconButton(
                                onClick = {
                                    inputMarket = entry.marketName
                                    inputDate = entry.date
                                    isHoliday = entry.isHoliday
                                    inputResult = entry.result
                                    editingEntryId = entry.id
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFF00E5FF)
                                )
                            }

                            // Delete
                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CyberFailRed
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Add/Edit Market subtab
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Custom Market Configurations",
                            color = CyberGoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Manage server URLs for Kalyan, Shridevi, Milan, and Time Bazar",
                            color = CyberTextSecondary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        availableMarkets.forEach { mkt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = mkt, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "Connected (GitHub Live)", color = CyberNeonGreen, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
