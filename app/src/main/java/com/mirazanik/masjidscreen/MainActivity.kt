package com.mirazanik.masjidscreen

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mirazanik.masjidscreen.admin.ui.AdminTheme
import com.mirazanik.masjidscreen.admin.ui.screen.AdminPanel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminAuthState
import com.mirazanik.masjidscreen.admin.viewmodel.AdminAuthViewModel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel
import com.mirazanik.masjidscreen.ui.screen.DisplayScreen
import com.mirazanik.masjidscreen.ui.screen.PairingScreen
import com.mirazanik.masjidscreen.ui.theme.LocalMosqueColors
import com.mirazanik.masjidscreen.ui.theme.MosqueTheme
import com.mirazanik.masjidscreen.viewmodel.DisplayViewModel
import com.mirazanik.masjidscreen.viewmodel.ScreenPairingState

class MainActivity : ComponentActivity() {

    private val viewModel: DisplayViewModel by viewModels()
    private val adminAuthViewModel: AdminAuthViewModel by viewModels()
    private val adminPanelViewModel: AdminPanelViewModel by viewModels()
    private var showAdmin by mutableStateOf(false)
    private var pendingUnpairExit by mutableStateOf(false)
    private var adminLandscape by mutableStateOf(false)
    private var immersiveEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setImmersiveMode(true)

        setContent {
            val state by viewModel.state.collectAsState()
            val pairingState by viewModel.pairingState.collectAsState()
            val deviceId by viewModel.deviceId.collectAsState()
            val unpairError by viewModel.unpairError.collectAsState()
            val authState by adminAuthViewModel.authState.collectAsState()
            val isAdminLoggedIn = authState.isLoggedInAdmin()
            val deviceUnpaired = pairingState !is ScreenPairingState.Paired &&
                pairingState !is ScreenPairingState.Loading
            val openAdmin = showAdmin || (isAdminLoggedIn && deviceUnpaired)
            val restoringAdminSession = authState is AdminAuthState.Loading && deviceUnpaired

            SideEffect {
                setImmersiveMode(!openAdmin || adminLandscape)
            }

            LaunchedEffect(openAdmin, adminLandscape) {
                requestedOrientation = when {
                    !openAdmin || adminLandscape -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

            LaunchedEffect(pairingState, pendingUnpairExit) {
                if (pendingUnpairExit && pairingState is ScreenPairingState.Unpaired) {
                    pendingUnpairExit = false
                    showAdmin = false
                    adminLandscape = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }
            LaunchedEffect(unpairError) {
                if (unpairError != null) pendingUnpairExit = false
            }

            when {
                openAdmin -> {
                    AdminTheme {
                        AdminPanel(
                            authViewModel = adminAuthViewModel,
                            adminViewModel = adminPanelViewModel,
                            isDevicePaired = pairingState is ScreenPairingState.Paired,
                            unpairError = unpairError,
                            onUnpair = {
                                pendingUnpairExit = true
                                viewModel.unpairFromThisDevice()
                            },
                            onClearUnpairError = viewModel::clearUnpairError,
                            onRequestLandscape = { landscape ->
                                adminLandscape = landscape
                                requestedOrientation = if (landscape) {
                                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                } else {
                                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            },
                            onBack = {
                                showAdmin = false
                                adminLandscape = false
                                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                if (isAdminLoggedIn && pairingState !is ScreenPairingState.Paired) {
                                    finish()
                                }
                            }
                        )
                    }
                }
                pairingState is ScreenPairingState.Paired -> {
                    MosqueTheme(
                        theme = state.activeTheme,
                        bangla = state.mosqueConfig.language == "bn"
                    ) {
                        DisplayScreen(
                            state = state,
                            onEnterAdmin = {
                                showAdmin = true
                                adminLandscape = false
                                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            },
                            onViewportMeasured = viewModel::reportDisplaySize
                        )
                    }
                }
                pairingState is ScreenPairingState.Loading || restoringAdminSession -> {
                    MosqueTheme(theme = state.activeTheme) {
                        StartupScreen()
                    }
                }
                else -> {
                    SideEffect {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                    AdminTheme {
                        PairingScreen(
                            pairingState = pairingState,
                            deviceId = deviceId,
                            onConnect = viewModel::pairToScreen,
                            onClearError = viewModel::clearPairingError,
                            onEnterAdmin = {
                                showAdmin = true
                                adminLandscape = false
                                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) setImmersiveMode(immersiveEnabled)
    }

    private fun setImmersiveMode(enabled: Boolean) {
        immersiveEnabled = enabled
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
private fun StartupScreen() {
    val colors = LocalMosqueColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.screenTop, colors.backgroundDeep, colors.screenBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mosque Live Screen",
                color = colors.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = colors.primaryLight,
                strokeWidth = 3.dp
            )
        }
    }
}
