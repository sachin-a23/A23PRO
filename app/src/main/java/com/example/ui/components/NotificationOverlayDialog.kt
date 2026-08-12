package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

data class AppNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false,
    val type: String = "info"
)

@Composable
fun NotificationOverlayDialog(
    onDismiss: () -> Unit
) {
    var notifications by remember {
        mutableStateOf(
            listOf(
                AppNotificationItem(
                    id = "1",
                    title = "⚡ AI-Jarvis Core Ready",
                    message = "Multi-Platform API (Gemini / Groq / OpenAI) active and ready for prompt answers.",
                    timestamp = "Just Now",
                    type = "success"
                ),
                AppNotificationItem(
                    id = "2",
                    title = "🔮 Kalyan & Shridevi Predictions",
                    message = "Formula Engine computed new OTC & Super Jodi recommendations for live market sessions.",
                    timestamp = "5 min ago",
                    type = "info"
                ),
                AppNotificationItem(
                    id = "3",
                    title = "🛡️ 4-Digit Security PIN Enabled",
                    message = "Your VIP Account is secured with AES-256 storage and 4-digit security PIN.",
                    timestamp = "10 min ago",
                    type = "security"
                )
            )
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, CyberGoldPrimary, RoundedCornerShape(22.dp)),
            color = Color(0xFF0D1420)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberGoldPrimary.copy(alpha = 0.2f))
                                .border(1.dp, CyberGoldPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Bell",
                                tint = CyberGoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "🔔 NOTIFICATION CENTER",
                                color = CyberGoldPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Realtime System & Market Alerts",
                                color = CyberTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_notifications_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active notifications right now.",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                    ) {
                        items(notifications) { item ->
                            val accentColor = when (item.type) {
                                "success" -> CyberNeonGreen
                                "security" -> Color(0xFF00E5FF)
                                else -> CyberGoldPrimary
                            }

                            Surface(
                                color = Color(0xFF141D2B),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = if (item.type == "success") Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = "Icon",
                                        tint = accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.title,
                                                color = accentColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = item.timestamp,
                                                color = CyberTextSecondary,
                                                fontSize = 9.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = item.message,
                                            color = CyberTextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { notifications = notifications.filter { it.id != item.id } },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = CyberTextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (notifications.isNotEmpty()) {
                    TextButton(
                        onClick = { notifications = emptyList() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear All Notifications", color = CyberGoldPrimary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
