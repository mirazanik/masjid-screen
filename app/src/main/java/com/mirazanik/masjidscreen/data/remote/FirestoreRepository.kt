package com.mirazanik.masjidscreen.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mirazanik.masjidscreen.data.local.AppDatabase
import com.mirazanik.masjidscreen.data.local.entity.*
import com.mirazanik.masjidscreen.util.DeviceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AppVersionInfo(
    val versionCode: Int = 0,
    val versionName: String = "",
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false
)

class FirestoreRepository(
    private val db: AppDatabase,
    private val deviceId: String,
    private val screenId: String?
) {

    private val firestore: FirebaseFirestore = FirestoreProvider.instance
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mosqueId = "default"

    private val rootRef get() = firestore.collection("mosques").document(mosqueId)
    private val screensRef get() = rootRef.collection("screens")

    private val _appVersionInfo = MutableStateFlow<AppVersionInfo?>(null)
    val appVersionInfo: StateFlow<AppVersionInfo?> = _appVersionInfo

    private val _screenInfoDirect = MutableStateFlow<ScreenInfoEntity?>(null)
    val screenInfoDirect: StateFlow<ScreenInfoEntity?> = _screenInfoDirect

    fun cancel() {
        scope.cancel()
    }

    fun startListening() {
        listenToGlobalConfig()
        listenToAppVersion()
        if (screenId != null) {
            listenToScreenInfo()
            listenToScreenJamaat()
            listenToScreenHadiths()
            listenToScreenNotices()
            startHeartbeat()
        } else {
            listenToGlobalJamaat()
            listenToGlobalHadiths()
            listenToGlobalNotices()
        }
    }

    // ── Global listeners ──────────────────────────────────────────────────────

    private fun listenToGlobalConfig() {
        rootRef.collection("data").document("config")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d ->
                    scope.launch {
                        db.mosqueConfigDao().upsert(
                            MosqueConfigEntity(
                                id = "default",
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
                            )
                        )
                    }
                }
            }
    }

    private fun listenToGlobalJamaat() {
        rootRef.collection("data").document("jamaat_times")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d -> scope.launch { db.jamaatTimesDao().upsert(mapJamaat(d)) } }
            }
    }

    private fun listenToGlobalHadiths() {
        rootRef.collection("hadiths")
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    val entities = qs.documents.mapNotNull { doc ->
                        doc.data?.let { d -> mapHadith(doc.id, d) }
                    }
                    scope.launch {
                        db.hadithDao().deleteAll()
                        db.hadithDao().upsertAll(entities)
                    }
                }
            }
    }

    private fun listenToGlobalNotices() {
        rootRef.collection("notices")
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    val entities = qs.documents.mapNotNull { doc ->
                        doc.data?.let { d -> mapNotice(doc.id, d) }
                    }
                    scope.launch {
                        db.noticeDao().deleteAll()
                        db.noticeDao().upsertAll(entities)
                    }
                }
            }
    }

    private fun listenToAppVersion() {
        rootRef.collection("data").document("app_version")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d ->
                    _appVersionInfo.value = AppVersionInfo(
                        versionCode = (d["versionCode"] as? Number)?.toInt() ?: 0,
                        versionName = d["versionName"] as? String ?: "",
                        downloadUrl = d["downloadUrl"] as? String ?: "",
                        forceUpdate = d["forceUpdate"] as? Boolean ?: false
                    )
                }
            }
    }

    // ── Screen-specific listeners ─────────────────────────────────────────────

    private fun listenToScreenInfo() {
        val sid = screenId ?: return
        screensRef.document(sid)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("FirestoreRepo", "listenToScreenInfo error for $sid: ${error.message}")
                    return@addSnapshotListener
                }
                snap?.data?.let { d ->
                    val entity = ScreenInfoEntity(
                        screenId = sid,
                        name = d["name"] as? String ?: "",
                        pairingCode = d["pairingCode"] as? String ?: "",
                        pairedDeviceId = d["pairedDeviceId"] as? String ?: "",
                        lastSeenMs = firestoreEpochMs(d["lastSeenMs"]),
                        appVersion = d["appVersion"] as? String ?: "",
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
                        headerScale = (d["headerScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 1f,
                        clockScale = (d["clockScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 1f,
                        sunGridScale = (d["sunGridScale"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 1f,
                        clockWidthFraction = (d["clockWidthFraction"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.40f,
                        mainRowWeight = (d["mainRowWeight"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.75f,
                        noticeHeightFraction = (d["noticeHeightFraction"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.20f,
                        jamaatColPrayer = (d["jamaatColPrayer"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.20f,
                        jamaatColWaqtStart = (d["jamaatColWaqtStart"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.25f,
                        jamaatColWaqtEnd = (d["jamaatColWaqtEnd"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.25f,
                        jamaatColJamaat = (d["jamaatColJamaat"] as? Number)?.toFloat()?.takeIf { it > 0f } ?: 0.30f,
                    )
                    // Update directly on main thread for immediate display refresh
                    _screenInfoDirect.value = entity
                    scope.launch { db.screenInfoDao().upsert(entity) }
                }
            }
    }

    private fun listenToScreenJamaat() {
        val sid = screenId ?: return
        screensRef.document(sid).collection("data").document("jamaat_times")
            .addSnapshotListener { snap, _ ->
                snap?.data?.let { d -> scope.launch { db.jamaatTimesDao().upsert(mapJamaat(d)) } }
            }
    }

    private fun listenToScreenHadiths() {
        val sid = screenId ?: return
        screensRef.document(sid).collection("hadiths")
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    val entities = qs.documents.mapNotNull { doc ->
                        doc.data?.let { d -> mapHadith(doc.id, d) }
                    }
                    scope.launch {
                        db.hadithDao().deleteAll()
                        db.hadithDao().upsertAll(entities)
                    }
                }
            }
    }

    private fun listenToScreenNotices() {
        val sid = screenId ?: return
        screensRef.document(sid).collection("notices")
            .addSnapshotListener { snap, _ ->
                snap?.let { qs ->
                    val entities = qs.documents.mapNotNull { doc ->
                        doc.data?.let { d -> mapNotice(doc.id, d) }
                    }
                    scope.launch {
                        db.noticeDao().deleteAll()
                        db.noticeDao().upsertAll(entities)
                    }
                }
            }
    }

    private var displayWidthPx = 0
    private var displayHeightPx = 0
    private var displayDensity = 0f
    private var displayFontScale = 0f
    private var canPushHeartbeat = false

    fun setDisplaySize(widthPx: Int, heightPx: Int, density: Float, fontScale: Float) {
        if (widthPx <= 80 || heightPx <= 80 || density <= 0.1f) return
        val (w, h) = if (widthPx >= heightPx) widthPx to heightPx else heightPx to widthPx
        val d = density
        val fs = fontScale.coerceAtLeast(0.1f)
        val changed = displayWidthPx != w || displayHeightPx != h ||
            displayDensity != d || displayFontScale != fs
        displayWidthPx = w
        displayHeightPx = h
        displayDensity = d
        displayFontScale = fs
        if (changed && canPushHeartbeat) {
            scope.launch { pushDisplaySize() }
        }
    }

    private fun startHeartbeat() {
        val sid = screenId ?: return
        canPushHeartbeat = true
        scope.launch {
            while (true) {
                pushLastSeen()
                pushDisplaySize()
                delay(15_000L)
            }
        }
    }

    private suspend fun pushLastSeen(): Boolean {
        val sid = screenId ?: return false
        return runCatching {
            screensRef.document(sid).update(
                mapOf(
                    "lastSeenMs" to System.currentTimeMillis(),
                    "appVersion" to DeviceManager.appVersionName()
                )
            ).await()
        }.onFailure {
            Log.w("FirestoreRepo", "heartbeat lastSeen failed: ${it.message}")
        }.isSuccess
    }

    private suspend fun pushDisplaySize(): Boolean {
        val sid = screenId ?: return false
        if (displayWidthPx <= 80 || displayHeightPx <= 80 || displayDensity <= 0.1f) return false
        return runCatching {
            screensRef.document(sid).update(
                mapOf(
                    "displayWidthPx" to displayWidthPx,
                    "displayHeightPx" to displayHeightPx,
                    "displayDensity" to displayDensity,
                    "displayFontScale" to displayFontScale,
                    "displayWidthDp" to displayWidthPx / displayDensity,
                    "displayHeightDp" to displayHeightPx / displayDensity,
                )
            ).await()
        }.onFailure {
            Log.w("FirestoreRepo", "heartbeat display size failed: ${it.message}")
        }.isSuccess
    }

    // ── Pairing ───────────────────────────────────────────────────────────────

    fun observeIncomingPair(deviceId: String): Flow<String> = callbackFlow {
        if (deviceId.isBlank()) {
            awaitClose { }
            return@callbackFlow
        }
        val reg = screensRef.whereEqualTo("pairedDeviceId", deviceId)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("FirestoreRepo", "observeIncomingPair: ${error.message}")
                    return@addSnapshotListener
                }
                snap?.documents?.firstOrNull()?.id?.let { trySend(it) }
            }
        awaitClose { reg.remove() }
    }

    suspend fun pairToScreen(pairingCode: String): Result<String> = runCatching {
        val query = screensRef.whereEqualTo("pairingCode", pairingCode).get().await()
        val doc = query.documents.firstOrNull()
            ?: error("Invalid pairing code. Please check and try again.")
        val sid = doc.id
        screensRef.document(sid).set(
            mapOf(
                "pairedDeviceId" to deviceId,
                "appVersion" to DeviceManager.appVersionName(),
                "lastSeenMs" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
        sid
    }

    suspend fun unpairThisDevice() {
        val sid = screenId ?: error("This display is not paired.")
        screensRef.document(sid).update(
            mapOf(
                "pairedDeviceId" to "",
                "lastSeenMs" to 0L,
            )
        ).await()
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private fun mapJamaat(d: Map<String, Any>) = JamaatTimesEntity(
        id = "default",
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

    private fun mapHadith(id: String, d: Map<String, Any>) = HadithEntity(
        id = id,
        translation = d["translation"] as? String ?: "",
        source = d["source"] as? String ?: "",
        narrator = d["narrator"] as? String ?: "",
        active = d["active"] as? Boolean ?: true
    )

    private fun mapNotice(id: String, d: Map<String, Any>) = NoticeEntity(
        id = id,
        text = d["text"] as? String ?: "",
        active = d["active"] as? Boolean ?: true,
        priority = (d["priority"] as? Number)?.toInt() ?: 0
    )
}
