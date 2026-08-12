package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CyberGoldOutline
import com.example.ui.theme.CyberGoldPrimary
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun SideMenuDrawerContent(
    userName: String,
    userMobile: String,
    onNavigate: (String) -> Unit,
    onOpenPdfReport: () -> Unit,
    onOpenOcrScanner: () -> Unit,
    onOpenProfileDialog: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color(0xFF0A0F18).copy(alpha = 0.88f),
        drawerContentColor = CyberTextPrimary,
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 👑 A23 Pro Formula Engine Official Banner Logo in Menu
            Image(
                painter = painterResource(id = R.drawable.img_a23_app_logo_1786420070249),
                contentDescription = "A23 Pro Official Banner Logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.2.dp, CyberGoldPrimary.copy(alpha = 0.8f)), RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Top User Profile Header Box
            Surface(
                color = Color(0xFF141C2B).copy(alpha = 0.75f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, CyberGoldOutline),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onOpenProfileDialog()
                        onCloseDrawer()
                    }
                    .testTag("side_menu_profile_header")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberGoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, CyberGoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Avatar",
                            tint = CyberGoldPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName.ifBlank { "Pro User" },
                                color = CyberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "VIP",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = userMobile.ifBlank { "+91 9876543210" },
                            color = CyberTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "MAIN NAVIGATION",
                color = CyberGoldOutline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Items
            DrawerMenuItem("Home Dashboard", Icons.Default.Home, "home") {
                onNavigate("home")
                onCloseDrawer()
            }

            DrawerMenuItem("A23 Prediction Engine", Icons.Default.Assessment, "report") {
                onNavigate("report")
                onCloseDrawer()
            }

            DrawerMenuItem("Data Market Entry", Icons.Default.Storage, "data_market") {
                onNavigate("data_market")
                onCloseDrawer()
            }

            DrawerMenuItem("Historical Records", Icons.Default.History, "history") {
                onNavigate("history")
                onCloseDrawer()
            }

            DrawerMenuItem("AI Jarvis (Full Screen)", Icons.Default.Psychology, "jarvis", highlight = true) {
                onNavigate("jarvis")
                onCloseDrawer()
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = CyberGoldOutline.copy(alpha = 0.3f)
            )

            Text(
                text = "PRO TOOLS & EXPORTS",
                color = CyberGoldOutline,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            DrawerMenuItem("Export PDF Report", Icons.Default.PictureAsPdf, "pdf_report") {
                onOpenPdfReport()
                onCloseDrawer()
            }

            DrawerMenuItem("Photo Scan / OCR", Icons.Default.DocumentScanner, "ocr_scan") {
                onOpenOcrScanner()
                onCloseDrawer()
            }

            DrawerMenuItem("User Profile & VIP Pass", Icons.Default.Person, "profile") {
                onOpenProfileDialog()
                onCloseDrawer()
            }

            DrawerMenuItem("Settings & Wallpapers", Icons.Default.Settings, "settings") {
                onNavigate("settings")
                onCloseDrawer()
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Version
            Surface(
                color = Color(0xFF080D14),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "A23 PRO v2.5.0 Premium",
                        color = CyberGoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "On-Device Neural Engine Active",
                        color = CyberTextSecondary,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    testTagKey: String,
    highlight: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        color = if (highlight) CyberGoldPrimary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = if (highlight) BorderStroke(1.dp, CyberGoldPrimary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onClick() }
            .testTag("drawer_menu_$testTagKey")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (highlight) CyberGoldPrimary else CyberTextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = if (highlight) CyberGoldPrimary else CyberTextPrimary,
                fontSize = 13.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
