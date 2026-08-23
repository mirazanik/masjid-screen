package com.mirazanik.masjidscreen.admin.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel
import com.mirazanik.masjidscreen.admin.viewmodel.AdminUiState
import com.mirazanik.masjidscreen.data.model.MosqueUser
import com.mirazanik.masjidscreen.data.model.PendingUserNotification
import com.mirazanik.masjidscreen.data.model.UserRole
import com.mirazanik.masjidscreen.data.model.UserStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(state: AdminUiState, adminVm: AdminPanelViewModel) {
    if (state.currentUser?.role == UserRole.SUPER_ADMIN) {
        SuperAdminUserScreen(state = state, adminVm = adminVm)
    } else {
        AdminInviteScreen(state = state, adminVm = adminVm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuperAdminUserScreen(state: AdminUiState, adminVm: AdminPanelViewModel) {
    val pending = state.allUsers.filter { it.status == UserStatus.PENDING }
    val active = state.allUsers.filter { it.status == UserStatus.ACTIVE }
    val disabled = state.allUsers.filter { it.status == UserStatus.DISABLED }

    var approveTarget by remember { mutableStateOf<MosqueUser?>(null) }
    var disableTarget by remember { mutableStateOf<MosqueUser?>(null) }
    var changeRoleTarget by remember { mutableStateOf<MosqueUser?>(null) }
    var showNotifications by remember { mutableStateOf(false) }

    val unreadCount = state.pendingNotifications.count { !it.read }

    Column(modifier = Modifier.fillMaxSize()) {
        // Settings & notification panel
        ElevatedCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Auto-Approval", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (state.autoApprovalEnabled) "New users approved automatically" else "New users need manual approval",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.autoApprovalEnabled,
                        onCheckedChange = { adminVm.toggleAutoApproval(it) }
                    )
                }

                if (state.pendingNotifications.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BadgedBox(badge = {
                                if (unreadCount > 0) Badge { Text(unreadCount.toString()) }
                            }) {
                                Icon(Icons.Default.Notifications, null,
                                    tint = if (unreadCount > 0) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                            }
                            Text(
                                if (unreadCount > 0) "$unreadCount new pending sign-in${if (unreadCount > 1) "s" else ""}"
                                else "No new notifications",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            TextButton(onClick = { showNotifications = !showNotifications }) {
                                Text(if (showNotifications) "Hide" else "Show")
                            }
                            if (state.pendingNotifications.any { it.read }) {
                                TextButton(onClick = { adminVm.clearReadNotifications() }) {
                                    Text("Clear read", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = showNotifications) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            state.pendingNotifications.forEach { notif ->
                                NotificationItem(
                                    notification = notif,
                                    onMarkRead = { adminVm.markNotificationRead(notif.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.allUsers.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Group, null, modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("No users yet", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (pending.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Default.HourglassEmpty,
                            title = "Pending Approval (${pending.size})",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    items(pending, key = { it.uid }) { user ->
                        UserCard(user = user, onApprove = { approveTarget = user }, onDisable = { disableTarget = user }, onChangeRole = null)
                    }
                }
                if (active.isNotEmpty()) {
                    item {
                        SectionHeader(icon = Icons.Default.CheckCircle, title = "Active (${active.size})",
                            color = MaterialTheme.colorScheme.primary)
                    }
                    items(active, key = { it.uid }) { user ->
                        UserCard(user = user, onApprove = null, onDisable = { disableTarget = user },
                            onChangeRole = { changeRoleTarget = user })
                    }
                }
                if (disabled.isNotEmpty()) {
                    item {
                        SectionHeader(icon = Icons.Default.Block, title = "Disabled (${disabled.size})",
                            color = MaterialTheme.colorScheme.error)
                    }
                    items(disabled, key = { it.uid }) { user ->
                        UserCard(user = user, onApprove = { approveTarget = user }, onDisable = null, onChangeRole = null)
                    }
                }
            }
        }
    }

    approveTarget?.let { user ->
        var selectedRole by remember { mutableStateOf(UserRole.ADMIN) }
        var roleExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            title = { Text("Approve User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Approving: ${user.displayName.ifBlank { user.email }}")
                    ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                        OutlinedTextField(
                            value = if (selectedRole == UserRole.SUPER_ADMIN) "Super Admin" else "Admin",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Assign Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                            DropdownMenuItem(text = { Text("Admin") },
                                onClick = { selectedRole = UserRole.ADMIN; roleExpanded = false })
                            DropdownMenuItem(text = { Text("Super Admin") },
                                onClick = { selectedRole = UserRole.SUPER_ADMIN; roleExpanded = false })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { adminVm.approveUser(user.uid, selectedRole); approveTarget = null }) { Text("Approve") }
            },
            dismissButton = { TextButton(onClick = { approveTarget = null }) { Text("Cancel") } }
        )
    }

    disableTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { disableTarget = null },
            title = { Text("Disable User") },
            text = { Text("Disable access for ${user.displayName.ifBlank { user.email }}?") },
            confirmButton = {
                TextButton(
                    onClick = { adminVm.disableUser(user.uid); disableTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Disable") }
            },
            dismissButton = { TextButton(onClick = { disableTarget = null }) { Text("Cancel") } }
        )
    }

    changeRoleTarget?.let { user ->
        var selectedRole by remember(user.uid) { mutableStateOf(user.role) }
        var roleExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { changeRoleTarget = null },
            title = { Text("Change Role") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("User: ${user.displayName.ifBlank { user.email }}")
                    ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                        OutlinedTextField(
                            value = if (selectedRole == UserRole.SUPER_ADMIN) "Super Admin" else "Admin",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                            DropdownMenuItem(text = { Text("Admin") },
                                onClick = { selectedRole = UserRole.ADMIN; roleExpanded = false })
                            DropdownMenuItem(text = { Text("Super Admin") },
                                onClick = { selectedRole = UserRole.SUPER_ADMIN; roleExpanded = false })
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { adminVm.changeUserRole(user.uid, selectedRole); changeRoleTarget = null },
                    enabled = selectedRole != user.role
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { changeRoleTarget = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun NotificationItem(notification: PendingUserNotification, onMarkRead: () -> Unit) {
    val fmt = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (!notification.read) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (!notification.read) Icons.Default.FiberNew else Icons.Default.DoneAll,
                null,
                tint = if (!notification.read) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    notification.displayName.ifBlank { notification.email },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (!notification.read) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    "${notification.email} • ${fmt.format(Date(notification.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!notification.read) {
                TextButton(onClick = onMarkRead, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("Mark read", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun AdminInviteScreen(state: AdminUiState, adminVm: AdminPanelViewModel) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var cancelInviteTarget by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.invitedUsers.isEmpty() && state.pendingInvites.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("No users yet", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Tap + to invite users by email",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.invitedUsers.isNotEmpty()) {
                    item {
                        SectionHeader(icon = Icons.Default.Group,
                            title = "My Users (${state.invitedUsers.size})",
                            color = MaterialTheme.colorScheme.primary)
                    }
                    items(state.invitedUsers, key = { it.uid }) { user ->
                        UserCard(user = user, onApprove = null, onDisable = null, onChangeRole = null)
                    }
                }
                if (state.pendingInvites.isNotEmpty()) {
                    item {
                        SectionHeader(icon = Icons.Default.Email,
                            title = "Pending Invites (${state.pendingInvites.size})",
                            color = MaterialTheme.colorScheme.tertiary)
                    }
                    items(state.pendingInvites, key = { it }) { email ->
                        PendingInviteCard(email = email, onCancel = { cancelInviteTarget = email })
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showInviteDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Invite User")
        }
    }

    if (showInviteDialog) {
        InviteUserDialog(
            isSaving = state.userManagementSaving,
            onConfirm = { email -> adminVm.inviteUser(email); showInviteDialog = false },
            onDismiss = { showInviteDialog = false }
        )
    }

    cancelInviteTarget?.let { email ->
        AlertDialog(
            onDismissRequest = { cancelInviteTarget = null },
            title = { Text("Cancel Invite") },
            text = { Text("Cancel the invite for $email?") },
            confirmButton = {
                TextButton(
                    onClick = { adminVm.cancelInvite(email); cancelInviteTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Cancel Invite") }
            },
            dismissButton = { TextButton(onClick = { cancelInviteTarget = null }) { Text("Keep") } }
        )
    }
}

@Composable
private fun PendingInviteCard(email: String, onCancel: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Email, null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(email, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("Invite pending — waiting for registration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel Invite",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun InviteUserDialog(isSaving: Boolean, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var email by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Invite User") },
        text = {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it.trim() },
                label = { Text("Email Address") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (email.isNotBlank()) onConfirm(email) },
                enabled = email.isNotBlank() && !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Send Invite")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun UserCard(
    user: MosqueUser,
    onApprove: (() -> Unit)?,
    onDisable: (() -> Unit)?,
    onChangeRole: (() -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = when (user.status) {
                    UserStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                    UserStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                    UserStatus.DISABLED -> MaterialTheme.colorScheme.errorContainer
                },
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = (user.displayName.ifBlank { user.email }).take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.displayName.ifBlank { user.email },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    user.email,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoleBadge(user.role)
                    StatusBadge(user.status)
                }
            }

            if (onChangeRole != null) {
                IconButton(onClick = onChangeRole) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = "Change Role",
                        tint = MaterialTheme.colorScheme.secondary)
                }
            }
            if (onApprove != null) {
                FilledTonalIconButton(onClick = onApprove) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Approve",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            if (onDisable != null) {
                IconButton(onClick = onDisable) {
                    Icon(Icons.Default.Block, contentDescription = "Disable",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (role == UserRole.SUPER_ADMIN)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = if (role == UserRole.SUPER_ADMIN) "Super Admin" else "Admin",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (role == UserRole.SUPER_ADMIN)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatusBadge(status: UserStatus) {
    val (bg, fg, label) = when (status) {
        UserStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Active"
        )
        UserStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Pending"
        )
        UserStatus.DISABLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Disabled"
        )
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = bg) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
