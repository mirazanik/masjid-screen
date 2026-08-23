package com.mirazanik.masjidscreen.admin.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mirazanik.masjidscreen.R
import com.mirazanik.masjidscreen.MainActivity
import com.mirazanik.masjidscreen.admin.data.AdminRepository
import com.mirazanik.masjidscreen.admin.data.AppVersionInfo
import com.mirazanik.masjidscreen.data.model.*
import com.mirazanik.masjidscreen.prayer.PrayerCalculator
import com.mirazanik.masjidscreen.ui.theme.AppTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class AdminUiState(
    val config: MosqueConfig = MosqueConfig(),
    val jamaatTimes: JamaatTimes = JamaatTimes(),
    val prayerTimes: PrayerTimesData = PrayerTimesData(),
    val hadiths: List<Hadith> = emptyList(),
    val notices: List<Notice> = emptyList(),
    val appVersion: AppVersionInfo = AppVersionInfo(),
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val activeTheme: AppTheme = AppTheme.NIGHT_NAVY,
    // Screen management
    val screens: List<ScreenInfo> = emptyList(),
    val selectedScreenId: String? = null,
    val screenConfig: ScreenConfig = ScreenConfig(),
    val screenPrayerTimes: PrayerTimesData = PrayerTimesData(),
    val screenJamaat: JamaatTimes = JamaatTimes(),
    val screenHadiths: List<Hadith> = emptyList(),
    val screenNotices: List<Notice> = emptyList(),
    val screenSaving: Boolean = false,
    val newScreenPairingCode: String? = null,
    // User & group management
    val currentUser: MosqueUser? = null,
    val allUsers: List<MosqueUser> = emptyList(),
    val invitedUsers: List<MosqueUser> = emptyList(),
    val pendingInvites: List<String> = emptyList(),
    val screenGroups: List<ScreenGroup> = emptyList(),
    val currentScreenGroup: ScreenGroup? = null,
    val userManagementSaving: Boolean = false,
    // Settings & notifications (super admin)
    val autoApprovalEnabled: Boolean = false,
    val pendingNotifications: List<PendingUserNotification> = emptyList(),
)

class AdminPanelViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = AdminRepository()

    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state.asStateFlow()

    private var screenObserveJobs: List<Job> = emptyList()
    private var userObserveJobs: List<Job> = emptyList()

    fun initForUser(user: MosqueUser) {
        if (_state.value.currentUser?.uid == user.uid) return
        _state.update { it.copy(currentUser = user) }

        userObserveJobs.forEach { it.cancel() }
        userObserveJobs = buildList {
            // Global config & data (always needed)
            add(viewModelScope.launch {
                repo.observeConfig().collect { c ->
                    _state.update {
                        it.copy(
                            config = c,
                            activeTheme = AppTheme.fromKey(c.activeTheme),
                            prayerTimes = PrayerCalculator.calculate(c.latitude, c.longitude, c.calculationMethod, c.madhab)
                        )
                    }
                }
            })
            add(viewModelScope.launch { repo.observeJamaat().collect { j -> _state.update { it.copy(jamaatTimes = j) } } })
            add(viewModelScope.launch { repo.observeHadiths().collect { h -> _state.update { it.copy(hadiths = h) } } })
            add(viewModelScope.launch { repo.observeNotices().collect { n -> _state.update { it.copy(notices = n) } } })
            add(viewModelScope.launch { repo.observeAppVersion().collect { v -> _state.update { it.copy(appVersion = v) } } })

            // Screen groups
            val groupsFlow = if (user.role == UserRole.SUPER_ADMIN)
                repo.observeScreenGroups()
            else
                repo.observeScreenGroupsForUser(user.uid)

            add(viewModelScope.launch {
                groupsFlow.collect { groups ->
                    _state.update { s ->
                        s.copy(
                            screenGroups = groups,
                            currentScreenGroup = s.selectedScreenId?.let { sid -> groups.find { sid in it.screenIds } }
                        )
                    }
                }
            })

            // Screens filtered by role
            add(viewModelScope.launch {
                if (user.role == UserRole.SUPER_ADMIN) {
                    repo.observeScreens().collect { screens -> _state.update { it.copy(screens = screens) } }
                } else {
                    combine(repo.observeScreens(), groupsFlow) { allScreens, groups ->
                        val accessible = groups.flatMap { it.screenIds }.toSet()
                        allScreens.filter {
                            it.ownerUserId == user.uid || it.screenId in accessible
                        }
                    }.collect { screens -> _state.update { it.copy(screens = screens) } }
                }
            })

            // User list, settings, notifications (super admin only)
            if (user.role == UserRole.SUPER_ADMIN) {
                add(viewModelScope.launch {
                    repo.observeAllUsers().collect { users -> _state.update { it.copy(allUsers = users) } }
                })
                add(viewModelScope.launch {
                    repo.observeSettings().collect { s -> _state.update { it.copy(autoApprovalEnabled = s.autoApprovalEnabled) } }
                })
                add(viewModelScope.launch {
                    var prevUnreadIds = emptySet<String>()
                    repo.observePendingNotifications().collect { notifications ->
                        val newUnread = notifications
                            .filter { !it.read && it.id !in prevUnreadIds }
                        if (newUnread.isNotEmpty() && prevUnreadIds.isNotEmpty()) {
                            newUnread.forEach { showLocalNotification(it) }
                        }
                        prevUnreadIds = notifications.filter { !it.read }.map { it.id }.toSet()
                        _state.update { it.copy(pendingNotifications = notifications) }
                    }
                })
            } else {
                add(viewModelScope.launch {
                    repo.observeUsersInvitedBy(user.uid).collect { users -> _state.update { it.copy(invitedUsers = users) } }
                })
                add(viewModelScope.launch {
                    repo.observePendingInvites(user.uid).collect { invites -> _state.update { it.copy(pendingInvites = invites) } }
                })
            }
        }
    }

    // ── Global config (SUPER_ADMIN only) ──────────────────────────────────────

    private fun isSuperAdmin(): Boolean =
        _state.value.currentUser?.role == UserRole.SUPER_ADMIN

    private fun rejectIfNotSuperAdmin(): Boolean {
        if (isSuperAdmin()) return false
        _state.update { it.copy(isSaving = false, errorMessage = "Only a super admin can change global settings") }
        return true
    }

    fun saveTheme(theme: AppTheme) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.saveTheme(theme.key) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun saveConfig(config: MosqueConfig) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            repo.saveConfig(config)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(isSaving = false, errorMessage = e.message) }
        }
    }

    fun saveJamaat(times: JamaatTimes) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            repo.saveJamaat(times)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(isSaving = false, errorMessage = e.message) }
        }
    }

    fun addHadith(translation: String, source: String, narrator: String) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.addHadith(translation, source, narrator) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun updateHadith(id: String, translation: String, source: String, narrator: String, active: Boolean) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.updateHadith(id, translation, source, narrator, active) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun toggleHadithActive(id: String, active: Boolean) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.toggleHadithActive(id, active) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun deleteHadith(id: String) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.deleteHadith(id) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun addNotice(text: String, priority: Int = 0) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.addNotice(text, priority) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun updateNotice(id: String, text: String, active: Boolean, priority: Int) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.updateNotice(id, text, active, priority) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun deleteNotice(id: String) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        try { repo.deleteNotice(id) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun saveAppVersion(info: AppVersionInfo) = viewModelScope.launch {
        if (rejectIfNotSuperAdmin()) return@launch
        _state.update { it.copy(isSaving = true, errorMessage = null) }
        try {
            repo.saveAppVersion(info)
            _state.update { it.copy(isSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(isSaving = false, errorMessage = e.message) }
        }
    }

    // ── Screen management ─────────────────────────────────────────────────────

    fun createScreen(name: String, pairedDeviceId: String = "") = viewModelScope.launch {
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            val ownerUserId = _state.value.currentUser?.uid
                ?: error("User session is not ready. Please sign in again.")
            val pairingCode = repo.createScreen(name, ownerUserId, pairedDeviceId)
            _state.update {
                it.copy(
                    screenSaving = false,
                    newScreenPairingCode = pairingCode.takeIf { pairedDeviceId.isBlank() },
                    saveSuccess = pairedDeviceId.isNotBlank()
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun bindDeviceToScreen(screenId: String, deviceId: String) = viewModelScope.launch {
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            repo.bindDeviceToScreen(screenId, deviceId)
            _state.update { it.copy(screenSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun clearNewScreenPairingCode() {
        _state.update { it.copy(newScreenPairingCode = null) }
    }

    fun unbindScreen(screenId: String) = viewModelScope.launch {
        try { repo.unbindScreen(screenId) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun deleteScreen(screenId: String) = viewModelScope.launch {
        try {
            if (_state.value.selectedScreenId == screenId) deselectScreen()
            repo.deleteScreen(screenId)
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = e.message) }
        }
    }

    fun selectScreen(screenId: String) {
        screenObserveJobs.forEach { it.cancel() }
        val group = _state.value.screenGroups.find { screenId in it.screenIds }
        _state.update {
            it.copy(
                selectedScreenId = screenId,
                currentScreenGroup = group,
                screenConfig = ScreenConfig(screenId = screenId),
                screenPrayerTimes = PrayerTimesData(),
                screenJamaat = JamaatTimes(),
                screenHadiths = emptyList(),
                screenNotices = emptyList()
            )
        }
        screenObserveJobs = listOf(
            viewModelScope.launch {
                combine(repo.observeScreenConfig(screenId), repo.observeConfig()) { screenConfig, globalConfig ->
                    screenConfig to globalConfig
                }.collect { (c, global) ->
                    _state.update {
                        it.copy(
                            screenConfig = c,
                            screenPrayerTimes = calculateScreenPrayerTimes(c, global)
                        )
                    }
                }
            },
            viewModelScope.launch { repo.observeScreenJamaat(screenId).collect { j -> _state.update { it.copy(screenJamaat = j) } } },
            viewModelScope.launch { repo.observeScreenHadiths(screenId).collect { h -> _state.update { it.copy(screenHadiths = h) } } },
            viewModelScope.launch { repo.observeScreenNotices(screenId).collect { n -> _state.update { it.copy(screenNotices = n) } } }
        )
    }

    fun deselectScreen() {
        screenObserveJobs.forEach { it.cancel() }
        screenObserveJobs = emptyList()
        _state.update { it.copy(selectedScreenId = null, currentScreenGroup = null) }
    }

    private fun groupScreenIds(applyToGroup: Boolean): List<String> {
        val sid = _state.value.selectedScreenId ?: return emptyList()
        return if (applyToGroup) _state.value.currentScreenGroup?.screenIds ?: listOf(sid) else listOf(sid)
    }

    private fun calculateScreenPrayerTimes(screenConfig: ScreenConfig, globalConfig: MosqueConfig): PrayerTimesData {
        val latitude = screenConfig.latitude.takeIf { it != 0.0 } ?: globalConfig.latitude
        val longitude = screenConfig.longitude.takeIf { it != 0.0 } ?: globalConfig.longitude
        val method = screenConfig.calculationMethod.takeIf { it.isNotBlank() } ?: globalConfig.calculationMethod
        val madhab = screenConfig.madhab.takeIf { it.isNotBlank() } ?: globalConfig.madhab
        return PrayerCalculator.calculate(latitude, longitude, method, madhab)
    }

    fun saveScreenConfig(config: ScreenConfig, applyToGroup: Boolean = false) = viewModelScope.launch {
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            groupScreenIds(applyToGroup).forEach { sid -> repo.saveScreenConfig(sid, config.copy(screenId = sid)) }
            _state.update { it.copy(screenSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun saveScreenJamaat(times: JamaatTimes, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            groupScreenIds(applyToGroup).forEach { id -> repo.saveScreenJamaat(id, times) }
            _state.update { it.copy(screenSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun addScreenHadith(translation: String, source: String, narrator: String, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { id -> repo.addScreenHadith(id, translation, source, narrator) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun updateScreenHadith(id: String, translation: String, source: String, narrator: String, active: Boolean, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { screenId -> repo.updateScreenHadith(screenId, id, translation, source, narrator, active) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun toggleScreenHadithActive(id: String, active: Boolean) = viewModelScope.launch {
        val sid = _state.value.selectedScreenId ?: return@launch
        try { repo.toggleScreenHadithActive(sid, id, active) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun deleteScreenHadith(id: String, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { screenId -> repo.deleteScreenHadith(screenId, id) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun addScreenNotice(text: String, priority: Int = 0, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { id -> repo.addScreenNotice(id, text, priority) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun updateScreenNotice(id: String, text: String, active: Boolean, priority: Int, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { screenId -> repo.updateScreenNotice(screenId, id, text, active, priority) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun deleteScreenNotice(id: String, applyToGroup: Boolean = false) = viewModelScope.launch {
        if (_state.value.selectedScreenId == null) return@launch
        try { groupScreenIds(applyToGroup).forEach { screenId -> repo.deleteScreenNotice(screenId, id) } }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun refreshScreens() = viewModelScope.launch {
        try {
            val screens = repo.refreshScreens()
            val currentUser = _state.value.currentUser
            val filtered = if (currentUser?.role == UserRole.SUPER_ADMIN) {
                screens
            } else {
                val accessible = _state.value.screenGroups.flatMap { it.screenIds }.toSet()
                screens.filter {
                    it.ownerUserId == currentUser?.uid || it.screenId in accessible
                }
            }
            _state.update { it.copy(screens = filtered) }
        } catch (e: Exception) {
            _state.update { it.copy(errorMessage = e.message) }
        }
    }

    // ── User management (SUPER_ADMIN only) ────────────────────────────────────

    fun approveUser(uid: String, role: UserRole) = viewModelScope.launch {
        _state.update { it.copy(userManagementSaving = true) }
        try {
            repo.updateUserRole(uid, role)
            repo.updateUserStatus(uid, UserStatus.ACTIVE)
            _state.update { it.copy(userManagementSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(userManagementSaving = false, errorMessage = e.message) }
        }
    }

    fun disableUser(uid: String) = viewModelScope.launch {
        _state.update { it.copy(userManagementSaving = true) }
        try {
            repo.updateUserStatus(uid, UserStatus.DISABLED)
            _state.update { it.copy(userManagementSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(userManagementSaving = false, errorMessage = e.message) }
        }
    }

    fun changeUserRole(uid: String, role: UserRole) = viewModelScope.launch {
        _state.update { it.copy(userManagementSaving = true) }
        try {
            repo.updateUserRole(uid, role)
            _state.update { it.copy(userManagementSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(userManagementSaving = false, errorMessage = e.message) }
        }
    }

    fun toggleAutoApproval(enabled: Boolean) = viewModelScope.launch {
        try { repo.saveSettings(AppSettings(autoApprovalEnabled = enabled)) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun markNotificationRead(id: String) = viewModelScope.launch {
        try { repo.markNotificationRead(id) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun clearReadNotifications() = viewModelScope.launch {
        try { repo.clearReadNotifications() }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun updateUserDisplayName(uid: String, displayName: String) = viewModelScope.launch {
        try { repo.updateUserDisplayName(uid, displayName) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    // ── Screen group management ───────────────────────────────────────────────

    fun createScreenGroup(name: String, screenIds: List<String>, memberUserIds: List<String>) = viewModelScope.launch {
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            val ownerUserId = _state.value.currentUser?.uid ?: ""
            repo.createScreenGroup(name, screenIds, memberUserIds, ownerUserId)
            _state.update { it.copy(screenSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun updateScreenGroup(group: ScreenGroup) = viewModelScope.launch {
        _state.update { it.copy(screenSaving = true, errorMessage = null) }
        try {
            repo.updateScreenGroup(group)
            _state.update { it.copy(screenSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(screenSaving = false, errorMessage = e.message) }
        }
    }

    fun deleteScreenGroup(groupId: String) = viewModelScope.launch {
        try { repo.deleteScreenGroup(groupId) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun inviteUser(email: String) = viewModelScope.launch {
        _state.update { it.copy(userManagementSaving = true) }
        try {
            val adminUid = _state.value.currentUser?.uid ?: error("Not signed in")
            repo.inviteUser(email.trim().lowercase(), adminUid)
            _state.update { it.copy(userManagementSaving = false, saveSuccess = true) }
        } catch (e: Exception) {
            _state.update { it.copy(userManagementSaving = false, errorMessage = e.message) }
        }
    }

    fun cancelInvite(email: String) = viewModelScope.launch {
        try { repo.cancelInvite(email) }
        catch (e: Exception) { _state.update { it.copy(errorMessage = e.message) } }
    }

    fun clearSaveSuccess() { _state.update { it.copy(saveSuccess = false) } }
    fun clearError() { _state.update { it.copy(errorMessage = null) } }

    companion object {
        const val NOTIF_CHANNEL_ID = "pending_users"
    }

    private fun showLocalNotification(notification: PendingUserNotification) {
        val ctx = getApplication<Application>()
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openAdmin", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, notification.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val name = notification.displayName.ifBlank { notification.email }
        val notif = NotificationCompat.Builder(ctx, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New User Pending Approval")
            .setContentText("$name is waiting for approval")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val manager = ctx.getSystemService(NotificationManager::class.java)
        manager.notify(notification.id.hashCode(), notif)
    }
}
