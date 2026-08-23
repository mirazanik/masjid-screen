package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.admin.ui.component.QrScannerDialog
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminUiState
import com.mirazanik.masjidscreen.data.model.MosqueConfig
import com.mirazanik.masjidscreen.data.model.ScreenConfig
import com.mirazanik.masjidscreen.data.model.ScreenGroup
import com.mirazanik.masjidscreen.data.model.ScreenInfo
import com.mirazanik.masjidscreen.data.model.ScreenLayout
import com.mirazanik.masjidscreen.ui.component.DisplayEditActions
import com.mirazanik.masjidscreen.ui.component.ScaledDisplayCanvas
import com.mirazanik.masjidscreen.ui.component.toPreviewSpec
import com.mirazanik.masjidscreen.ui.theme.AppTheme
import com.mirazanik.masjidscreen.data.model.PrayerTimesData
import com.mirazanik.masjidscreen.prayer.PrayerCalculator
import com.mirazanik.masjidscreen.util.TimePreview
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Composable
fun ScreensScreen(
    state: AdminUiState,
    adminVm: AdminPanelViewModel,
    onManagingChanged: (Boolean) -> Unit = {},
    onRequestLandscape: (Boolean) -> Unit = {}
) {
    var managingScreen by remember { mutableStateOf<ScreenInfo?>(null) }

    LaunchedEffect(state.screens, managingScreen) {
        managingScreen?.let { current ->
            managingScreen = state.screens.find { it.screenId == current.screenId } ?: managingScreen
        }
    }

    DisposableEffect(managingScreen != null) {
        val open = managingScreen != null
        onManagingChanged(open)
        onDispose { if (open) onManagingChanged(false) }
    }

    if (managingScreen != null) {
        BackHandler {
            managingScreen = null
            adminVm.deselectScreen()
        }
        ScreenManageView(
            screen = managingScreen!!,
            state = state,
            adminVm = adminVm,
            onRequestLandscape = onRequestLandscape,
            onBack = {
                managingScreen = null
                adminVm.deselectScreen()
            }
        )
    } else {
        ScreenListView(
            screens = state.screens,
            screenSaving = state.screenSaving,
            newScreenPairingCode = state.newScreenPairingCode,
            onCreateScreen = { name -> adminVm.createScreen(name) },
            onCreateScreenFromQr = { name, deviceId -> adminVm.createScreen(name, deviceId) },
            onBindDevice = adminVm::bindDeviceToScreen,
            onDeleteScreen = adminVm::deleteScreen,
            onUnbindScreen = adminVm::unbindScreen,
            onSelectScreen = { screen ->
                adminVm.selectScreen(screen.screenId)
                managingScreen = screen
            },
            onClearPairingCode = adminVm::clearNewScreenPairingCode,
            onRefreshOnline = adminVm::refreshScreens,
        )
    }
}

// â”€â”€ Screen list view â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

@Composable
private fun ScreenListView(
    screens: List<ScreenInfo>,
    screenSaving: Boolean,
    newScreenPairingCode: String?,
    onCreateScreen: (String) -> Unit,
    onCreateScreenFromQr: (String, String) -> Unit,
    onBindDevice: (String, String) -> Unit,
    onDeleteScreen: (String) -> Unit,
    onUnbindScreen: (String) -> Unit,
    onSelectScreen: (ScreenInfo) -> Unit,
    onClearPairingCode: () -> Unit,
    onRefreshOnline: () -> Unit,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var scannedDeviceId by remember { mutableStateOf<String?>(null) }
    var deleteConfirmScreen by remember { mutableStateOf<ScreenInfo?>(null) }
    var unbindConfirmScreen by remember { mutableStateOf<ScreenInfo?>(null) }

    val tick by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            kotlinx.coroutines.delay(15_000L)
            value = System.currentTimeMillis()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Check Online header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${screens.count { it.isOnline }} of ${screens.size} online",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                    onClick = onRefreshOnline,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Check Online", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (screens.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No screens registered",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap + to scan a display QR or create a pairing code",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(screens, key = { it.screenId }) { screen ->
                        ScreenCard(
                            screen = screen,
                            tick = tick,
                            onManage = { onSelectScreen(screen) },
                            onUnbind = { unbindConfirmScreen = screen },
                            onDelete = { deleteConfirmScreen = screen }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Screen")
        }
    }

    if (showCreateDialog) {
        CreateScreenDialog(
            isSaving = screenSaving,
            onScanQr = {
                showCreateDialog = false
                showQrScanner = true
            },
            onConfirm = { name ->
                onCreateScreen(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showQrScanner) {
        QrScannerDialog(
            onDeviceIdScanned = { deviceId ->
                showQrScanner = false
                scannedDeviceId = deviceId
            },
            onDismiss = { showQrScanner = false }
        )
    }

    scannedDeviceId?.let { deviceId ->
        ScannedDeviceDialog(
            unpairedScreens = screens.filter { !it.isPaired },
            isSaving = screenSaving,
            onCreate = { name ->
                onCreateScreenFromQr(name, deviceId)
                scannedDeviceId = null
            },
            onBind = { screenId ->
                onBindDevice(screenId, deviceId)
                scannedDeviceId = null
            },
            onDismiss = { scannedDeviceId = null }
        )
    }

    newScreenPairingCode?.let { code ->
        PairingCodeDialog(code = code, onDismiss = onClearPairingCode)
    }

    deleteConfirmScreen?.let { screen ->
        AlertDialog(
            onDismissRequest = { deleteConfirmScreen = null },
            title = { Text("Delete Screen") },
            text = { Text("Delete \"${screen.name}\"? All its hadiths, notices and settings will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = { onDeleteScreen(screen.screenId); deleteConfirmScreen = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmScreen = null }) { Text("Cancel") }
            }
        )
    }

    unbindConfirmScreen?.let { screen ->
        AlertDialog(
            onDismissRequest = { unbindConfirmScreen = null },
            icon = { Icon(Icons.Default.LinkOff, null) },
            title = { Text("Unbind Device") },
            text = { Text("Disconnect the paired device from \"${screen.name}\"? A new pairing code will be generated.") },
            confirmButton = {
                TextButton(
                    onClick = { onUnbindScreen(screen.screenId); unbindConfirmScreen = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Unbind") }
            },
            dismissButton = {
                TextButton(onClick = { unbindConfirmScreen = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ScreenCard(
    screen: ScreenInfo,
    tick: Long,
    onManage: () -> Unit,
    onUnbind: () -> Unit,
    onDelete: () -> Unit,
) {
    val onlineColor = Color(0xFF4CAF50)
    val offlineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

    val lastSeenText = remember(screen.lastSeenMs, tick) {
        if (screen.lastSeenMs == 0L) "Never connected"
        else {
            val diffMs = tick - screen.lastSeenMs
            val mins = TimeUnit.MILLISECONDS.toMinutes(diffMs)
            when {
                mins < 5 -> "Online now"
                mins < 60 -> "$mins min ago"
                else -> "${TimeUnit.MILLISECONDS.toHours(diffMs)}h ago"
            }
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = if (screen.isOnline) onlineColor else offlineColor
            ) {}

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = screen.name.ifBlank { "Unnamed Screen" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lastSeenText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!screen.isPaired) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Code: ${screen.pairingCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (screen.appVersion.isNotBlank()) {
                    Text(
                        text = "v${screen.appVersion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            if (screen.isPaired) {
                IconButton(onClick = onUnbind) {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = "Unbind device",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onManage) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Manage",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun CreateScreenDialog(
    isSaving: Boolean,
    onScanQr: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Screen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onScanQr,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Scan QR on display")
                }
                Text(
                    "Point your camera at the QR code shown on the TV.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Text(
                    "Or create a pairing code",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Screen Name") },
                    placeholder = { Text("e.g. Main Hall") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create code")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ScannedDeviceDialog(
    unpairedScreens: List<ScreenInfo>,
    isSaving: Boolean,
    onCreate: (String) -> Unit,
    onBind: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
        title = { Text("Display found") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Name this screen to add it, or connect it to an existing unpaired slot.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Screen Name") },
                    placeholder = { Text("e.g. Main Hall") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (unpairedScreens.isNotEmpty()) {
                    Text(
                        "Or connect to an existing screen",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    unpairedScreens.forEach { screen ->
                        OutlinedButton(
                            onClick = { onBind(screen.screenId) },
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(screen.name.ifBlank { "Unnamed Screen" })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onCreate(name.trim()) },
                enabled = name.isNotBlank() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Add Screen")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun PairingCodeDialog(code: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Screen Created") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Enter this code on the display, or scan the display QR from Add Screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = code,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = MaterialTheme.typography.displaySmall.letterSpacing
                )
                Text(
                    "The code is also shown on each unconnected screen card.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

// â”€â”€ Screen manage view â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

private val manageTabs = listOf("Show", "Jamaat", "Hadith", "Notices", "Settings")
private val manageTabIcons = listOf(
    Icons.Default.Tv,
    Icons.Default.AccessTime,
    Icons.AutoMirrored.Filled.MenuBook,
    Icons.Default.Campaign,
    Icons.Default.Settings,
)

private enum class ScreenEditor {
    Jamaat, Hadith, Notice, Settings, GlobalSettings
}

@Composable
private fun ScreenManageView(
    screen: ScreenInfo,
    state: AdminUiState,
    adminVm: AdminPanelViewModel,
    onBack: () -> Unit,
    onRequestLandscape: (Boolean) -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var liveEdit by remember { mutableStateOf(false) }
    var editor by remember { mutableStateOf<ScreenEditor?>(null) }
    var syncDialogAction by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }
    var timePreview by remember { mutableStateOf<TimePreview?>(null) }
    val jamaatPrayerTimes = remember(timePreview, state.screenPrayerTimes, state.screenConfig, state.config) {
        prayerTimesForPreview(state, timePreview)
    }
    val previewDate = timePreview?.now()?.toLocalDate()

    val group = state.currentScreenGroup
    val isGrouped = group != null && group.screenIds.size > 1

    fun requestSave(action: (Boolean) -> Unit) {
        if (isGrouped) syncDialogAction = action else action(false)
    }

    val showDevicePreview = selectedTab == 0 && editor == null && !liveEdit
    val liveEditPreview = liveEdit && editor == null

    LaunchedEffect(showDevicePreview, liveEditPreview) {
        onRequestLandscape(showDevicePreview || liveEditPreview)
    }
    DisposableEffect(Unit) {
        onDispose { onRequestLandscape(false) }
    }

    if (editor != null) {
        BackHandler { editor = null }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            liveEditPreview -> LiveEditView(
                screen = screen,
                state = state,
                onJamaat = { editor = ScreenEditor.Jamaat },
                onHadith = { editor = ScreenEditor.Hadith },
                onNotice = { editor = ScreenEditor.Notice },
                onHeader = { editor = ScreenEditor.Settings },
                onSaveLayout = { layout ->
                    adminVm.saveScreenConfig(state.screenConfig.copy(layout = layout), applyToGroup = false)
                },
                onClose = { liveEdit = false },
                timePreview = timePreview,
                onTimePreviewChange = { timePreview = it },
            )
            showDevicePreview -> ShowDisplayView(
                screen = screen,
                state = state,
                onBack = onBack,
                onLiveEdit = {
                    editor = null
                    liveEdit = true
                },
                onSelectTab = { selectedTab = it },
                timePreview = timePreview,
                onTimePreviewChange = { timePreview = it },
            )
            else -> Column(modifier = Modifier.fillMaxSize()) {
                ScreenManageTopBar(
                    screen = screen,
                    isGrouped = isGrouped,
                    groupName = group?.name,
                    onBack = {
                        if (liveEdit && editor != null) {
                            editor = null
                        } else {
                            onBack()
                        }
                    },
                    onLiveEdit = {
                        editor = null
                        liveEdit = true
                    },
                )

                if (!(liveEdit && editor != null)) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 8.dp
                    ) {
                        manageTabs.forEachIndexed { index, label ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    liveEdit = false
                                    editor = null
                                    selectedTab = index
                                },
                                text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                icon = { Icon(manageTabIcons[index], contentDescription = null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    val activeEditor = editor
                    if (activeEditor != null) {
                        ScreenEditorContent(
                            editor = activeEditor,
                            state = state,
                            adminVm = adminVm,
                            requestSave = ::requestSave,
                            prayerTimes = jamaatPrayerTimes,
                            waqtDate = previewDate,
                        )
                    } else {
                        when (selectedTab) {
                            1 -> JamaatScreen(
                                initial = state.screenJamaat,
                                prayerTimes = jamaatPrayerTimes,
                                isSaving = state.screenSaving,
                                waqtDate = previewDate,
                                onSave = { times -> requestSave { applyToGroup -> adminVm.saveScreenJamaat(times, applyToGroup) } }
                            )
                            2 -> HadithScreen(
                                hadiths = state.screenHadiths,
                                hadithInterval = state.screenConfig.hadithInterval,
                                onAdd = { t, s, n -> requestSave { applyToGroup -> adminVm.addScreenHadith(t, s, n, applyToGroup) } },
                                onUpdate = { id, t, s, n, active -> requestSave { applyToGroup -> adminVm.updateScreenHadith(id, t, s, n, active, applyToGroup) } },
                                onToggleActive = adminVm::toggleScreenHadithActive,
                                onDelete = { id -> requestSave { applyToGroup -> adminVm.deleteScreenHadith(id, applyToGroup) } },
                            ) { interval -> requestSave { applyToGroup -> adminVm.saveScreenConfig(state.screenConfig.copy(hadithInterval = interval), applyToGroup) } }
                            3 -> NoticeScreen(
                                notices = state.screenNotices,
                                onAdd = { text, priority -> requestSave { applyToGroup -> adminVm.addScreenNotice(text, priority, applyToGroup) } },
                                onUpdate = { id, text, active, priority -> requestSave { applyToGroup -> adminVm.updateScreenNotice(id, text, active, priority, applyToGroup) } },
                                onDelete = { id -> requestSave { applyToGroup -> adminVm.deleteScreenNotice(id, applyToGroup) } }
                            )
                            4 -> ScreenSettingsTab(
                                config = state.screenConfig,
                                globalConfig = state.config,
                                isSaving = state.screenSaving || state.isSaving,
                                onSaveConfig = { config -> requestSave { applyToGroup -> adminVm.saveScreenConfig(config, applyToGroup) } },
                            )
                        }
                    }
                }
            }
        }
    }

    syncDialogAction?.let { action ->
        SyncScopeDialog(
            group = group!!,
            onThisOnly = { action(false); syncDialogAction = null },
            onAllScreens = { action(true); syncDialogAction = null },
            onDismiss = { syncDialogAction = null }
        )
    }
}

@Composable
private fun ScreenEditorContent(
    editor: ScreenEditor,
    state: AdminUiState,
    adminVm: AdminPanelViewModel,
    requestSave: ((Boolean) -> Unit) -> Unit,
    prayerTimes: PrayerTimesData = state.screenPrayerTimes,
    waqtDate: LocalDate? = null,
) {
    when (editor) {
        ScreenEditor.Jamaat -> JamaatScreen(
            initial = state.screenJamaat,
            prayerTimes = prayerTimes,
            isSaving = state.screenSaving,
            waqtDate = waqtDate,
            onSave = { times -> requestSave { applyToGroup -> adminVm.saveScreenJamaat(times, applyToGroup) } }
        )
        ScreenEditor.Hadith -> HadithScreen(
            hadiths = state.screenHadiths,
            hadithInterval = state.screenConfig.hadithInterval,
            onAdd = { t, s, n -> requestSave { applyToGroup -> adminVm.addScreenHadith(t, s, n, applyToGroup) } },
            onUpdate = { id, t, s, n, active -> requestSave { applyToGroup -> adminVm.updateScreenHadith(id, t, s, n, active, applyToGroup) } },
            onToggleActive = adminVm::toggleScreenHadithActive,
            onDelete = { id -> requestSave { applyToGroup -> adminVm.deleteScreenHadith(id, applyToGroup) } },
        ) { interval -> requestSave { applyToGroup -> adminVm.saveScreenConfig(state.screenConfig.copy(hadithInterval = interval), applyToGroup) } }
        ScreenEditor.Notice -> NoticeScreen(
            notices = state.screenNotices,
            onAdd = { text, priority -> requestSave { applyToGroup -> adminVm.addScreenNotice(text, priority, applyToGroup) } },
            onUpdate = { id, text, active, priority -> requestSave { applyToGroup -> adminVm.updateScreenNotice(id, text, active, priority, applyToGroup) } },
            onDelete = { id -> requestSave { applyToGroup -> adminVm.deleteScreenNotice(id, applyToGroup) } }
        )
        ScreenEditor.Settings -> ScreenSettingsTab(
            config = state.screenConfig,
            globalConfig = state.config,
            isSaving = state.screenSaving || state.isSaving,
            onSaveConfig = { config -> requestSave { applyToGroup -> adminVm.saveScreenConfig(config, applyToGroup) } },
        )
        ScreenEditor.GlobalSettings -> GlobalSettingsScreen(
            config = state.config,
            isSaving = state.isSaving,
            onSave = adminVm::saveConfig
        )
    }
}

@Composable
private fun DeviceScreenPreview(
    screen: ScreenInfo,
    state: AdminUiState,
    editActions: DisplayEditActions? = null,
    layoutOverride: ScreenLayout? = null,
    timePreview: TimePreview? = null,
    modifier: Modifier = Modifier,
) {
    val spec = screen.toPreviewSpec()
    ScaledDisplayCanvas(
        spec = spec,
        modifier = modifier.fillMaxSize()
    ) {
        ScreenDisplayPreview(
            screen = screen,
            state = state,
            editActions = editActions,
            layoutOverride = layoutOverride,
            timePreview = timePreview,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDisplayView(
    screen: ScreenInfo,
    state: AdminUiState,
    onBack: () -> Unit,
    onLiveEdit: () -> Unit,
    onSelectTab: (Int) -> Unit,
    timePreview: TimePreview?,
    onTimePreviewChange: (TimePreview?) -> Unit,
) {
    var chromeVisible by remember { mutableStateOf(true) }
    BackHandler(onBack = {
        if (!chromeVisible) chromeVisible = true else onBack()
    })
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        DeviceScreenPreview(
            screen = screen,
            state = state,
            timePreview = timePreview,
        )
        if (chromeVisible) {
            FloatingLiveEditButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(50))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                manageTabs.forEachIndexed { index, label ->
                    FilterChip(
                        selected = index == 0,
                        onClick = { if (index != 0) onSelectTab(index) },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(manageTabIcons[index], contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingLiveEditButton(onClick = { chromeVisible = false }) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = "Hide controls", tint = Color.White)
                }
                FilledTonalButton(
                    onClick = onLiveEdit,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Live Edit", style = MaterialTheme.typography.labelMedium)
                }
            }
            AdminTimePreviewBar(
                state = state,
                preview = timePreview,
                onPreviewChange = onTimePreviewChange,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { chromeVisible = true }
            )
        }
    }
}

@Composable
private fun LiveEditView(
    screen: ScreenInfo,
    state: AdminUiState,
    onJamaat: () -> Unit,
    onHadith: () -> Unit,
    onNotice: () -> Unit,
    onHeader: () -> Unit,
    onSaveLayout: (ScreenLayout) -> Unit,
    onClose: () -> Unit,
    timePreview: TimePreview? = null,
    onTimePreviewChange: (TimePreview?) -> Unit = {},
) {
    var layout by remember { mutableStateOf(state.screenConfig.layout.coerced()) }
    var dragging by remember { mutableStateOf(false) }
    var timeBarVisible by remember { mutableStateOf(true) }
    val latestSave = rememberUpdatedState(onSaveLayout)

    LaunchedEffect(state.screenConfig.layout) {
        if (!dragging) layout = state.screenConfig.layout.coerced()
    }

    val editActions = DisplayEditActions(
        onJamaatClick = onJamaat,
        onHadithClick = onHadith,
        onNoticeClick = onNotice,
        onHeaderClick = onHeader,
        onLayoutChange = { next ->
            dragging = true
            layout = next.coerced()
        },
        onLayoutCommit = { next ->
            val committed = next.coerced()
            layout = committed
            dragging = false
            latestSave.value(committed)
        },
    )

    BackHandler(onBack = onClose)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        DeviceScreenPreview(
            screen = screen,
            state = state,
            editActions = editActions,
            layoutOverride = layout,
            timePreview = timePreview,
        )
        FloatingLiveEditButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingLiveEditButton(
                onClick = {
                    val reset = ScreenLayout()
                    layout = reset
                    dragging = false
                    latestSave.value(reset)
                }
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset layout", tint = Color.White)
            }
            FloatingLiveEditButton(
                onClick = { timeBarVisible = !timeBarVisible },
            ) {
                Icon(
                    if (timeBarVisible) Icons.Default.VisibilityOff else Icons.Default.Schedule,
                    contentDescription = if (timeBarVisible) "Hide test time" else "Show test time",
                    tint = Color.White
                )
            }
            FloatingLiveEditButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close live edit", tint = Color.White)
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = run {
                    val spec = screen.toPreviewSpec()
                    if (spec.fromDevice) {
                        "Matching TV ${spec.widthPx}×${spec.heightPx}. Drag gold bars to resize. Tap a region to edit content."
                    } else {
                        "Waiting for the TV to send its size — open the display app, then return here."
                    }
                },
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelSmall
            )
            if (timeBarVisible) {
                AdminTimePreviewBar(
                    state = state,
                    preview = timePreview,
                    onPreviewChange = onTimePreviewChange,
                    onHide = { timeBarVisible = false },
                )
            } else {
                FilledTonalButton(
                    onClick = { timeBarVisible = true },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (timePreview != null) "Show test time" else "Test time")
                }
            }
        }
    }
}

@Composable
private fun AdminTimePreviewBar(
    state: AdminUiState,
    preview: TimePreview?,
    onPreviewChange: (TimePreview?) -> Unit,
    modifier: Modifier = Modifier,
    onHide: (() -> Unit)? = null,
) {
    TimePreviewBar(
        mosqueConfig = mergeScreenMosqueConfig(state.screenConfig, state.config),
        jamaat = state.screenJamaat,
        preview = preview,
        onPreviewChange = onPreviewChange,
        modifier = modifier,
        onHide = onHide,
    )
}

private fun prayerTimesForPreview(state: AdminUiState, preview: TimePreview?): PrayerTimesData {
    if (preview == null) return state.screenPrayerTimes
    val config = mergeScreenMosqueConfig(state.screenConfig, state.config)
    return PrayerCalculator.calculate(
        config.latitude,
        config.longitude,
        config.calculationMethod,
        config.madhab,
        preview.now().toLocalDate()
    )
}

@Composable
private fun FloatingLiveEditButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
    ) {
        content()
    }
}

@Composable
private fun ScreenManageTopBar(
    screen: ScreenInfo,
    isGrouped: Boolean,
    groupName: String?,
    onBack: () -> Unit,
    onLiveEdit: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                screen.name.ifBlank { "Unnamed Screen" },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (screen.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (screen.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isGrouped && groupName != null) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.Link,
                                null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                groupName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
        FilledTonalButton(
            onClick = onLiveEdit,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Live Edit", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun SyncScopeDialog(
    group: ScreenGroup,
    onThisOnly: () -> Unit,
    onAllScreens: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Link, null) },
        title = { Text("Apply to linked screens?") },
        text = {
            Text("This screen is in group \"${group.name}\" with ${group.screenIds.size} linked screens.")
        },
        confirmButton = {
            Button(onClick = onAllScreens) {
                Text("All ${group.screenIds.size} screens")
            }
        },
        dismissButton = {
            TextButton(onClick = onThisOnly) {
                Text("This screen only")
            }
        }
    )
}

// â”€â”€ Per-screen settings tab â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

private val calculationMethodsLocal = listOf(
    "MWL" to "Muslim World League",
    "ISNA" to "ISNA (North America)",
    "EGYPT" to "Egyptian",
    "MAKKAH" to "Umm al-Qura (Makkah)",
    "KARACHI" to "Karachi / Hanafi",
    "MOON_SIGHTING" to "Moon Sighting Committee",
    "DUBAI" to "Dubai",
)

private val languages = listOf("en" to "English", "bn" to "\u09ac\u09be\u0982\u09b2\u09be")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenSettingsTab(
    config: ScreenConfig,
    globalConfig: MosqueConfig,
    isSaving: Boolean,
    onSaveConfig: (ScreenConfig) -> Unit,
) {
    var mosqueName by remember(config) { mutableStateOf(config.mosqueName.ifBlank { globalConfig.name }) }
    var mosqueAddress by remember(config) { mutableStateOf(config.mosqueAddress.ifBlank { globalConfig.address }) }
    var lat by remember(config) { mutableStateOf(if (config.latitude != 0.0) config.latitude.toString() else globalConfig.latitude.toString()) }
    var lng by remember(config) { mutableStateOf(if (config.longitude != 0.0) config.longitude.toString() else globalConfig.longitude.toString()) }
    var method by remember(config) { mutableStateOf(config.calculationMethod.ifBlank { globalConfig.calculationMethod }) }
    var madhab by remember(config) { mutableStateOf(config.madhab.ifBlank { globalConfig.madhab }) }
    var selectedTheme by remember(config) { mutableStateOf(AppTheme.fromKey(config.activeTheme)) }
    var language by remember(config) { mutableStateOf(config.language) }
    var fontScale by remember(config) { mutableFloatStateOf(config.tableFontScale) }
    var layout by remember(config) { mutableStateOf(config.layout.coerced()) }
    var countdownMins by remember(config) { mutableIntStateOf(config.jamaatCountdownMins) }
    var hijriOffset by remember(config) { mutableIntStateOf(config.hijriDateOffset) }
    var methodExpanded by remember { mutableStateOf(false) }
    var madhabExpanded by remember { mutableStateOf(false) }
    var languageExpanded by remember { mutableStateOf(false) }
    var countdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsCard(icon = Icons.Default.Mosque, title = "Masjid Identity") {
            OutlinedTextField(
                value = mosqueName, onValueChange = { mosqueName = it },
                label = { Text("Masjid Name") }, singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = mosqueAddress, onValueChange = { mosqueAddress = it },
                label = { Text("Address") },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        SettingsCard(icon = Icons.Default.MyLocation, title = "Prayer Time Calculation") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lat, onValueChange = { lat = it },
                    label = { Text("Latitude") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f), placeholder = { Text("23.777176") }
                )
                OutlinedTextField(
                    value = lng, onValueChange = { lng = it },
                    label = { Text("Longitude") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f), placeholder = { Text("90.399452") }
                )
            }

            ExposedDropdownMenuBox(expanded = methodExpanded, onExpandedChange = { methodExpanded = it }) {
                OutlinedTextField(
                    value = calculationMethodsLocal.find { it.first == method }?.second ?: method,
                    onValueChange = {}, readOnly = true,
                    label = { Text("Calculation Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(methodExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = methodExpanded, onDismissRequest = { methodExpanded = false }) {
                    calculationMethodsLocal.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { method = key; methodExpanded = false })
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = madhabExpanded, onExpandedChange = { madhabExpanded = it }) {
                OutlinedTextField(
                    value = madhab, onValueChange = {}, readOnly = true,
                    label = { Text("Madhab (Asr calculation)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(madhabExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = madhabExpanded, onDismissRequest = { madhabExpanded = false }) {
                    listOf("HANAFI", "SHAFI").forEach { m ->
                        DropdownMenuItem(text = { Text(m) }, onClick = { madhab = m; madhabExpanded = false })
                    }
                }
            }
        }

        SettingsCard(icon = Icons.Default.Palette, title = "Display Theme") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    ThemeChip(
                        theme = theme,
                        selected = theme == selectedTheme,
                        onClick = { selectedTheme = theme }
                    )
                }
            }
        }

        SettingsCard(icon = Icons.Default.TextFields, title = "Prayer Table Font Size") {
            Text(
                "Scale: ${"%.0f".format(fontScale * 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Slider(
                value = fontScale,
                onValueChange = { fontScale = it },
                valueRange = 0.5f..1.5f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("150%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SettingsCard(icon = Icons.Default.AspectRatio, title = "Display Layout") {
            Text(
                "Layout follows the connected display. You can also drag regions in Live Edit.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LayoutSizeSlider(
                label = "Top bar date",
                value = layout.headerScale,
                valueRange = ScreenLayout.MIN_SCALE..ScreenLayout.MAX_SCALE,
                display = "${"%.0f".format(layout.headerScale * 100)}%",
                onValueChange = { layout = layout.copy(headerScale = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Clock time height",
                value = layout.clockScale,
                valueRange = ScreenLayout.MIN_SCALE..ScreenLayout.MAX_SCALE,
                display = "${"%.0f".format(layout.clockScale * 100)}%",
                onValueChange = { layout = layout.copy(clockScale = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Sunrise / Sehri / Iftar",
                value = layout.sunGridScale,
                valueRange = ScreenLayout.MIN_SCALE..ScreenLayout.MAX_SCALE,
                display = "${"%.0f".format(layout.sunGridScale * 100)}%",
                onValueChange = { layout = layout.copy(sunGridScale = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Clock panel width",
                value = layout.clockWidthFraction,
                valueRange = ScreenLayout.MIN_CLOCK_WIDTH..ScreenLayout.MAX_CLOCK_WIDTH,
                display = "${"%.0f".format(layout.clockWidthFraction * 100)}%",
                onValueChange = { layout = layout.copy(clockWidthFraction = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Prayer table height",
                value = layout.mainRowWeight,
                valueRange = ScreenLayout.MIN_MAIN_ROW..ScreenLayout.MAX_MAIN_ROW,
                display = "${"%.0f".format(layout.mainRowWeight * 100)}%",
                onValueChange = { layout = layout.copy(mainRowWeight = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Notice height",
                value = layout.noticeHeightFraction,
                valueRange = ScreenLayout.MIN_NOTICE..ScreenLayout.MAX_NOTICE,
                display = "${"%.0f".format(layout.noticeHeightFraction * 100)}%",
                onValueChange = { layout = layout.copy(noticeHeightFraction = it).coerced() }
            )
            LayoutSizeSlider(
                label = "Jamaat: Prayer column",
                value = layout.jamaatColPrayer,
                valueRange = ScreenLayout.MIN_JAMAAT_COL..ScreenLayout.MAX_JAMAAT_COL,
                display = "${"%.0f".format(layout.jamaatColPrayer * 100)}%",
                onValueChange = { layout = layout.setJamaatCol(0, it).coerced() }
            )
            LayoutSizeSlider(
                label = "Jamaat: Waqt start column",
                value = layout.jamaatColWaqtStart,
                valueRange = ScreenLayout.MIN_JAMAAT_COL..ScreenLayout.MAX_JAMAAT_COL,
                display = "${"%.0f".format(layout.jamaatColWaqtStart * 100)}%",
                onValueChange = { layout = layout.setJamaatCol(1, it).coerced() }
            )
            LayoutSizeSlider(
                label = "Jamaat: Waqt end column",
                value = layout.jamaatColWaqtEnd,
                valueRange = ScreenLayout.MIN_JAMAAT_COL..ScreenLayout.MAX_JAMAAT_COL,
                display = "${"%.0f".format(layout.jamaatColWaqtEnd * 100)}%",
                onValueChange = { layout = layout.setJamaatCol(2, it).coerced() }
            )
            LayoutSizeSlider(
                label = "Jamaat: Jamaat column",
                value = layout.jamaatColJamaat,
                valueRange = ScreenLayout.MIN_JAMAAT_COL..ScreenLayout.MAX_JAMAAT_COL,
                display = "${"%.0f".format(layout.jamaatColJamaat * 100)}%",
                onValueChange = { layout = layout.setJamaatCol(3, it).coerced() }
            )
            TextButton(onClick = { layout = ScreenLayout() }) {
                Text("Reset layout to default")
            }
        }

        SettingsCard(icon = Icons.Default.Translate, title = "Display Language") {
            ExposedDropdownMenuBox(expanded = languageExpanded, onExpandedChange = { languageExpanded = it }) {
                OutlinedTextField(
                    value = languages.find { it.first == language }?.second ?: language,
                    onValueChange = {}, readOnly = true,
                    label = { Text("Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(languageExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                    languages.forEach { (key, label) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { language = key; languageExpanded = false })
                    }
                }
            }
        }

        SettingsCard(icon = Icons.Default.Timer, title = "Jamaat Countdown Timer") {
            ExposedDropdownMenuBox(expanded = countdownExpanded, onExpandedChange = { countdownExpanded = it }) {
                OutlinedTextField(
                    value = "$countdownMins minutes before Jamaat",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Show countdown timer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(countdownExpanded) },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = countdownExpanded, onDismissRequest = { countdownExpanded = false }) {
                    listOf(1, 2, 3, 5, 10, 15).forEach { mins ->
                        DropdownMenuItem(
                            text = { Text("$mins minutes before Jamaat") },
                            onClick = { countdownMins = mins; countdownExpanded = false }
                        )
                    }
                }
            }
        }

        SettingsCard(icon = Icons.Default.CalendarMonth, title = "Arabic Date Adjustment") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hijri date offset", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        when {
                            hijriOffset > 0 -> "+$hijriOffset day${if (hijriOffset > 1) "s" else ""} from calculated"
                            hijriOffset < 0 -> "$hijriOffset day${if (hijriOffset < -1) "s" else ""} from calculated"
                            else -> "No adjustment (default)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(onClick = { if (hijriOffset > -3) hijriOffset-- }, shape = MaterialTheme.shapes.medium) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    Text(
                        text = if (hijriOffset >= 0) "+$hijriOffset" else "$hijriOffset",
                        modifier = Modifier.widthIn(min = 40.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    FilledTonalIconButton(onClick = { if (hijriOffset < 3) hijriOffset++ }, shape = MaterialTheme.shapes.medium) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
    StickySaveBar(
        label = "Save Settings",
        isSaving = isSaving,
        onClick = {
            onSaveConfig(config.copy(
                mosqueName = mosqueName,
                mosqueAddress = mosqueAddress,
                latitude = lat.toDoubleOrNull() ?: config.latitude,
                longitude = lng.toDoubleOrNull() ?: config.longitude,
                calculationMethod = method,
                madhab = madhab,
                activeTheme = selectedTheme.key,
                language = language,
                tableFontScale = fontScale,
                layout = layout.coerced(),
                jamaatCountdownMins = countdownMins,
                hijriDateOffset = hijriOffset,
            ))
        }
    )
    }
}

@Composable
private fun LayoutSizeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    display: String,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                display,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
