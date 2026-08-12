package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MarketViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.A23ProTheme
import com.example.utils.PdfReportHelper
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            A23ProTheme {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

                var showProfileDialog by remember { mutableStateOf(false) }
                var showOcrDialog by remember { mutableStateOf(false) }
                var pdfViewerFile by remember { mutableStateOf<File?>(null) }

                val selectedMarket by viewModel.selectedMarket.collectAsStateWithLifecycle()
                val currentPrediction by viewModel.currentPrediction.collectAsStateWithLifecycle()
                val current7DayReport by viewModel.current7DayReport.collectAsStateWithLifecycle()
                val a23NewPrediction by viewModel.a23NewPrediction.collectAsStateWithLifecycle()
                val a23New1Prediction by viewModel.a23New1Prediction.collectAsStateWithLifecycle()
                val currentMarketEntries by viewModel.currentMarketEntries.collectAsStateWithLifecycle()
                val allEntries by viewModel.allEntries.collectAsStateWithLifecycle()
                val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
                val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()

                val selectedWallpaper by viewModel.selectedWallpaper.collectAsStateWithLifecycle()
                val backgroundDim by viewModel.backgroundDim.collectAsStateWithLifecycle()
                val isPinLockEnabled by viewModel.isPinLockEnabled.collectAsStateWithLifecycle()
                val currentPin by viewModel.securityPin.collectAsStateWithLifecycle()
                val isPinSet by viewModel.isPinSet.collectAsStateWithLifecycle()
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()
                val isSendingJarvisCommand by viewModel.isSendingJarvisCommand.collectAsStateWithLifecycle()

                val userAccountName by viewModel.userAccountName.collectAsStateWithLifecycle()
                val userAccountMobile by viewModel.userAccountMobile.collectAsStateWithLifecycle()
                val userAccountEmail by viewModel.userAccountEmail.collectAsStateWithLifecycle()
                val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()

                var showNotificationsDialog by remember { mutableStateOf(false) }
                var showPinSetupDialog by remember { mutableStateOf(false) }

                val isJarvisEnabled by viewModel.isJarvisEnabled.collectAsStateWithLifecycle()
                val isBackgroundMicGranted by viewModel.isBackgroundMicGranted.collectAsStateWithLifecycle()
                val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted.collectAsStateWithLifecycle()
                val isAccessibilityServiceEnabled by viewModel.isAccessibilityServiceEnabled.collectAsStateWithLifecycle()

                val apiKeys by viewModel.apiKeys.collectAsStateWithLifecycle()
                val isVoiceOfflineMode by viewModel.isVoiceOfflineMode.collectAsStateWithLifecycle()
                val isForegroundServiceActive by viewModel.isForegroundServiceActive.collectAsStateWithLifecycle()
                val ocrLastResult by viewModel.ocrLastResult.collectAsStateWithLifecycle()
                val isSelfLearningEnabled by viewModel.isSelfLearningEnabled.collectAsStateWithLifecycle()
                val learnedPatternsCount by viewModel.learnedPatternsCount.collectAsStateWithLifecycle()
                val isAes256Encrypted by viewModel.isAes256Encrypted.collectAsStateWithLifecycle()
                val lastIngestedReport by viewModel.lastIngestedReport.collectAsStateWithLifecycle()

                val offlineLibraries by viewModel.offlineLibraries.collectAsStateWithLifecycle()
                val jarvisMessages by viewModel.jarvisMessages.collectAsStateWithLifecycle()
                val jarvisVoiceState by viewModel.jarvisVoiceState.collectAsStateWithLifecycle()
                val selectedVoiceLang by viewModel.selectedVoiceLang.collectAsStateWithLifecycle()

                var currentRoute by remember { mutableStateOf(NavTab.Home.route) }

                // Toast message handler
                LaunchedEffect(syncMessage) {
                    syncMessage?.let { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                        viewModel.clearSyncMessage()
                    }
                }

                fun generateAndOpenPdf() {
                    val file = PdfReportHelper.generateAndOpenMarketPdf(
                        context = context,
                        marketName = selectedMarket,
                        prediction = currentPrediction,
                        sevenDayReports = current7DayReport,
                        marketEntries = currentMarketEntries
                    )
                    if (file != null) {
                        pdfViewerFile = file
                    }
                }

                CyberBackground(
                    wallpaperName = selectedWallpaper,
                    dimLevel = backgroundDim
                ) {
                    if (isPinLockEnabled && !isUnlocked && isLoggedIn) {
                        PinLockOverlay(
                            isPinSet = isPinSet,
                            onSetPin = { pin -> viewModel.setupSecurityPin(pin) },
                            onAttemptUnlock = { pin -> viewModel.attemptUnlock(pin) }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = true,
                            drawerContent = {
                                SideMenuDrawerContent(
                                    userName = userAccountName,
                                    userMobile = userAccountMobile,
                                    onNavigate = { route -> currentRoute = route },
                                    onOpenPdfReport = { generateAndOpenPdf() },
                                    onOpenOcrScanner = { showOcrDialog = true },
                                    onOpenProfileDialog = { showProfileDialog = true },
                                    onCloseDrawer = { coroutineScope.launch { drawerState.close() } }
                                )
                            }
                        ) {
                            Scaffold(
                                containerColor = Color.Transparent,
                                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                                topBar = {
                                    if (currentRoute == NavTab.Home.route) {
                                        A23TopBar(
                                            selectedMarket = selectedMarket,
                                            availableMarkets = viewModel.availableMarkets,
                                            onMarketSelect = { market -> viewModel.selectMarket(market) },
                                            onMenuClick = {
                                                coroutineScope.launch {
                                                    if (drawerState.isClosed) {
                                                        drawerState.open()
                                                    }
                                                }
                                            },
                                            onProfileClick = { showProfileDialog = true },
                                            onJarvisClick = { currentRoute = NavTab.Jarvis.route },
                                            onNotificationClick = { showNotificationsDialog = true }
                                        )
                                    }
                                },
                                bottomBar = {
                                    if (currentRoute != NavTab.Jarvis.route) {
                                        BottomNavBar(
                                            currentRoute = currentRoute,
                                            onTabSelected = { route -> currentRoute = route }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    when (currentRoute) {
                                        NavTab.Home.route -> {
                                            HomeScreen(
                                                selectedMarket = selectedMarket,
                                                availableMarkets = viewModel.availableMarkets,
                                                allEntries = allEntries,
                                                prediction = currentPrediction,
                                                sevenDayReport = current7DayReport,
                                                isSyncing = isSyncing,
                                                userName = userAccountName,
                                                onSelectMarket = { market -> viewModel.selectMarket(market) },
                                                onDownloadClick = { viewModel.syncMarketData() },
                                                onViewFullReportClick = { currentRoute = NavTab.Report.route }
                                            )
                                        }
                                        NavTab.Report.route -> {
                                            SevenDayReportScreen(
                                                selectedMarket = selectedMarket,
                                                availableMarkets = viewModel.availableMarkets,
                                                allEntries = allEntries,
                                                onSelectMarket = { market -> viewModel.selectMarket(market) },
                                                sevenDayReports = current7DayReport,
                                                a23NewPrediction = a23NewPrediction,
                                                a23New1Prediction = a23New1Prediction
                                            )
                                        }
                                        NavTab.DataMarket.route -> {
                                            DataMarketScreen(
                                                selectedMarket = selectedMarket,
                                                availableMarkets = viewModel.availableMarkets,
                                                entries = currentMarketEntries,
                                                onSelectMarket = { market -> viewModel.selectMarket(market) },
                                                onSaveEntry = { market, date, result, isHoliday, entryId ->
                                                    viewModel.saveOrUpdateEntry(market, date, result, isHoliday, entryId)
                                                },
                                                onDeleteEntry = { id -> viewModel.deleteEntry(id) },
                                                onSyncDataFromUrl = { viewModel.syncGithubPortalData(context) },
                                                onClearAndResetData = { viewModel.resetAndSyncFreshData(context) }
                                            )
                                        }
                                        NavTab.History.route -> {
                                            HistoryScreen(
                                                selectedMarket = selectedMarket,
                                                allEntries = allEntries
                                            )
                                        }
                                        NavTab.Settings.route -> {
                                            SettingsScreen(
                                                selectedWallpaper = selectedWallpaper,
                                                backgroundDim = backgroundDim,
                                                isPinLockEnabled = isPinLockEnabled,
                                                currentPin = currentPin,
                                                userAccountName = userAccountName,
                                                userAccountMobile = userAccountMobile,
                                                userAccountEmail = userAccountEmail,
                                                notificationsEnabled = notificationsEnabled,
                                                isJarvisEnabled = isJarvisEnabled,
                                                isBackgroundMicGranted = isBackgroundMicGranted,
                                                isOverlayPermissionGranted = isOverlayPermissionGranted,
                                                isAccessibilityServiceEnabled = isAccessibilityServiceEnabled,
                                                allEntries = allEntries,
                                                onWallpaperSelect = { wp -> viewModel.setWallpaper(wp) },
                                                onBackgroundDimChange = { dim -> viewModel.setBackgroundDim(dim) },
                                                onTogglePinLock = { enabled -> viewModel.togglePinLock(enabled) },
                                                onUpdatePin = { pin -> viewModel.updatePin(pin) },
                                                onToggleNotifications = { enabled -> viewModel.toggleNotifications(enabled) },
                                                onToggleJarvis = { enabled -> viewModel.toggleJarvis(enabled) },
                                                onToggleBackgroundMic = { enabled -> viewModel.toggleBackgroundMic(enabled) },
                                                onToggleOverlayPermission = { enabled -> viewModel.toggleOverlayPermission(enabled) },
                                                onToggleAccessibilityService = { enabled -> viewModel.toggleAccessibilityService(enabled) },
                                                apiKeys = apiKeys,
                                                isVoiceOfflineMode = isVoiceOfflineMode,
                                                isForegroundServiceActive = isForegroundServiceActive,
                                                ocrLastResult = ocrLastResult,
                                                isSelfLearningEnabled = isSelfLearningEnabled,
                                                learnedPatternsCount = learnedPatternsCount,
                                                isAes256Encrypted = isAes256Encrypted,
                                                lastIngestedReport = lastIngestedReport,
                                                onAddApiKey = { name, provider, key, baseUrl, model -> viewModel.addApiKey(name, provider, key, baseUrl, model) },
                                                onDeleteApiKey = { id -> viewModel.deleteApiKey(id) },
                                                onToggleApiKeyActive = { id -> viewModel.toggleApiKeyActive(id) },
                                                onTestApiKey = { id -> viewModel.testApiKey(id) },
                                                onToggleVoiceOfflineMode = { offline -> viewModel.toggleVoiceOfflineMode(offline) },
                                                onToggleForegroundService = { active -> viewModel.toggleForegroundService(active) },
                                                onProcessOcrScanResult = { text -> viewModel.processOcrScanResult(text) },
                                                onToggleSelfLearning = { enabled -> viewModel.toggleSelfLearning(enabled) },
                                                onToggleAesEncryption = { enabled -> viewModel.toggleAesEncryption(enabled) },
                                                onIngestMarketDocument = { filename -> viewModel.ingestMarketDocument(filename) }
                                            )
                                        }
                                        NavTab.Jarvis.route -> {
                                            JarvisScreen(
                                                isVoiceOfflineMode = isVoiceOfflineMode,
                                                jarvisVoiceState = jarvisVoiceState,
                                                selectedVoiceLang = selectedVoiceLang,
                                                offlineLibraries = offlineLibraries,
                                                jarvisMessages = jarvisMessages,
                                                isSendingJarvisCommand = isSendingJarvisCommand,
                                                onBackClick = { currentRoute = NavTab.Home.route },
                                                onToggleVoiceOfflineMode = { offline -> viewModel.toggleVoiceOfflineMode(offline) },
                                                onSetJarvisLanguage = { lang -> viewModel.setJarvisLanguage(lang) },
                                                onSendVoiceCommand = { cmd, name, mime, b64, docText -> viewModel.sendJarvisVoiceCommand(cmd, name, mime, b64, docText) },
                                                onDownloadOfflineLibrary = { id -> viewModel.downloadOfflineLibrary(id) },
                                                onDownloadAllLibraries = { viewModel.downloadAllOfflineLibraries() }
                                            )
                                        }
                                    }

                                    // User Profile Dialog Overlay
                                    if (showProfileDialog) {
                                        UserProfileDialog(
                                            userAccountName = userAccountName,
                                            userAccountMobile = userAccountMobile,
                                            userAccountEmail = userAccountEmail,
                                            onUpdateAccountDetails = { name, mobile, email ->
                                                viewModel.updateAccountDetails(name, mobile, email)
                                            },
                                            onChangePinRequested = { showPinSetupDialog = true },
                                            onLogout = { viewModel.performLogout() },
                                            onDismiss = { showProfileDialog = false }
                                        )
                                    }

                                    // Advance Notifications Overlay
                                    if (showNotificationsDialog) {
                                        NotificationOverlayDialog(
                                            onDismiss = { showNotificationsDialog = false }
                                        )
                                    }

                                    // Manual PIN Setup/Change Overlay
                                    if (showPinSetupDialog) {
                                        PinLockOverlay(
                                            isPinSet = false,
                                            onSetPin = { newPin ->
                                                viewModel.setupSecurityPin(newPin)
                                                showPinSetupDialog = false
                                            },
                                            onAttemptUnlock = { true }
                                        )
                                    }

                                    // Main Authentication Screen Overlay (for initial installation / logout)
                                    if (!isLoggedIn) {
                                        AuthScreen(
                                            onLoginSuccess = { name, email, mobile ->
                                                viewModel.performLogin(name, email, mobile)
                                            }
                                        )
                                    }

                                    // OCR Scanner Dialog Overlay
                                    if (showOcrDialog) {
                                        OcrScannerDialog(
                                            onDismiss = { showOcrDialog = false },
                                            onProcessOcrText = { text -> viewModel.processOcrScanResult(text) },
                                            onSendToJarvis = { text ->
                                                currentRoute = NavTab.Jarvis.route
                                                viewModel.sendJarvisVoiceCommand(text)
                                            }
                                        )
                                    }

                                    // In-App PDF Viewer Dialog
                                    pdfViewerFile?.let { file ->
                                        PdfViewerDialog(
                                            pdfFile = file,
                                            onDismiss = { pdfViewerFile = null }
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
}
