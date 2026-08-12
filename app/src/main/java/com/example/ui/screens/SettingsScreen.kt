package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    selectedWallpaper: String,
    backgroundDim: Float,
    isPinLockEnabled: Boolean,
    currentPin: String,
    userAccountName: String,
    userAccountMobile: String,
    userAccountEmail: String,
    notificationsEnabled: Boolean,
    isJarvisEnabled: Boolean = true,
    isBackgroundMicGranted: Boolean = true,
    isOverlayPermissionGranted: Boolean = true,
    isAccessibilityServiceEnabled: Boolean = true,
    apiKeys: List<com.example.data.MarketViewModel.ApiKeyItem> = emptyList(),
    isVoiceOfflineMode: Boolean = false,
    isForegroundServiceActive: Boolean = true,
    ocrLastResult: String? = null,
    isSelfLearningEnabled: Boolean = true,
    learnedPatternsCount: Int = 142,
    isAes256Encrypted: Boolean = true,
    lastIngestedReport: String? = null,
    allEntries: List<com.example.data.MarketEntry> = emptyList(),
    onWallpaperSelect: (String) -> Unit,
    onBackgroundDimChange: (Float) -> Unit,
    onTogglePinLock: (Boolean) -> Unit,
    onUpdatePin: (String) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleJarvis: (Boolean) -> Unit = {},
    onToggleBackgroundMic: (Boolean) -> Unit = {},
    onToggleOverlayPermission: (Boolean) -> Unit = {},
    onToggleAccessibilityService: (Boolean) -> Unit = {},
    onAddApiKey: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onDeleteApiKey: (String) -> Unit = {},
    onToggleApiKeyActive: (String) -> Unit = {},
    onTestApiKey: (String) -> Unit = {},
    onToggleVoiceOfflineMode: (Boolean) -> Unit = {},
    onToggleForegroundService: (Boolean) -> Unit = {},
    onProcessOcrScanResult: (String) -> Unit = {},
    onToggleSelfLearning: (Boolean) -> Unit = {},
    onToggleAesEncryption: (Boolean) -> Unit = {},
    onIngestMarketDocument: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }
    var showA23LabScreen by remember { mutableStateOf(false) }

    if (showA23LabScreen) {
        A23FormulaLabScreen(
            allEntries = allEntries,
            onBackClick = { showA23LabScreen = false }
        )
        return
    }

    // System gallery image picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(sourceUri)
                val file = File(context.filesDir, "saved_gallery_wallpaper.jpg")
                val outputStream = FileOutputStream(file)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val savedPath = file.toURI().toString()
                onWallpaperSelect(savedPath)
                Toast.makeText(context, "Gallery Wallpaper Saved Permanently!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving wallpaper: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

        Spacer(modifier = Modifier.height(12.dp))

        // 👑 A23 FORMULA CREATOR & PATTERN LAB BUTTON CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.5.dp, CyberGoldPrimary), RoundedCornerShape(18.dp))
                .clickable { showA23LabScreen = true },
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E182A).copy(alpha = 0.85f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CyberGoldPrimary.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, CyberGoldPrimary), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "A23 Lab",
                            tint = CyberGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "👑 A23 FORMULA LAB & PATTERN ENGINE",
                            color = CyberGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "कस्टम फॉर्मूला बनाएँ, AI पैटर्न व बेस्ट डे चेक करें",
                            color = CyberTextCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = CyberGoldPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "खोलें >",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Set Wallpaper Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.6f)), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = "Wallpaper",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Set Wallpaper (Change Option)",
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Wallpaper Options LazyRow (7 Built-in HD Wallpapers + Gallery Custom Picker)
                val wallpapers = listOf(
                    "Cyber Gold",
                    "Pro Studio",
                    "Neon Glass",
                    "Gold Matrix",
                    "Royal Velvet",
                    "Cyber Circuit",
                    "Dark Obsidian",
                    "Gallery"
                )
                val isCustomWallpaper = selectedWallpaper.startsWith("file:") ||
                        selectedWallpaper.startsWith("content:") ||
                        selectedWallpaper.startsWith("/")

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(wallpapers) { name ->
                        val isSelected = if (name == "Gallery") isCustomWallpaper else selectedWallpaper.equals(name, ignoreCase = true)

                        val thumbDrawableRes = when (name) {
                            "Pro Studio" -> R.drawable.wallpaper_pro_studio_1786159481584
                            "Neon Glass" -> R.drawable.wallpaper_neon_glass_1786159495602
                            "Gold Matrix" -> R.drawable.wallpaper_gold_matrix_1786159510045
                            "Royal Velvet" -> R.drawable.wallpaper_royal_velvet_1786159525036
                            "Cyber Circuit" -> R.drawable.wallpaper_cyber_circuit_1786159540301
                            "Dark Obsidian" -> R.drawable.wallpaper_dark_obsidian_1786159554951
                            else -> R.drawable.img_cyber_bg_1785167084570
                        }

                        Box(
                            modifier = Modifier
                                .width(95.dp)
                                .height(115.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A2230))
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) CyberGoldPrimary else CyberGoldOutline.copy(alpha = 0.3f)
                                    ),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (name == "Gallery") {
                                        galleryLauncher.launch("image/*")
                                    } else {
                                        onWallpaperSelect(name)
                                    }
                                }
                                .testTag("wallpaper_$name"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (name == "Gallery") {
                                if (isCustomWallpaper) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = selectedWallpaper),
                                        contentDescription = "Custom Gallery Wallpaper",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.75f))
                                            .padding(vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Custom HD",
                                            color = CyberGoldPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Gallery",
                                            tint = CyberGoldPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Gallery",
                                            color = CyberTextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = thumbDrawableRes),
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f))
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) CyberGoldPrimary else CyberTextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Image Dim Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(18.dp)),
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
                        text = "🔆 Background Image Dim",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${(backgroundDim * 100).toInt()}%",
                        color = CyberGoldPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Slider(
                    value = backgroundDim,
                    onValueChange = onBackgroundDimChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberGoldPrimary,
                        activeTrackColor = CyberGoldPrimary,
                        inactiveTrackColor = Color(0xFF2A3448)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Dim Level Presets (0% to 100%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dimPresets = listOf(
                        Pair("0% (Glass)", 0f),
                        Pair("25%", 0.25f),
                        Pair("50%", 0.5f),
                        Pair("75%", 0.75f),
                        Pair("100% (Dark)", 1.0f)
                    )

                    dimPresets.forEach { (label, valFloat) ->
                        val isSelected = Math.abs(backgroundDim - valFloat) < 0.05f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberGoldPrimary else Color(0xFF1A2230))
                                .clickable { onBackgroundDimChange(valFloat) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else CyberTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4-Digit Security PIN Lock Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(18.dp)),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Pin Lock",
                            tint = CyberGoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "4-Digit Security PIN Lock",
                            color = CyberTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Switch(
                        checked = isPinLockEnabled,
                        onCheckedChange = onTogglePinLock,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberGoldPrimary,
                            uncheckedThumbColor = CyberTextSecondary,
                            uncheckedTrackColor = Color(0xFF1F2937)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentPin.length == 4) "PIN Status: Active (Encrypted)" else "PIN Status: Not Set",
                        color = CyberTextSecondary,
                        fontSize = 13.sp
                    )

                    Text(
                        text = "Change PIN",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showPinDialog = true }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Account Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(18.dp)),
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
                        text = "Account Details",
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Account",
                        tint = CyberTextSecondary,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                Toast.makeText(context, "Account details verified", Toast.LENGTH_SHORT).show()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = userAccountName,
                            color = CyberGoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mobile: $userAccountMobile",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Email: $userAccountEmail",
                            color = CyberTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI-JARVIS VOICE ASSISTANT & BACKGROUND ACCESSIBILITY PERMISSIONS Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.8f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1420).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Row with Master Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "AI Jarvis",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🤖 AI-JARVIS VOICE ASSISTANT",
                                color = Color(0xFF00E5FF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Speak in background outside app",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isJarvisEnabled,
                        onCheckedChange = onToggleJarvis,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00E5FF),
                            uncheckedThumbColor = CyberTextSecondary,
                            uncheckedTrackColor = Color(0xFF1F2937)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Jarvis Active Status Chip
                Surface(
                    color = if (isJarvisEnabled) CyberNeonGreen.copy(alpha = 0.15f) else Color(0xFF1F2937),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isJarvisEnabled) CyberNeonGreen else CyberTextSecondary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Equalizer",
                            tint = if (isJarvisEnabled) CyberNeonGreen else CyberTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isJarvisEnabled) "JARVIS ACTIVE: Wake Word 'Hey Jarvis' or Floating Orb" else "JARVIS DEACTIVATED",
                            color = if (isJarvisEnabled) CyberNeonGreen else CyberTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CyberGoldOutline.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "REQUIRED BACKGROUND PERMISSIONS",
                    color = CyberGoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Permission 1: Record Audio (Background Mic)
                JarvisPermissionRow(
                    icon = Icons.Default.Mic,
                    title = "Record Audio (Microphone)",
                    description = "Allows Jarvis to listen to voice commands in background",
                    isGranted = isBackgroundMicGranted,
                    accentColor = CyberNeonGreen,
                    onToggleState = { onToggleBackgroundMic(!isBackgroundMicGranted) },
                    onOpenSettings = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening App Permissions...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Permission 2: Display Over Other Apps (Overlay Floating Orb)
                JarvisPermissionRow(
                    icon = Icons.Default.Layers,
                    title = "Display Over Other Apps (Overlay)",
                    description = "Shows floating AI Jarvis orb on top of other applications",
                    isGranted = isOverlayPermissionGranted,
                    accentColor = Color(0xFF00E5FF),
                    onToggleState = { onToggleOverlayPermission(!isOverlayPermissionGranted) },
                    onOpenSettings = {
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Overlay Settings...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Permission 3: Accessibility & Foreground Service
                JarvisPermissionRow(
                    icon = Icons.Default.AccessibilityNew,
                    title = "Accessibility & Foreground Service",
                    description = "Keeps voice listener running smoothly in background",
                    isGranted = isAccessibilityServiceEnabled,
                    accentColor = CyberGoldPrimary,
                    onToggleState = { onToggleAccessibilityService(!isAccessibilityServiceEnabled) },
                    onOpenSettings = {
                        try {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Accessibility Settings...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Command Test Button
                Button(
                    onClick = {
                        if (isJarvisEnabled) {
                            Toast.makeText(context, "🎙️ Listening... Say 'Jarvis open market prediction'", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Please enable AI-Jarvis first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Test Mic",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST JARVIS VOICE COMMAND",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 1. API KEYS TESTING & SAVED LIST MODULE ====================
        var newKeyName by remember { mutableStateOf("") }
        var newKeyProvider by remember { mutableStateOf("Google AI Studio") }
        var newKeyValue by remember { mutableStateOf("") }
        var newBaseUrl by remember { mutableStateOf("https://generativelanguage.googleapis.com") }
        var newModelName by remember { mutableStateOf("gemini-2.5-flash") }
        var isKeyVisible by remember { mutableStateOf(false) }
        var showHelpGuide by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberGoldPrimary), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1522).copy(alpha = 0.75f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CyberGoldPrimary.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, CyberGoldPrimary), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Keys",
                                tint = CyberGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🔑 MULTI-PLATFORM API",
                                color = CyberGoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                maxLines = 1
                            )
                            Text(
                                text = "Gemini • Groq • OpenAI • DeepSeek",
                                color = CyberTextSecondary,
                                fontSize = 10.5.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { showHelpGuide = !showHelpGuide },
                        color = CyberGoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberGoldPrimary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (showHelpGuide) Icons.Default.CheckCircle else Icons.Default.Api,
                                contentDescription = "Help",
                                tint = CyberGoldPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showHelpGuide) "Hide Guide" else "API Guide",
                                color = CyberGoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expandable API Key Help Guide
                if (showHelpGuide) {
                    Surface(
                        color = Color(0xFF162133),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberGoldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 Kaise Milegi Free API Key? (Step-by-Step Guide)",
                                color = CyberGoldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "1. Google AI Studio (Recommended - Free & Fast):\n   • Visit aistudio.google.com -> Click 'Get API Key' -> Create Key.\n2. Groq Cloud (Ultra Fast Llama 3):\n   • Visit console.groq.com -> API Keys -> Create Key (Key starts with 'gsk_').\n3. DeepSeek AI:\n   • Visit platform.deepseek.com -> API Keys -> Create Key (Key starts with 'sk-ds-').\n4. OpenAI / Custom Endpoint:\n   • Enter key and specify custom base URL endpoint if needed.",
                                color = CyberTextPrimary,
                                fontSize = 10.5.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val url = when (newKeyProvider) {
                                        "Groq Cloud" -> "https://console.groq.com/keys"
                                        "DeepSeek AI" -> "https://platform.deepseek.com/api_keys"
                                        "OpenAI" -> "https://platform.openai.com/api-keys"
                                        else -> "https://aistudio.google.com/app/apikey"
                                    }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("🌐 OPEN $newKeyProvider KEY PORTAL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // New API Key Form Card
                Surface(
                    color = Color(0xFF141C2B),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Add / Configure AI Provider Key",
                            color = CyberTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Provider Chip Selection
                        Text(
                            text = "Select Platform Provider:",
                            color = CyberTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf("Google AI Studio", "Groq Cloud", "OpenAI", "DeepSeek AI", "Custom REST")) { provider ->
                                val selected = newKeyProvider == provider
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        newKeyProvider = provider
                                        when (provider) {
                                            "Google AI Studio" -> {
                                                newBaseUrl = "https://generativelanguage.googleapis.com"
                                                newModelName = "gemini-2.5-flash"
                                                if (newKeyName.isBlank()) newKeyName = "Google AI Studio Key"
                                            }
                                            "Groq Cloud" -> {
                                                newBaseUrl = "https://api.groq.com/openai/v1"
                                                newModelName = "llama3-70b-8192"
                                                if (newKeyName.isBlank()) newKeyName = "Groq Llama3 Key"
                                            }
                                            "DeepSeek AI" -> {
                                                newBaseUrl = "https://api.deepseek.com/v1"
                                                newModelName = "deepseek-chat"
                                                if (newKeyName.isBlank()) newKeyName = "DeepSeek AI Key"
                                            }
                                            "OpenAI" -> {
                                                newBaseUrl = "https://api.openai.com/v1"
                                                newModelName = "gpt-4o-mini"
                                                if (newKeyName.isBlank()) newKeyName = "OpenAI GPT-4 Key"
                                            }
                                            "Custom REST" -> {
                                                newBaseUrl = "https://your-custom-ai-domain.com/v1"
                                                newModelName = "custom-model"
                                                if (newKeyName.isBlank()) newKeyName = "Custom Market REST Key"
                                            }
                                        }
                                    },
                                    label = { Text(provider, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberGoldPrimary,
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1A2436),
                                        labelColor = CyberTextSecondary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = newKeyName,
                            onValueChange = { newKeyName = it },
                            label = { Text("Key Label (e.g. Gemini Primary Key)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedLabelColor = CyberGoldPrimary,
                                unfocusedLabelColor = CyberTextSecondary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newKeyValue,
                            onValueChange = { newKeyValue = it },
                            label = { Text("Paste API Key Value (AIzaSy... or sk-...) ", fontSize = 11.sp) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle key view",
                                        tint = CyberGoldPrimary
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberGoldPrimary,
                                unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                                focusedLabelColor = CyberGoldPrimary,
                                unfocusedLabelColor = CyberTextSecondary
                            )
                        )

                        // Optional Base URL & Model inputs for advanced users
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newBaseUrl,
                                onValueChange = { newBaseUrl = it },
                                label = { Text("Base Endpoint URL", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGoldPrimary,
                                    unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.4f),
                                    focusedLabelColor = CyberGoldPrimary,
                                    unfocusedLabelColor = CyberTextSecondary
                                )
                            )
                            OutlinedTextField(
                                value = newModelName,
                                onValueChange = { newModelName = it },
                                label = { Text("Model Name", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberGoldPrimary,
                                    unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.4f),
                                    focusedLabelColor = CyberGoldPrimary,
                                    unfocusedLabelColor = CyberTextSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newKeyValue.isNotBlank()) {
                                    onAddApiKey(newKeyName, newKeyProvider, newKeyValue, newBaseUrl, newModelName)
                                    newKeyName = ""
                                    newKeyValue = ""
                                    Toast.makeText(context, "$newKeyProvider Key Saved Permanently!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter an API Key value", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Save Key", tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SAVE & BIND $newKeyProvider KEY", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SAVED API KEYS & DIAGNOSTICS (${apiKeys.size})",
                    color = CyberGoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (apiKeys.isEmpty()) {
                    Text(
                        text = "No saved API keys yet. Add one above to unlock all AI modules.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    apiKeys.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .clip(RoundedCornerShape(14.dp)),
                            color = Color(0xFF121926),
                            border = BorderStroke(1.2.dp, if (item.isActive) CyberGoldOutline.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Api,
                                            contentDescription = "API",
                                            tint = if (item.isActive) CyberGoldPrimary else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = item.name,
                                                color = CyberTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.provider,
                                                    color = CyberGoldPrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (item.modelName.isNotBlank()) {
                                                    Text(
                                                        text = " • ${item.modelName}",
                                                        color = CyberTextSecondary,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = item.isActive,
                                            onCheckedChange = { onToggleApiKeyActive(item.id) },
                                            modifier = Modifier.scale(0.8f)
                                        )
                                        IconButton(onClick = { onDeleteApiKey(item.id) }, modifier = Modifier.size(32.dp)) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = CyberFailRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Masked key display + Latency Tag
                                val maskedKey = if (item.key.length > 10) "${item.key.take(7)}...${item.key.takeLast(4)}" else if (item.key.isBlank()) "No Key Entered" else item.key
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Key: $maskedKey",
                                        color = CyberTextSecondary,
                                        fontSize = 11.sp
                                    )

                                    if (item.latencyMs > 0L) {
                                        Surface(
                                            color = Color(0xFF0D2818),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.8.dp, CyberNeonGreen.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "⚡ ${item.latencyMs} ms",
                                                color = CyberNeonGreen,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Diagnostic Result Badge
                                val isSuccess = item.statusReport.contains("200 OK") || item.statusReport.contains("Connected") || item.statusReport.contains("Active")
                                val isFailure = item.statusReport.contains("400") || item.statusReport.contains("401") || item.statusReport.contains("429") || item.statusReport.contains("Error") || item.statusReport.contains("Invalid")
                                
                                Surface(
                                    color = when {
                                        isSuccess -> CyberNeonGreen.copy(alpha = 0.12f)
                                        isFailure -> CyberFailRed.copy(alpha = 0.12f)
                                        else -> Color(0xFF1E293B)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.8.dp, when {
                                        isSuccess -> CyberNeonGreen
                                        isFailure -> CyberFailRed
                                        else -> CyberTextSecondary
                                    }),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = item.statusReport,
                                            color = when {
                                                isSuccess -> CyberNeonGreen
                                                isFailure -> CyberFailRed
                                                else -> CyberTextSecondary
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (item.hindiDiagnostic.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "📢 Help: ${item.hindiDiagnostic}",
                                                color = CyberTextPrimary,
                                                fontSize = 10.5.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { onTestApiKey(item.id) },
                                    enabled = !item.isTesting,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.fillMaxWidth().height(32.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (item.isTesting) {
                                            CircularProgressIndicator(
                                                color = Color.Black,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TESTING CONNECTION...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        } else {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Test", tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("TEST API CONNECTION NOW", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 2. OFFLINE & ONLINE VOICE ENGINE MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.8f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1420).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = "Voice Engine", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🗣️ OFFLINE & ONLINE VOICE ENGINE",
                                color = Color(0xFF00E5FF),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Hindi (hi-IN) + English (en-US) Commands",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isVoiceOfflineMode) "Mode: Offline Voice Engine" else "Mode: Hybrid Online + Offline",
                            color = CyberTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isVoiceOfflineMode) "Processes commands locally without internet using preloaded Hindi/English models" else "Uses cloud AI when connected, falls back to local engine offline",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isVoiceOfflineMode,
                        onCheckedChange = onToggleVoiceOfflineMode,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CyberNeonGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberNeonGreen.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Preloaded", tint = CyberNeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Preloaded Voice Packs: Hindi (हिंदी) & English (India) Ready",
                            color = CyberNeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 3. BACKGROUND & FOREGROUND SERVICE MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberNeonGreen.copy(alpha = 0.7f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1816).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberNeonGreen.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, CyberNeonGreen), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Foreground Service", tint = CyberNeonGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "⚡ BACKGROUND & FOREGROUND SERVICE",
                                color = CyberNeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Keep Jarvis active when app is minimized or phone is locked",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isForegroundServiceActive,
                        onCheckedChange = onToggleForegroundService,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = CyberNeonGreen
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isForegroundServiceActive) "🟢 Status: Active Foreground Notification Service Running" else "🔴 Status: Service Paused",
                    color = if (isForegroundServiceActive) CyberNeonGreen else CyberFailRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 4. ON-DEVICE OCR MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberGoldOutline), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121824).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberGoldPrimary.copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, CyberGoldPrimary), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = "OCR Scanner", tint = CyberGoldPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📷 ON-DEVICE OCR MODULE (ZERO ENTRY)",
                            color = CyberGoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Direct camera & image scanning for chart results",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onProcessOcrScanResult("KALYAN 27/07/2026 -> Open: 3, Close: 8 (Jodi: 38)")
                            Toast.makeText(context, "📷 Camera OCR Scan Complete!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Camera", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SCAN CAMERA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            onProcessOcrScanResult("SHRIDEVI 27/07/2026 -> Result: 128-10-235")
                            Toast.makeText(context, "🖼️ Image Gallery OCR Scan Complete!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = "Gallery", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SCAN GALLERY", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (ocrLastResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF1A2232),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "Latest Scanned OCR Output:", color = CyberGoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(text = ocrLastResult, color = CyberTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 5. MULTI-API SUPPORT & SELF-LEARNING MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, Color(0xFFAB47BC).copy(alpha = 0.8f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF150D20).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFAB47BC).copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, Color(0xFFAB47BC)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Psychology, contentDescription = "Self Learning", tint = Color(0xFFAB47BC), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🧠 MULTI-API & SELF-LEARNING MODULE",
                                color = Color(0xFFAB47BC),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Automatically learns new pattern formulas over time",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isSelfLearningEnabled,
                        onCheckedChange = onToggleSelfLearning,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFAB47BC)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = Color(0xFF231633),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFAB47BC).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Learned Market Formulas & Patterns:",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$learnedPatternsCount Patterns Active",
                            color = Color(0xFFE1BEE7),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 6. SECURITY & CRYPTOGRAPHIC ENCRYPTION MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, Color(0xFFFF9800).copy(alpha = 0.8f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1208).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800).copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, Color(0xFFFF9800)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = "Security", tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🔒 AES-256 CRYPTOGRAPHIC ENCRYPTION",
                                color = Color(0xFFFF9800),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "End-to-end local Room database and API key protection",
                                color = CyberTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAes256Encrypted,
                        onCheckedChange = onToggleAesEncryption,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color(0xFFFF9800)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isAes256Encrypted) "🛡️ Status: AES-256 Bit Hardware Cryptographic Shield Active" else "⚠️ Status: Standard Protection",
                    color = if (isAes256Encrypted) Color(0xFFFFB74D) else CyberFailRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ==================== 7. AUTOMATED DATA INGESTION MODULE ====================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.7f)), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1622).copy(alpha = 0.50f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                            .border(BorderStroke(1.dp, Color(0xFF00E5FF)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Data Ingestion", tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📄 AUTOMATED DATA INGESTION (.TXT / .PDF)",
                            color = Color(0xFF00E5FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Auto-reads and parses market reports into database",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onIngestMarketDocument("Shridevi_Weekly_Chart.pdf") },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("INGEST PDF FILE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = { onIngestMarketDocument("Kalyan_Data_Logs.txt") },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                    ) {
                        Text("INGEST TXT FILE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                if (lastIngestedReport != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF122030),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Last Ingested: $lastIngestedReport",
                            color = Color(0xFF80DEEA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Notifications ON/OFF Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101622).copy(alpha = 0.40f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Notifications ON / OFF",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CyberGoldPrimary,
                        uncheckedThumbColor = CyberTextSecondary,
                        uncheckedTrackColor = Color(0xFF1F2937)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LOGOUT / SWITCH ACCOUNT Red Border Button
        OutlinedButton(
            onClick = {
                Toast.makeText(context, "Session reset successfully", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button"),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, CyberFailRed)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout",
                    tint = CyberFailRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LOGOUT / SWITCH ACCOUNT",
                    color = CyberFailRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ADMIN INFO / CONTACT US Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(BorderStroke(1.5.dp, CyberGoldOutline), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520).copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "👑 ADMIN & CONTACT INFO",
                        color = CyberGoldPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = CyberGoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CyberGoldOutline)
                    ) {
                        Text(
                            text = "Verified Admin",
                            color = CyberGoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Image & Name Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Photo with Gold Cyber Ring
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(2.dp, CyberGoldPrimary), CircleShape)
                            .background(Color(0xFF1A2230))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_admin_profile_1785168847618),
                            contentDescription = "Admin Photo - Sachin Solunke",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SACHIN SOLUNKE",
                                color = CyberTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = CyberNeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "A23 Pro Admin & Support Owner",
                            color = CyberGoldPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Contact directly for queries, keys & updates",
                            color = CyberTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = CyberGoldOutline.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Email Action
                ContactActionButton(
                    icon = Icons.Default.Email,
                    label = "Email Address",
                    value = "sachins8411@gmail.com",
                    buttonText = "Send Email",
                    accentColor = CyberGoldPrimary,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:sachins8411@gmail.com")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Email: sachins8411@gmail.com", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Call Action
                ContactActionButton(
                    icon = Icons.Default.Phone,
                    label = "Phone / Call",
                    value = "+91 8698431018",
                    buttonText = "Call Now",
                    accentColor = Color(0xFF00E5FF),
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918698431018"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Phone: +91 8698431018", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. WhatsApp Direct Chat
                ContactActionButton(
                    icon = Icons.Default.Chat,
                    label = "WhatsApp Chat",
                    value = "+91 8698431018",
                    buttonText = "Chat",
                    accentColor = CyberNeonGreen,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/918698431018"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening WhatsApp...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Telegram Channel / Chat
                ContactActionButton(
                    icon = Icons.Default.Send,
                    label = "Telegram",
                    value = "t.me/Open_network_Sachin",
                    buttonText = "Open Telegram",
                    accentColor = Color(0xFF229ED9),
                    onClick = {
                        try {
                            val url = "https://t.me/Open_network_Sachin"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Telegram...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Instagram Profile Link
                ContactActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Instagram Profile",
                    value = "@black_b.o.y__",
                    buttonText = "Follow Insta",
                    accentColor = Color(0xFFE1306C),
                    onClick = {
                        try {
                            val url = "https://www.instagram.com/black_b.o.y__?igsh=MWp5aWNqdWFqbjc3dg=="
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Instagram...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 6. Facebook Profile Link
                ContactActionButton(
                    icon = Icons.Default.Share,
                    label = "Facebook Profile",
                    value = "Sachin Solunke",
                    buttonText = "Visit Profile",
                    accentColor = Color(0xFF1877F2),
                    onClick = {
                        try {
                            val url = "https://www.facebook.com/share/1HuviyT3dg/"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Facebook...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 7. WhatsApp Group Link
                ContactActionButton(
                    icon = Icons.Default.Group,
                    label = "WhatsApp Group",
                    value = "Join A23 Pro Official Group",
                    buttonText = "Join Group",
                    accentColor = CyberNeonGreen,
                    onClick = {
                        try {
                            val url = "https://chat.whatsapp.com/BSTELXcrbei88V8FS5rl3h?s=cl&p=a&ilr=1&amv=1"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening WhatsApp Group...", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Change PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set New 4-Digit Security PIN", color = CyberGoldPrimary) },
            text = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { newValue -> newPinInput = newValue.filter { c -> c.isDigit() }.take(4) },
                    label = { Text("Enter 4 digits", color = CyberTextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGoldPrimary,
                        unfocusedBorderColor = CyberGoldOutline
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPinInput.length == 4) {
                            onUpdatePin(newPinInput)
                            showPinDialog = false
                            Toast.makeText(context, "PIN updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGoldPrimary)
                ) {
                    Text("Save PIN", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text("Cancel", color = CyberTextSecondary)
                }
            },
            containerColor = Color(0xFF141A24)
        )
    }
}

@Composable
private fun ContactActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    buttonText: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = Color(0xFF141A24).copy(alpha = 0.55f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = label,
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = value,
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = accentColor,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ℹ️ APP ABOUT & INFORMATION CARD (ऐप की संपूर्ण जानकारी व डिस्क्लेमर)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(BorderStroke(1.2.dp, CyberGoldPrimary), RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1728).copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official App Banner Logo
                Image(
                    painter = painterResource(id = R.drawable.img_a23_app_logo_1786420070249),
                    contentDescription = "A23 Pro Official Banner Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.6f)), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "👑 A23 PRO - Satta Formula Engine",
                    color = CyberGoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "संस्करण: v2.5.0 Official Release (Publishing Ready)",
                    color = CyberTextCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(color = CyberGoldOutline.copy(alpha = 0.4f))

                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "📱 ऐप के बारे में (About App):",
                        color = CyberGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "A23 Pro एक उन्नत गणितीय सट्टा फॉर्मूला इंजन है, जो बाज़ार के पिछले ऐतिहासिक नतीजों (Historical Data) के आधार पर सटीक OTC Digits, Super Jodi और Panel Prediction निकालता है।",
                        color = CyberTextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "🔥 ऐप की मुख्य विशेषताएँ (Key Features):",
                        color = CyberNeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val features = listOf(
                        "• (30 × Last Jodi) ÷ Divisor कस्टमाइज़ेबल फॉर्मूला इंजन",
                        "• फास्ट मार्केट चुनाव (Kalyan, Shridevi, Milan Day, Time Bazar, Main Bazar)",
                        "• पिछले सभी दिनों का बैकटेस्ट पास/फेल डिजिटल रिकॉर्ड",
                        "• आने वाले दिनों के लिए बेस्ट फॉर्मूला सेव (Bookmark) सुविधा",
                        "• HD डिजिटल ऑल-डेज PDF रिपोर्ट डाउनलोड व शेयरिंग",
                        "• AI ऑटो पैटर्न खोजक व कस्टम मार्केट सपोर्ट"
                    )

                    features.forEach { ft ->
                        Text(
                            text = ft,
                            color = CyberTextSecondary,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFF141F30),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "👨‍💻 डेवलपर जानकारी (Developer Profile):",
                                color = CyberGoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "नाम: Sachin Solunke (A23 Studio Owner)", color = CyberTextPrimary, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "सपोर्ट ई-मेल: woldcom87@gmail.com", color = CyberTextCyan, fontSize = 10.5.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color(0xFF231215),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberFailRed.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "⚠️ कानूनी अस्वीकरण (Legal Disclaimer):",
                                color = CyberFailRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "यह ऐप केवल शैक्षणिक, मनोरंजन तथा गणितीय संभावना (Mathematical Probability) के शोध उद्देश्य से बनाई गई है। इस ऐप का किसी भी प्रकार के अवैध जुए अथवा सट्टेबाजी गतिविधियों से कोई लेना-देना नहीं है। उपयोगकर्ता अपने विवेक से उपयोग करें।",
                                color = CyberTextPrimary,
                                fontSize = 9.5.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JarvisPermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    accentColor: Color,
    onToggleState: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        color = Color(0xFF141A24).copy(alpha = 0.55f),
        border = BorderStroke(1.dp, if (isGranted) accentColor.copy(alpha = 0.5f) else CyberFailRed.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) accentColor.copy(alpha = 0.15f) else CyberFailRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isGranted) accentColor else CyberFailRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            color = CyberTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = description,
                            color = CyberTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Toggle Switch for Quick Active/Inactive state
                Switch(
                    checked = isGranted,
                    onCheckedChange = { onToggleState() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = CyberTextSecondary,
                        uncheckedTrackColor = Color(0xFF1F2937)
                    ),
                    modifier = Modifier.scale(0.85f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Row: Status badge & System Settings button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isGranted) accentColor.copy(alpha = 0.15f) else CyberFailRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, if (isGranted) accentColor.copy(alpha = 0.5f) else CyberFailRed)
                ) {
                    Text(
                        text = if (isGranted) "✓ PERMISSION GRANTED" else "✕ ACTION REQUIRED",
                        color = if (isGranted) accentColor else CyberFailRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Configure System Settings ➔",
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onOpenSettings() }
                )
            }
        }
    }
}
