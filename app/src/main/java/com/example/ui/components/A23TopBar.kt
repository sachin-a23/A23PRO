package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun A23TopBar(
    selectedMarket: String,
    availableMarkets: List<String>,
    onMarketSelect: (String) -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onJarvisClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Icon Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu Button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141A24).copy(alpha = 0.50f))
                    .border(BorderStroke(1.2.dp, CyberGoldOutline), CircleShape)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = CyberGoldPrimary
                )
            }

            // Center Logo Badge "👑 A23 PRO FORMULA ENGINE"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF1F1A0A).copy(alpha = 0.65f),
                                Color(0xFF0D1728).copy(alpha = 0.65f),
                                Color(0xFF1B1609).copy(alpha = 0.65f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            Brush.horizontalGradient(
                                colors = listOf(CyberGoldPrimary, Color(0xFF00E5FF), CyberGoldPrimary)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "👑", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "A23 PRO ENGINE",
                        color = CyberGoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Right Icons (Notifications + Mic + Profile)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B263B).copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, CyberGoldOutline), CircleShape)
                        .testTag("topbar_notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onJarvisClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberGoldPrimary.copy(alpha = 0.18f))
                        .border(BorderStroke(1.2.dp, CyberGoldPrimary), CircleShape)
                        .testTag("jarvis_mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "AI Jarvis Mic",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00B0FF).copy(alpha = 0.2f))
                        .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape)
                        .testTag("profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Markets & Predictions",
            color = CyberTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Market Chips Row (Single horizontal line)
        val marketList = if (availableMarkets.isNotEmpty()) availableMarkets else listOf("KALYAN", "SHRIDEVI", "MILAN DAY", "TIME BAZAR", "KALYAN NIGHT", "MAIN BAZAR")
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(marketList.size) { index ->
                val market = marketList[index]
                val isSelected = market.equals(selectedMarket, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) CyberGoldPrimary else Color(0xFF121822).copy(alpha = 0.50f)
                        )
                        .border(
                            BorderStroke(
                                1.2.dp,
                                if (isSelected) CyberGoldOutline else CyberGoldOutline.copy(alpha = 0.4f)
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onMarketSelect(market) }
                        .padding(horizontal = 16.dp)
                        .testTag("market_chip_$market"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = market.uppercase(),
                        color = if (isSelected) Color.Black else CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
