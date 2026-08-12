package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

sealed class NavTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavTab("home", "Home", Icons.Default.Home)
    object Report : NavTab("report", "A23", Icons.Default.BarChart)
    object DataMarket : NavTab("data_market", "Data & Market", Icons.Default.Add)
    object History : NavTab("history", "History", Icons.Default.History)
    object Settings : NavTab("settings", "Settings", Icons.Default.Settings)
    object Jarvis : NavTab("jarvis", "AI Jarvis", Icons.Default.Mic)
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(
        NavTab.Home,
        NavTab.Report,
        NavTab.DataMarket,
        NavTab.History,
        NavTab.Settings
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0D121B).copy(alpha = 0.45f))
            .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isSelected = currentRoute == tab.route
                val tintColor = if (isSelected) CyberGoldPrimary else CyberTextSecondary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab.route) }
                        .testTag("nav_tab_${tab.route}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = tintColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        color = tintColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
