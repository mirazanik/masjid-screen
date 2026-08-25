package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.admin.viewmodel.AdminAuthViewModel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel
import com.mirazanik.masjidscreen.data.model.UserRole
import com.mirazanik.masjidscreen.data.model.UserStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    adminVm: AdminPanelViewModel,
    authVm: AdminAuthViewModel,
    onBack: () -> Unit,
    onRequestLandscape: (Boolean) -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
) {
    val state by adminVm.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isSuperAdmin = state.currentUser?.role == UserRole.SUPER_ADMIN

    // 0=Screens, 1=Users, 2=Groups, 3=Global (super admin only)
    var topTab by remember { mutableIntStateOf(0) }
    var screenManageOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isSuperAdmin) {
        if (!isSuperAdmin && topTab == 3) topTab = 0
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Saved successfully!")
            adminVm.clearSaveSuccess()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            adminVm.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = if (screenManageOpen) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (!screenManageOpen) {
                Column {
                    TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Admin Panel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            state.currentUser?.let { user ->
                                Text(
                                    "${user.displayName.ifBlank { user.email }} · ${if (user.role == UserRole.SUPER_ADMIN) "Super Admin" else "Admin"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Display")
                        }
                    },
                    actions = {
                        IconButton(onClick = onPrivacyPolicy) {
                            Icon(
                                Icons.Default.Policy,
                                contentDescription = "Privacy Policy",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )

                TabRow(
                    selectedTabIndex = topTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = topTab == 0,
                        onClick = { topTab = 0 },
                        text = { Text("Screens", style = MaterialTheme.typography.labelMedium) },
                        icon = { Icon(Icons.Default.Tv, null, modifier = Modifier.size(16.dp)) }
                    )
                    Tab(
                        selected = topTab == 1,
                        onClick = { topTab = 1 },
                        text = { Text("Users", style = MaterialTheme.typography.labelMedium) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    val badgeCount = if (isSuperAdmin)
                                        state.allUsers.count { it.status == UserStatus.PENDING }
                                    else
                                        state.pendingInvites.size
                                    if (badgeCount > 0) Badge { Text("$badgeCount") }
                                }
                            ) {
                                Icon(Icons.Default.Group, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    Tab(
                        selected = topTab == 2,
                        onClick = { topTab = 2 },
                        text = { Text("Groups", style = MaterialTheme.typography.labelMedium) },
                        icon = { Icon(Icons.Default.Link, null, modifier = Modifier.size(16.dp)) }
                    )
                    if (isSuperAdmin) {
                        Tab(
                            selected = topTab == 3,
                            onClick = { topTab = 3 },
                            text = { Text("Global", style = MaterialTheme.typography.labelMedium) },
                            icon = { Icon(Icons.Default.Public, null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (screenManageOpen) Modifier else Modifier.padding(padding))
        ) {
            when (topTab) {
                0 -> ScreensScreen(
                    state = state,
                    adminVm = adminVm,
                    onManagingChanged = { screenManageOpen = it },
                    onRequestLandscape = onRequestLandscape
                )
                1 -> UserManagementScreen(state = state, adminVm = adminVm)
                2 -> ScreenGroupScreen(state = state, adminVm = adminVm)
                3 -> if (isSuperAdmin) {
                    GlobalSettingsScreen(
                        config = state.config,
                        isSaving = state.isSaving,
                        onSave = adminVm::saveConfig
                    )
                } else {
                    ScreensScreen(
                        state = state,
                        adminVm = adminVm,
                        onManagingChanged = { screenManageOpen = it },
                        onRequestLandscape = onRequestLandscape
                    )
                }
                else -> ScreensScreen(
                    state = state,
                    adminVm = adminVm,
                    onManagingChanged = { screenManageOpen = it },
                    onRequestLandscape = onRequestLandscape
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Sign out from the admin panel?") },
            confirmButton = {
                TextButton(
                    onClick = { authVm.logout(); showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}
