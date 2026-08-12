package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MarketViewModel
import com.example.ui.theme.*

@Composable
fun JarvisScreen(
    isVoiceOfflineMode: Boolean,
    jarvisVoiceState: String,
    selectedVoiceLang: String,
    offlineLibraries: List<MarketViewModel.OfflineLibraryItem>,
    jarvisMessages: List<MarketViewModel.JarvisMessage>,
    isSendingJarvisCommand: Boolean = false,
    onBackClick: () -> Unit,
    onToggleVoiceOfflineMode: (Boolean) -> Unit,
    onSetJarvisLanguage: (String) -> Unit,
    onSendVoiceCommand: (String, String?, String?, String?, String?) -> Unit,
    onDownloadOfflineLibrary: (String) -> Unit,
    onDownloadAllLibraries: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var showAttachmentDialog by remember { mutableStateOf(false) }
    var attachedName by remember { mutableStateOf<String?>(null) }
    var attachedMime by remember { mutableStateOf<String?>(null) }
    var attachedBase64 by remember { mutableStateOf<String?>(null) }
    var attachedDocText by remember { mutableStateOf<String?>(null) }

    val isKeyboardOpen = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    // Function to process selected attachment URI
    fun processUri(uri: Uri?) {
        if (uri == null) return
        try {
            val contentResolver = context.contentResolver
            var fileName = "Attached_File"
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }

            val mime = contentResolver.getType(uri) ?: "application/octet-stream"
            attachedName = fileName
            attachedMime = mime

            if (mime.startsWith("image/")) {
                val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    attachedBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    attachedDocText = null
                }
            } else {
                val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (!text.isNullOrBlank()) {
                    attachedDocText = if (text.length > 50000) text.take(50000) + "\n...[truncated]" else text
                    attachedBase64 = null
                }
            }
            Toast.makeText(context, "Attached: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error processing file: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> processUri(uri) }

    val docLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> processUri(uri) }

    // Auto-scroll to bottom when new message arrives or when keyboard opens
    LaunchedEffect(jarvisMessages.size, isKeyboardOpen) {
        if (jarvisMessages.isNotEmpty()) {
            listState.animateScrollToItem(jarvisMessages.size - 1)
        }
    }

    // Pulsing animation for the Voice Visualizer Orb
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (jarvisVoiceState != "Idle") 1.25f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(12.dp)
    ) {
        // ==================== 1. TOP HEADER BAR ====================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF141A24).copy(alpha = 0.8f))
                        .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.6f)), CircleShape)
                        .testTag("jarvis_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyberGoldPrimary
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "🤖 AI-JARVIS COMMAND CENTER",
                        color = CyberGoldPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isVoiceOfflineMode) "Offline Neural Engine Active" else "Gemini Cloud AI Unlocked",
                        color = CyberTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Mode Badge
            Surface(
                color = if (isVoiceOfflineMode) Color(0xFF00E5FF).copy(alpha = 0.15f) else CyberNeonGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isVoiceOfflineMode) Color(0xFF00E5FF) else CyberNeonGreen),
                modifier = Modifier.clickable { onToggleVoiceOfflineMode(!isVoiceOfflineMode) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isVoiceOfflineMode) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                        contentDescription = "Mode",
                        tint = if (isVoiceOfflineMode) Color(0xFF00E5FF) else CyberNeonGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVoiceOfflineMode) "OFFLINE AI" else "GEMINI CLOUD",
                        color = if (isVoiceOfflineMode) Color(0xFF00E5FF) else CyberNeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (!isKeyboardOpen) {
            Spacer(modifier = Modifier.height(10.dp))

            // ==================== 2. VOICE MIC & QUICK PROMPT HEADER ====================
            Surface(
                color = Color(0xFF0D1420).copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Voice Orb Small Trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (!isSendingJarvisCommand && jarvisVoiceState == "Idle") {
                                if (textInput.isNotBlank() || attachedName != null) {
                                    val cmd = textInput
                                    val name = attachedName
                                    val mime = attachedMime
                                    val b64 = attachedBase64
                                    val doc = attachedDocText
                                    textInput = ""
                                    attachedName = null
                                    attachedMime = null
                                    attachedBase64 = null
                                    attachedDocText = null
                                    onSendVoiceCommand(cmd, name, mime, b64, doc)
                                } else {
                                    onSendVoiceCommand("Namaste Jarvis!", null, null, null, null)
                                }
                            }
                        }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(if (jarvisVoiceState != "Idle") Color(0xFF00E5FF).copy(alpha = 0.2f) else CyberGoldPrimary.copy(alpha = 0.15f))
                                    .border(BorderStroke(1.dp, if (jarvisVoiceState != "Idle") Color(0xFF00E5FF) else CyberGoldPrimary), CircleShape)
                            )
                            Icon(
                                imageVector = if (jarvisVoiceState != "Idle") Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = if (jarvisVoiceState != "Idle") Color(0xFF00E5FF) else CyberGoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (jarvisVoiceState == "Idle") "TAP MIC TO SPEAK" else jarvisVoiceState.uppercase(),
                                color = if (jarvisVoiceState != "Idle") Color(0xFF00E5FF) else CyberGoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Language: $selectedVoiceLang",
                                color = CyberTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Language Toggle Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Hindi", "English").forEach { langKey ->
                            val isSel = selectedVoiceLang.contains(langKey, ignoreCase = true)
                            FilterChip(
                                selected = isSel,
                                onClick = { onSetJarvisLanguage(if (langKey == "Hindi") "Hindi (हिंदी)" else "English (EN)") },
                                label = { Text(langKey, fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberGoldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color(0xFF162030),
                                    labelColor = CyberTextSecondary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Prompt Shortcuts Row (Horizontal Scrollable)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("Namaste Jarvis", "What is AI?", "Solve general math / query", "App Features", "Analyze Market (Only if asked)")) { prompt ->
                    Surface(
                        color = Color(0xFF162032),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { onSendVoiceCommand(prompt, null, null, null, null) }
                    ) {
                        Text(
                            text = prompt,
                            color = CyberTextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ==================== 3. DEDICATED FULL-SCREEN CHAT MESSAGES ====================
        Surface(
            color = Color(0xFF070C14),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.35f)),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (jarvisMessages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤖 AI-Jarvis Unlocked!\nAsk any general question, solve tasks, or send documents/photos using + button below.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(jarvisMessages) { msg ->
                        val isUser = msg.sender == "User"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (isUser) CyberGoldPrimary.copy(alpha = 0.18f) else Color(0xFF121B2A),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(0.9.dp, if (isUser) CyberGoldPrimary else Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth(if (isUser) 0.88f else 0.98f)
                                    .testTag("chat_bubble_${msg.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isUser) "🗣️ You" else "🤖 AI-Jarvis",
                                            color = if (isUser) CyberGoldPrimary else Color(0xFF00E5FF),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = msg.timestamp,
                                                color = CyberTextSecondary,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Copy Button on Every Message
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(msg.text))
                                                    Toast.makeText(context, "Response text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .testTag("copy_chat_${msg.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy Text",
                                                    tint = CyberGoldPrimary,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (!msg.attachmentName.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = Color(0xFF0F1926),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.8.dp, CyberGoldOutline.copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (msg.attachmentType == "image") Icons.Default.Image else Icons.Default.Description,
                                                    contentDescription = "Attachment",
                                                    tint = CyberGoldPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = msg.attachmentName,
                                                    color = CyberGoldPrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = msg.text,
                                        color = CyberTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ==================== 4. BOTTOM CHAT INPUT BAR WITH + ATTACHMENT BUTTON ====================
        Column(modifier = Modifier.fillMaxWidth()) {
            // Attachment Preview Chip
            if (attachedName != null) {
                Surface(
                    color = Color(0xFF131D2E),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CyberGoldOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (attachedMime?.startsWith("image/") == true) Icons.Default.Image else Icons.Default.Description,
                                contentDescription = "Attachment",
                                tint = CyberGoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Attached: $attachedName",
                                color = CyberTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = {
                                attachedName = null
                                attachedMime = null
                                attachedBase64 = null
                                attachedDocText = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.Red,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Plus Attachment Button
                IconButton(
                    onClick = { showAttachmentDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF162030))
                        .border(BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.6f)), RoundedCornerShape(12.dp))
                        .testTag("jarvis_add_attachment")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach File",
                        tint = CyberGoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask Jarvis anything in Hindi/English...", fontSize = 11.sp, color = CyberTextSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("jarvis_chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberGoldPrimary,
                        unfocusedBorderColor = CyberGoldOutline.copy(alpha = 0.5f),
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    )
                )

                Button(
                    enabled = !isSendingJarvisCommand && jarvisVoiceState == "Idle" && (textInput.isNotBlank() || attachedName != null),
                    onClick = {
                        if (!isSendingJarvisCommand && jarvisVoiceState == "Idle" && (textInput.isNotBlank() || attachedName != null)) {
                            val cmd = textInput
                            val name = attachedName
                            val mime = attachedMime
                            val b64 = attachedBase64
                            val doc = attachedDocText
                            textInput = ""
                            attachedName = null
                            attachedMime = null
                            attachedBase64 = null
                            attachedDocText = null
                            onSendVoiceCommand(cmd, name, mime, b64, doc)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberGoldPrimary,
                        disabledContainerColor = CyberGoldOutline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("jarvis_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (!isSendingJarvisCommand && jarvisVoiceState == "Idle") Color.Black else CyberTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Attachment Chooser Popup Dialog
    if (showAttachmentDialog) {
        AlertDialog(
            onDismissRequest = { showAttachmentDialog = false },
            containerColor = Color(0xFF0F1724),
            title = {
                Text(
                    text = "📎 Select Attachment Type",
                    color = CyberGoldPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = Color(0xFF1A2436),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAttachmentDialog = false
                                imageLauncher.launch("image/*")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photo",
                                tint = CyberGoldPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📷 Photo / Gallery Image", color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Send screenshot, photo or market chart for AI analysis", color = CyberTextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFF1A2436),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberGoldOutline.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showAttachmentDialog = false
                                docLauncher.launch("*/*")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Document",
                                tint = Color(0xFF00E5FF)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("📄 PDF / Document File", color = CyberTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Send PDF report, text file or document for AI reading", color = CyberTextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAttachmentDialog = false }) {
                    Text("Cancel", color = CyberGoldPrimary)
                }
            }
        )
    }
}
