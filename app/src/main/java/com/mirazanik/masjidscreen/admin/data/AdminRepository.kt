package com.mirazanik.masjidscreen.admin.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mirazanik.masjidscreen.data.model.*
import com.mirazanik.masjidscreen.data.remote.FirestoreProvider
import com.mirazanik.masjidscreen.data.remote.firestoreEpochMs
import com.mirazanik.masjidscreen.data.remote.withFirestoreRetry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class AppVersionInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false
)

class AdminRepository {
    private val firestore: FirebaseFirestore = FirestoreProvider.instance
    private val mosqueId = "default"
    private val rootRef get() = firestore.collection("mosques").document(mosqueId)
    private val screensRef get() = rootRef.collection("screens")

    // ── Global mosque config ──────────────────────────────────────────────────

    fun observeConfig(): Flow<MosqueConfig> = callbackFlow {
        val reg = rootRef.collection("data").document("config")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d ->
                    trySend(MosqueConfig(
                        name = d["name"] as? String ?: "Masjid",
                        address = d["address"] as? String ?: "",
                        latitude = (d["latitude"] as? Number)?.toDouble() ?: 23.777176,
                        longitude = (d["longitude"] as? Number)?.toDouble() ?: 90.399452,
                        calculationMethod = d["calculationMethod"] as? String ?: "MWL",
                        madhab = d["madhab"] as? String ?: "HANAFI",
                        language = d["language"] as? String ?: "en",
                        hadithInterval = (d["hadithInterval"] as? Number)?.toInt() ?: 30,
                        jamaatCountdownMins = (d["jamaatCountdownMins"] as? Number)?.toInt() ?: 3,
                        hijriDateOffset = (d["hijriDateOffset"] as? Number)?.toInt() ?: 0,
                        tableFontScale = (d["tableFontScale"] as? Number)?.toFloat() ?: 1f,
                        activeTheme = d["activeTheme"] as? String ?: "night_navy"
                    ))
                } ?: trySend(MosqueConfig())
            }
        awaitClose { reg.remove() }
    }

    fun observeJamaat(): Flow<JamaatTimes> = callbackFlow {
        val reg = rootRef.collection("data").document("jamaat_times")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d -> trySend(mapJamaat(d)) } ?: trySend(JamaatTimes())
            }
        awaitClose { reg.remove() }
    }

    fun observeHadiths(): Flow<List<Hadith>> = callbackFlow {
        val reg = rootRef.collection("hadiths")
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapHadith(doc.id, it) } } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun observeNotices(): Flow<List<Notice>> = callbackFlow {
        val reg = rootRef.collection("notices")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc -> doc.data?.let { mapNotice(doc.id, it) } } ?: emptyList()
                trySend(list.sortedBy { it.priority })
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveConfig(config: MosqueConfig) {
        rootRef.collection("data").document("config").set(
            mapOf(
                "name" to config.name,
                "address" to config.address,
                "latitude" to config.latitude,
                "longitude" to config.longitude,
                "calculationMethod" to config.calculationMethod,
                "madhab" to config.madhab,
                "language" to config.language,
                "hadithInterval" to config.hadithInterval,
                "jamaatCountdownMins" to config.jamaatCountdownMins,
                "hijriDateOffset" to config.hijriDateOffset,
                "tableFontScale" to config.tableFontScale,
                "activeTheme" to config.activeTheme,
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun saveTheme(themeKey: String) {
        rootRef.collection("data").document("config")
            .set(mapOf("activeTheme" to themeKey), SetOptions.merge())
            .await()
    }

    suspend fun saveJamaat(times: JamaatTimes) {
        rootRef.collection("data").document("jamaat_times").set(jamaatMap(times)).await()
    }

    suspend fun addHadith(translation: String, source: String, narrator: String) {
        rootRef.collection("hadiths").add(
            mapOf("translation" to translation, "source" to source, "narrator" to narrator, "active" to true)
        ).await()
    }

    suspend fun updateHadith(id: String, translation: String, source: String, narrator: String, active: Boolean) {
        rootRef.collection("hadiths").document(id).set(
            mapOf("translation" to translation, "source" to source, "narrator" to narrator, "active" to active)
        ).await()
    }

    suspend fun toggleHadithActive(id: String, active: Boolean) {
        rootRef.collection("hadiths").document(id).update("active", active).await()
    }

    suspend fun deleteHadith(id: String) {
        rootRef.collection("hadiths").document(id).delete().await()
    }

    suspend fun addNotice(text: String, priority: Int = 0) {
        rootRef.collection("notices").add(
            mapOf("text" to text, "active" to true, "priority" to priority)
        ).await()
    }

    suspend fun updateNotice(id: String, text: String, active: Boolean, priority: Int) {
        rootRef.collection("notices").document(id).set(
            mapOf("text" to text, "active" to active, "priority" to priority)
        ).await()
    }

    suspend fun deleteNotice(id: String) {
        rootRef.collection("notices").document(id).delete().await()
    }

    fun observeAppVersion(): Flow<AppVersionInfo> = callbackFlow {
        val reg = rootRef.collection("data").document("app_version")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d ->
                    trySend(AppVersionInfo(
                        versionCode = (d["versionCode"] as? Number)?.toInt() ?: 0,
                        versionName = d["versionName"] as? String ?: "",
                        downloadUrl = d["downloadUrl"] as? String ?: "",
                        forceUpdate = d["forceUpdate"] as? Boolean ?: false
                    ))
                } ?: trySend(AppVersionInfo())
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveAppVersion(info: AppVersionInfo) {
        rootRef.collection("data").document("app_version").set(
            mapOf(
                "versionCode" to info.versionCode,
                "versionName" to info.versionName,
                "downloadUrl" to info.downloadUrl,
                "forceUpdate" to info.forceUpdate
            )
        ).await()
    }

    // ── Screen management ─────────────────────────────────────────────────────

    fun observeScreens(): Flow<List<ScreenInfo>> = callbackFlow {
        val reg = screensRef.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { doc ->
                doc.data?.let { d -> mapScreenInfo(doc.id, d) }
            } ?: emptyList()
            trySend(list.sortedBy { it.name.lowercase() })
        }
        awaitClose { reg.remove() }
    }

    suspend fun createScreen(name: String, ownerUserId: String, pairedDeviceId: String = ""): String {
        if (pairedDeviceId.isNotBlank()) {
            val existing = screensRef.whereEqualTo("pairedDeviceId", pairedDeviceId).limit(1).get().await()
            if (!existing.isEmpty) error("This display is already connected to another screen.")
        }
        val screenId = UUID.randomUUID().toString()
        val pairingCode = (100_000..999_999).random().toString()
        screensRef.document(screenId).set(
            mapOf(
                "name" to name,
                "ownerUserId" to ownerUserId,
                "pairingCode" to pairingCode,
                "pairedDeviceId" to pairedDeviceId,
                "lastSeenMs" to if (pairedDeviceId.isNotBlank()) System.currentTimeMillis() else 0L,
                "appVersion" to "",
                "activeTheme" to "night_navy",
                "language" to "en",
                "tableFontScale" to 1.0,
                "hadithInterval" to 30,
                "jamaatCountdownMins" to 3,
                "hijriDateOffset" to 0,
                "mosqueName" to "",
                "mosqueAddress" to "",
                "latitude" to 0.0,
                "longitude" to 0.0,
                "calculationMethod" to "",
                "madhab" to "",
            ) + ScreenLayout().toMap()
        ).await()
        screensRef.document(screenId).collection("data").document("jamaat_times")
            .set(jamaatMap(JamaatTimes())).await()
        return pairingCode
    }

    suspend fun bindDeviceToScreen(screenId: String, deviceId: String) {
        val existing = screensRef.whereEqualTo("pairedDeviceId", deviceId).limit(1).get().await()
        val other = existing.documents.firstOrNull { it.id != screenId }
        if (other != null) error("This display is already connected to another screen.")
        screensRef.document(screenId).update(
            mapOf(
                "pairedDeviceId" to deviceId,
                "lastSeenMs" to System.currentTimeMillis(),
            )
        ).await()
    }

    suspend fun unbindScreen(screenId: String) {
        val newPairingCode = (100_000..999_999).random().toString()
        screensRef.document(screenId).update(
            mapOf(
                "pairedDeviceId" to "",
                "lastSeenMs" to 0L,
                "pairingCode" to newPairingCode,
            )
        ).await()
    }

    suspend fun deleteScreen(screenId: String) {
        val screenDocRef = screensRef.document(screenId)
        // Delete known sub-documents and subcollections (best-effort client-side)
        screenDocRef.collection("data").document("jamaat_times").delete().await()
        val hadiths = screenDocRef.collection("hadiths").get().await()
        hadiths.documents.forEach { it.reference.delete().await() }
        val notices = screenDocRef.collection("notices").get().await()
        notices.documents.forEach { it.reference.delete().await() }
        screenDocRef.delete().await()
    }

    // ── Per-screen config ─────────────────────────────────────────────────────

    fun observeScreenConfig(screenId: String): Flow<ScreenConfig> = callbackFlow {
        val reg = screensRef.document(screenId).addSnapshotListener { snap, _ ->
            snap?.data?.let { d ->
                trySend(ScreenConfig(
                    screenId = screenId,
                    activeTheme = d["activeTheme"] as? String ?: "night_navy",
                    language = d["language"] as? String ?: "en",
                    tableFontScale = (d["tableFontScale"] as? Number)?.toFloat() ?: 1f,
                    hadithInterval = (d["hadithInterval"] as? Number)?.toInt() ?: 30,
                    jamaatCountdownMins = (d["jamaatCountdownMins"] as? Number)?.toInt() ?: 3,
                    hijriDateOffset = (d["hijriDateOffset"] as? Number)?.toInt() ?: 0,
                    mosqueName = d["mosqueName"] as? String ?: "",
                    mosqueAddress = d["mosqueAddress"] as? String ?: "",
                    latitude = (d["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (d["longitude"] as? Number)?.toDouble() ?: 0.0,
                    calculationMethod = d["calculationMethod"] as? String ?: "",
                    madhab = d["madhab"] as? String ?: "",
                    layout = ScreenLayout.fromMap(d),
                ))
            } ?: trySend(ScreenConfig(screenId = screenId))
        }
        awaitClose { reg.remove() }
    }

    suspend fun saveScreenConfig(screenId: String, config: ScreenConfig) {
        screensRef.document(screenId).set(
            mapOf(
                "activeTheme" to config.activeTheme,
                "language" to config.language,
                "tableFontScale" to config.tableFontScale,
                "hadithInterval" to config.hadithInterval,
                "jamaatCountdownMins" to config.jamaatCountdownMins,
                "hijriDateOffset" to config.hijriDateOffset,
                "mosqueName" to config.mosqueName,
                "mosqueAddress" to config.mosqueAddress,
                "latitude" to config.latitude,
                "longitude" to config.longitude,
                "calculationMethod" to config.calculationMethod,
                "madhab" to config.madhab,
            ) + config.layout.toMap(),
            SetOptions.merge()
        ).await()
    }

    suspend fun refreshScreens(): List<ScreenInfo> {
        val snap = screensRef.get().await()
        return snap.documents.mapNotNull { doc ->
            doc.data?.let { d -> mapScreenInfo(doc.id, d) }
        }.sortedBy { it.name.lowercase() }
    }

    // ── Per-screen jamaat ─────────────────────────────────────────────────────

    fun observeScreenJamaat(screenId: String): Flow<JamaatTimes> = callbackFlow {
        val reg = screensRef.document(screenId).collection("data").document("jamaat_times")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d -> trySend(mapJamaat(d)) } ?: trySend(JamaatTimes())
            }
        awaitClose { reg.remove() }
    }

    suspend fun saveScreenJamaat(screenId: String, times: JamaatTimes) {
        screensRef.document(screenId).collection("data").document("jamaat_times")
            .set(jamaatMap(times)).await()
    }

    // ── Per-screen hadiths ────────────────────────────────────────────────────

    fun observeScreenHadiths(screenId: String): Flow<List<Hadith>> = callbackFlow {
        val reg = screensRef.document(screenId).collection("hadiths")
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapHadith(doc.id, it) } } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun addScreenHadith(screenId: String, translation: String, source: String, narrator: String) {
        screensRef.document(screenId).collection("hadiths").add(
            mapOf("translation" to translation, "source" to source, "narrator" to narrator, "active" to true)
        ).await()
    }

    suspend fun updateScreenHadith(screenId: String, id: String, translation: String, source: String, narrator: String, active: Boolean) {
        screensRef.document(screenId).collection("hadiths").document(id).set(
            mapOf("translation" to translation, "source" to source, "narrator" to narrator, "active" to active)
        ).await()
    }

    suspend fun toggleScreenHadithActive(screenId: String, id: String, active: Boolean) {
        screensRef.document(screenId).collection("hadiths").document(id).update("active", active).await()
    }

    suspend fun deleteScreenHadith(screenId: String, id: String) {
        screensRef.document(screenId).collection("hadiths").document(id).delete().await()
    }

    // ── Per-screen notices ────────────────────────────────────────────────────

    fun observeScreenNotices(screenId: String): Flow<List<Notice>> = callbackFlow {
        val reg = screensRef.document(screenId).collection("notices")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull { doc -> doc.data?.let { mapNotice(doc.id, it) } } ?: emptyList()
                trySend(list.sortedBy { it.priority })
            }
        awaitClose { reg.remove() }
    }

    suspend fun addScreenNotice(screenId: String, text: String, priority: Int = 0) {
        screensRef.document(screenId).collection("notices").add(
            mapOf("text" to text, "active" to true, "priority" to priority)
        ).await()
    }

    suspend fun updateScreenNotice(screenId: String, id: String, text: String, active: Boolean, priority: Int) {
        screensRef.document(screenId).collection("notices").document(id).set(
            mapOf("text" to text, "active" to active, "priority" to priority)
        ).await()
    }

    suspend fun deleteScreenNotice(screenId: String, id: String) {
        screensRef.document(screenId).collection("notices").document(id).delete().await()
    }

    // ── App settings ──────────────────────────────────────────────────────────

    private val settingsRef get() = rootRef.collection("data").document("settings")

    fun observeSettings(): Flow<AppSettings> = callbackFlow {
        val reg = settingsRef.addSnapshotListener { snap, _ ->
            trySend(AppSettings(autoApprovalEnabled = snap?.getBoolean("autoApprovalEnabled") ?: false))
        }
        awaitClose { reg.remove() }
    }

    suspend fun saveSettings(settings: AppSettings) {
        settingsRef.set(mapOf("autoApprovalEnabled" to settings.autoApprovalEnabled), SetOptions.merge()).await()
    }

    // ── Pending-user notifications ────────────────────────────────────────────

    private val notificationsRef get() = rootRef.collection("notifications")

    fun observePendingNotifications(): Flow<List<PendingUserNotification>> = callbackFlow {
        val reg = notificationsRef.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.mapNotNull { doc ->
                doc.data?.let { d ->
                    PendingUserNotification(
                        id = doc.id,
                        uid = d["uid"] as? String ?: "",
                        email = d["email"] as? String ?: "",
                        displayName = d["displayName"] as? String ?: "",
                        createdAt = (d["createdAt"] as? Number)?.toLong() ?: 0L,
                        read = d["read"] as? Boolean ?: false
                    )
                }
            }?.sortedByDescending { it.createdAt } ?: emptyList()
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun markNotificationRead(id: String) {
        notificationsRef.document(id).update("read", true).await()
    }

    suspend fun clearReadNotifications() {
        val docs = notificationsRef.whereEqualTo("read", true).get().await()
        docs.documents.forEach { it.reference.delete().await() }
    }

    private suspend fun createPendingNotification(uid: String, email: String, displayName: String) {
        notificationsRef.add(mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "createdAt" to System.currentTimeMillis(),
            "read" to false
        )).await()
    }

    // ── User management ───────────────────────────────────────────────────────

    private val usersRef get() = rootRef.collection("users")
    private val groupsRef get() = rootRef.collection("screenGroups")
    private val invitesRef get() = rootRef.collection("invites")

    suspend fun bootstrapOrFetchUser(
        uid: String,
        email: String,
        displayName: String? = null
    ): MosqueUser = withFirestoreRetry {
        val doc = usersRef.document(uid).get().await()
        if (doc.exists()) return@withFirestoreRetry mapUser(uid, doc.data!!)
        val invite = invitesRef.document(email).get().await()
        val invitedByUserId = invite.getString("invitedByUserId") ?: ""
        val superAdminExists = usersRef
            .whereEqualTo("role", UserRole.SUPER_ADMIN.name)
            .limit(1).get().await().isEmpty.not()
        val autoApproval = settingsRef.get().await().getBoolean("autoApprovalEnabled") ?: false
        val role = if (superAdminExists) UserRole.ADMIN else UserRole.SUPER_ADMIN
        val status = when {
            !superAdminExists -> UserStatus.ACTIVE
            invitedByUserId.isNotBlank() -> UserStatus.ACTIVE
            autoApproval -> UserStatus.ACTIVE
            else -> UserStatus.PENDING
        }
        val name = displayName?.trim()?.takeIf { it.isNotEmpty() }
            ?: email.substringBefore("@").ifBlank { "User" }
        val user = MosqueUser(uid = uid, email = email, displayName = name,
            role = role, status = status, createdAt = System.currentTimeMillis(),
            invitedByUserId = invitedByUserId)
        usersRef.document(uid).set(userMap(user)).await()
        if (invitedByUserId.isNotBlank()) invitesRef.document(email).delete().await()
        if (status == UserStatus.PENDING) runCatching { createPendingNotification(uid, email, name) }
        user
    }

    suspend fun registerUser(uid: String, email: String, displayName: String): MosqueUser = withFirestoreRetry {
        val invite = invitesRef.document(email).get().await()
        val invitedByUserId = invite.getString("invitedByUserId") ?: ""
        val superAdminExists = usersRef
            .whereEqualTo("role", UserRole.SUPER_ADMIN.name)
            .limit(1).get().await().isEmpty.not()
        val autoApproval = settingsRef.get().await().getBoolean("autoApprovalEnabled") ?: false
        val role = if (superAdminExists) UserRole.ADMIN else UserRole.SUPER_ADMIN
        val status = when {
            !superAdminExists -> UserStatus.ACTIVE
            invitedByUserId.isNotBlank() -> UserStatus.ACTIVE
            autoApproval -> UserStatus.ACTIVE
            else -> UserStatus.PENDING
        }
        val user = MosqueUser(uid = uid, email = email, displayName = displayName,
            role = role, status = status, createdAt = System.currentTimeMillis(),
            invitedByUserId = invitedByUserId)
        usersRef.document(uid).set(userMap(user)).await()
        if (invitedByUserId.isNotBlank()) invitesRef.document(email).delete().await()
        if (status == UserStatus.PENDING) runCatching { createPendingNotification(uid, email, displayName) }
        user
    }

    fun observeUserProfile(uid: String): Flow<MosqueUser?> = callbackFlow {
        val reg = usersRef.document(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.data?.let { mapUser(uid, it) })
        }
        awaitClose { reg.remove() }
    }

    fun observeAllUsers(): Flow<List<MosqueUser>> = callbackFlow {
        val reg = usersRef.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapUser(doc.id, it) } }
                ?.sortedBy { it.email } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun updateUserStatus(uid: String, status: UserStatus) {
        usersRef.document(uid).update("status", status.name).await()
    }

    suspend fun updateUserRole(uid: String, role: UserRole) {
        usersRef.document(uid).update("role", role.name).await()
    }

    suspend fun updateUserDisplayName(uid: String, displayName: String) {
        usersRef.document(uid).update("displayName", displayName).await()
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        usersRef.document(uid).update("fcmToken", token).await()
    }

    fun observeUsersInvitedBy(adminUid: String): Flow<List<MosqueUser>> = callbackFlow {
        val reg = usersRef.whereEqualTo("invitedByUserId", adminUid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapUser(doc.id, it) } }
                    ?.sortedBy { it.email } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun observePendingInvites(adminUid: String): Flow<List<String>> = callbackFlow {
        val reg = invitesRef.whereEqualTo("invitedByUserId", adminUid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { it.getString("email") } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun inviteUser(email: String, adminUid: String) {
        invitesRef.document(email).set(mapOf(
            "email" to email,
            "invitedByUserId" to adminUid,
            "createdAt" to System.currentTimeMillis()
        )).await()
    }

    suspend fun cancelInvite(email: String) {
        invitesRef.document(email).delete().await()
    }

    // ── Screen groups ─────────────────────────────────────────────────────────

    fun observeScreenGroups(): Flow<List<ScreenGroup>> = callbackFlow {
        val reg = groupsRef.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapGroup(doc.id, it) } }
                ?.sortedBy { it.name } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun observeScreenGroupsForUser(uid: String): Flow<List<ScreenGroup>> = callbackFlow {
        val reg = groupsRef.whereArrayContains("memberUserIds", uid)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.mapNotNull { doc -> doc.data?.let { mapGroup(doc.id, it) } }
                    ?.sortedBy { it.name } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun createScreenGroup(name: String, screenIds: List<String>, memberUserIds: List<String>, ownerUserId: String): String {
        val groupId = UUID.randomUUID().toString()
        // Auto-include owner in members so they can see their own group
        val members = if (ownerUserId.isNotBlank() && ownerUserId !in memberUserIds)
            memberUserIds + ownerUserId else memberUserIds
        groupsRef.document(groupId).set(mapOf(
            "name" to name,
            "screenIds" to screenIds,
            "memberUserIds" to members,
            "ownerUserId" to ownerUserId
        )).await()
        return groupId
    }

    suspend fun updateScreenGroup(group: ScreenGroup) {
        groupsRef.document(group.groupId).set(mapOf(
            "name" to group.name,
            "screenIds" to group.screenIds,
            "memberUserIds" to group.memberUserIds,
            "ownerUserId" to group.ownerUserId
        )).await()
    }

    suspend fun deleteScreenGroup(groupId: String) {
        groupsRef.document(groupId).delete().await()
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun mapJamaat(d: Map<String, Any>) = JamaatTimes(
        fajr = d["fajr"] as? String ?: "05:45",
        dhuhr = d["dhuhr"] as? String ?: "13:15",
        asr = d["asr"] as? String ?: "17:30",
        maghrib = d["maghrib"] as? String ?: "auto",
        isha = d["isha"] as? String ?: "21:45",
        fajrNote = d["fajrNote"] as? String ?: "",
        dhuhrNote = d["dhuhrNote"] as? String ?: "",
        asrNote = d["asrNote"] as? String ?: "",
        maghribNote = d["maghribNote"] as? String ?: "",
        ishaNote = d["ishaNote"] as? String ?: ""
    )

    private fun jamaatMap(t: JamaatTimes) = mapOf(
        "fajr" to t.fajr, "dhuhr" to t.dhuhr, "asr" to t.asr,
        "maghrib" to t.maghrib, "isha" to t.isha,
        "fajrNote" to t.fajrNote, "dhuhrNote" to t.dhuhrNote,
        "asrNote" to t.asrNote, "maghribNote" to t.maghribNote, "ishaNote" to t.ishaNote
    )

    private fun mapHadith(id: String, d: Map<String, Any>) = Hadith(
        id = id,
        translation = d["translation"] as? String ?: "",
        source = d["source"] as? String ?: "",
        narrator = d["narrator"] as? String ?: "",
        active = d["active"] as? Boolean ?: true
    )

    private fun mapScreenInfo(id: String, d: Map<String, Any>) = ScreenInfo(
        screenId = id,
        name = d["name"] as? String ?: "",
        ownerUserId = d["ownerUserId"] as? String ?: "",
        pairingCode = d["pairingCode"] as? String ?: "",
        pairedDeviceId = d["pairedDeviceId"] as? String ?: "",
        lastSeenMs = firestoreEpochMs(d["lastSeenMs"]),
        appVersion = d["appVersion"] as? String ?: "",
        displayWidthDp = (d["displayWidthDp"] as? Number)?.toFloat() ?: 0f,
        displayHeightDp = (d["displayHeightDp"] as? Number)?.toFloat() ?: 0f,
        displayWidthPx = (d["displayWidthPx"] as? Number)?.toInt() ?: 0,
        displayHeightPx = (d["displayHeightPx"] as? Number)?.toInt() ?: 0,
        displayDensity = (d["displayDensity"] as? Number)?.toFloat() ?: 0f,
        displayFontScale = (d["displayFontScale"] as? Number)?.toFloat() ?: 0f,
    )

    private fun mapNotice(id: String, d: Map<String, Any>) = Notice(
        id = id,
        text = d["text"] as? String ?: "",
        active = d["active"] as? Boolean ?: true,
        priority = (d["priority"] as? Number)?.toInt() ?: 0
    )

    private fun mapUser(uid: String, d: Map<String, Any>) = MosqueUser(
        uid = uid,
        email = d["email"] as? String ?: "",
        displayName = d["displayName"] as? String ?: "",
        role = runCatching { UserRole.valueOf(d["role"] as? String ?: "") }.getOrDefault(UserRole.ADMIN),
        status = runCatching { UserStatus.valueOf(d["status"] as? String ?: "") }.getOrDefault(UserStatus.PENDING),
        createdAt = (d["createdAt"] as? Number)?.toLong() ?: 0L,
        invitedByUserId = d["invitedByUserId"] as? String ?: ""
    )

    @Suppress("UNCHECKED_CAST")
    private fun mapGroup(id: String, d: Map<String, Any>) = ScreenGroup(
        groupId = id,
        name = d["name"] as? String ?: "",
        screenIds = (d["screenIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        memberUserIds = (d["memberUserIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        ownerUserId = d["ownerUserId"] as? String ?: ""
    )

    private fun userMap(u: MosqueUser) = mapOf(
        "email" to u.email,
        "displayName" to u.displayName,
        "role" to u.role.name,
        "status" to u.status.name,
        "createdAt" to u.createdAt,
        "invitedByUserId" to u.invitedByUserId
    )
}
