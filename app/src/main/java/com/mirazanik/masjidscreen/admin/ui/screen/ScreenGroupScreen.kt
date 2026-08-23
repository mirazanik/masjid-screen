package com.mirazanik.masjidscreen.admin.ui.screen

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
import com.mirazanik.masjidscreen.data.model.ScreenGroup
import com.mirazanik.masjidscreen.data.model.ScreenInfo

@Composable
fun ScreenGroupScreen(state: AdminUiState, adminVm: AdminPanelViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<ScreenGroup?>(null) }
    var deleteTarget by remember { mutableStateOf<ScreenGroup?>(null) }

    // Merge all users visible to the current role for display and member selection
    val usersForDisplay = remember(state.allUsers, state.invitedUsers) {
        (state.allUsers + state.invitedUsers).distinctBy { it.uid }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.screenGroups.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Link, null, modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("No screen groups", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Groups link screens so changes can sync across them",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.screenGroups, key = { it.groupId }) { group ->
                    GroupCard(
                        group = group,
                        screens = state.screens,
                        users = usersForDisplay,
                        onEdit = { editTarget = group },
                        onDelete = { deleteTarget = group }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Group")
        }
    }

    if (showCreateDialog) {
        GroupEditDialog(
            title = "Create Group",
            initial = ScreenGroup(),
            screens = state.screens,
            users = usersForDisplay,
            isSaving = state.screenSaving,
            onConfirm = { name, screenIds, memberIds ->
                adminVm.createScreenGroup(name, screenIds, memberIds)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    editTarget?.let { group ->
        GroupEditDialog(
            title = "Edit Group",
            initial = group,
            screens = state.screens,
            users = usersForDisplay,
            isSaving = state.screenSaving,
            onConfirm = { name, screenIds, memberIds ->
                adminVm.updateScreenGroup(group.copy(name = name, screenIds = screenIds, memberUserIds = memberIds))
                editTarget = null
            },
            onDismiss = { editTarget = null }
        )
    }

    deleteTarget?.let { group ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Group") },
            text = { Text("Delete group \"${group.name}\"? Screens are not deleted, just unlinked.") },
            confirmButton = {
                TextButton(
                    onClick = { adminVm.deleteScreenGroup(group.groupId); deleteTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun GroupCard(
    group: ScreenGroup,
    screens: List<ScreenInfo>,
    users: List<com.mirazanik.masjidscreen.data.model.MosqueUser>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val groupScreenNames = screens.filter { it.screenId in group.screenIds }.map { it.name.ifBlank { it.screenId } }
    val groupUserNames = users.filter { it.uid in group.memberUserIds }.map { it.displayName.ifBlank { it.email } }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Link, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text(group.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }

            if (groupScreenNames.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Tv, null, modifier = Modifier.size(14.dp).padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Screens: ${groupScreenNames.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (groupUserNames.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp).padding(top = 2.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Members: ${groupUserNames.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (groupScreenNames.isEmpty() && groupUserNames.isEmpty()) {
                Text("No screens or members assigned yet",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupEditDialog(
    title: String,
    initial: ScreenGroup,
    screens: List<ScreenInfo>,
    users: List<com.mirazanik.masjidscreen.data.model.MosqueUser>,
    isSaving: Boolean,
    onConfirm: (String, List<String>, List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial.name) }
    var selectedScreenIds by remember(initial) { mutableStateOf(initial.screenIds.toSet()) }
    var selectedUserIds by remember(initial) { mutableStateOf(initial.memberUserIds.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                if (screens.isNotEmpty()) {
                    Text("Screens", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    screens.forEach { screen ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = screen.screenId in selectedScreenIds,
                                onCheckedChange = { checked ->
                                    selectedScreenIds = if (checked)
                                        selectedScreenIds + screen.screenId
                                    else
                                        selectedScreenIds - screen.screenId
                                }
                            )
                            Text(screen.name.ifBlank { screen.screenId },
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (users.isNotEmpty()) {
                    Text("Members", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    users.forEach { user ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = user.uid in selectedUserIds,
                                onCheckedChange = { checked ->
                                    selectedUserIds = if (checked)
                                        selectedUserIds + user.uid
                                    else
                                        selectedUserIds - user.uid
                                }
                            )
                            Column {
                                Text(user.displayName.ifBlank { user.email },
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(user.email, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), selectedScreenIds.toList(), selectedUserIds.toList()) },
                enabled = name.isNotBlank() && !isSaving
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
