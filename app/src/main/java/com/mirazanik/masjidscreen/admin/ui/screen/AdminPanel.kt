package com.mirazanik.masjidscreen.admin.ui.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.mirazanik.masjidscreen.admin.viewmodel.AdminAuthState
import com.mirazanik.masjidscreen.admin.viewmodel.AdminAuthViewModel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel

@Composable
fun AdminPanel(
    authViewModel: AdminAuthViewModel,
    adminViewModel: AdminPanelViewModel,
    onBack: () -> Unit,
    isDevicePaired: Boolean = false,
    unpairError: String? = null,
    onUnpair: () -> Unit = {},
    onClearUnpairError: () -> Unit = {},
    onRequestLandscape: (Boolean) -> Unit = {}
) {
    var showRegister by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    BackHandler(onBack = {
        when {
            showPrivacyPolicy -> showPrivacyPolicy = false
            showForgotPassword -> showForgotPassword = false
            showRegister -> showRegister = false
            else -> onBack()
        }
    })
    val authState by authViewModel.authState.collectAsState()

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* result handled by system */ }

    LaunchedEffect(authState) {
        if (authState is AdminAuthState.Authenticated) {
            adminViewModel.initForUser((authState as AdminAuthState.Authenticated).user)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyScreen(onBack = { showPrivacyPolicy = false })
    } else when (val state = authState) {
        is AdminAuthState.Authenticated ->
            DashboardScreen(
                adminVm = adminViewModel,
                authVm = authViewModel,
                onBack = onBack,
                onRequestLandscape = onRequestLandscape,
                onPrivacyPolicy = { showPrivacyPolicy = true },
            )

        is AdminAuthState.PendingApproval ->
            PendingApprovalScreen(onSignOut = authViewModel::logout)

        is AdminAuthState.Disabled ->
            PendingApprovalScreen(onSignOut = authViewModel::logout)

        else -> {
            when {
                showForgotPassword -> ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { showForgotPassword = false }
                )
                showRegister -> RegisterScreen(
                    viewModel = authViewModel,
                    onBackToLogin = { showRegister = false },
                    onPrivacyPolicy = { showPrivacyPolicy = true },
                )
                else -> LoginScreen(
                    viewModel = authViewModel,
                    onRegister = { showRegister = true },
                    onForgotPassword = { showForgotPassword = true },
                    onPrivacyPolicy = { showPrivacyPolicy = true },
                    showUnpair = isDevicePaired,
                    unpairError = unpairError,
                    onUnpair = onUnpair,
                    onClearUnpairError = onClearUnpairError
                )
            }
        }
    }
}
