package com.mirazanik.masjidscreen.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mirazanik.masjidscreen.data.local.AppDatabase
import com.mirazanik.masjidscreen.data.local.entity.MosqueConfigEntity
import com.mirazanik.masjidscreen.data.model.*
import com.mirazanik.masjidscreen.data.remote.FirestoreRepository
import com.mirazanik.masjidscreen.prayer.PrayerCalculator
import com.mirazanik.masjidscreen.ui.theme.AppTheme
import com.mirazanik.masjidscreen.util.DeviceManager
import com.mirazanik.masjidscreen.util.NtpTimeProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

data class DisplayState(
    val currentTime: LocalTime = NtpTimeProvider.nowInBangladesh().toLocalTime(),
    val currentDate: LocalDate = NtpTimeProvider.nowInBangladesh().toLocalDate(),
    val hijriDate: HijriDate = HijriDate(),
    val prayerTimes: PrayerTimesData = PrayerTimesData(),
    val jamaatTimes: JamaatTimes = JamaatTimes(),
    val nextPrayerName: String = "",
    val nextPrayerMinsLeft: Long = 0,
    val currentPrayerName: String = "",
    val currentPrayerMinsLeft: Long = 0,
    val hadiths: List<Hadith> = emptyList(),
    val currentHadithIndex: Int = 0,
    val notices: List<Notice> = emptyList(),
    val mosqueConfig: MosqueConfig = MosqueConfig(),
    val isNetworkAvailable: Boolean = true,
    val activeTheme: AppTheme = AppTheme.NIGHT_NAVY,
    val tableFontScale: Float = 1f,
    val layout: ScreenLayout = ScreenLayout(),
)

sealed class ScreenPairingState {
    object Loading : ScreenPairingState()
    object Unpaired : ScreenPairingState()
    data class Pairing(val code: String) : ScreenPairingState()
    data class Paired(val screenId: String) : ScreenPairingState()
    data class PairingError(val message: String) : ScreenPairingState()
}

class DisplayViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private var firestoreRepo: FirestoreRepository? = null
    private var currentDeviceId: String = ""
    private var displayObserving = false
    private var incomingPairJob: Job? = null

    private val _deviceId = MutableStateFlow("")
    val deviceId: StateFlow<String> = _deviceId.asStateFlow()

    private val _pairingState = MutableStateFlow<ScreenPairingState>(ScreenPairingState.Loading)
    val pairingState: StateFlow<ScreenPairingState> = _pairingState.asStateFlow()

    private val _unpairError = MutableStateFlow<String?>(null)
    val unpairError: StateFlow<String?> = _unpairError.asStateFlow()

    private val _state = MutableStateFlow(DisplayState())
    val state: StateFlow<DisplayState> = _state.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private val authenticationReady = CompletableDeferred<Unit>()

    init {
        val app = getApplication<Application>()
        viewModelScope.launch {
            // Remote sync requires authentication, but local startup should never wait for the network.
            if (Firebase.auth.currentUser == null) {
                withContext(Dispatchers.IO) {
                    runCatching { Firebase.auth.signInAnonymously().await() }
                        .onFailure { Log.w("DisplayViewModel", "Anonymous auth failed: ${it.message}") }
                }
            }
            authenticationReady.complete(Unit)
        }
        viewModelScope.launch {
            currentDeviceId = DeviceManager.getOrCreateDeviceId(app)
            _deviceId.value = currentDeviceId
            DeviceManager.screenIdFlow(app).collect { screenId ->
                if (screenId != null && !displayObserving) {
                    incomingPairJob?.cancel()
                    displayObserving = true
                    firestoreRepo = FirestoreRepository(db, currentDeviceId, screenId).also { repo ->
                        seedDisplaySize(repo)
                    }
                    observeDatabase(screenId)
                    _pairingState.update { ScreenPairingState.Paired(screenId) }
                    launch {
                        authenticationReady.await()
                        firestoreRepo?.startListening()
                    }
                } else if (screenId == null) {
                    if (_pairingState.value !is ScreenPairingState.Pairing) {
                        _pairingState.update { ScreenPairingState.Unpaired }
                    }
                    startIncomingPairListener()
                }
            }
        }
        startClock()
        observeNetwork()
    }

    override fun onCleared() {
        incomingPairJob?.cancel()
        super.onCleared()
        networkCallback?.let {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }
    }

    fun pairToScreen(code: String) {
        viewModelScope.launch {
            _pairingState.update { ScreenPairingState.Pairing(code) }
            authenticationReady.await()
            val tempRepo = FirestoreRepository(db, currentDeviceId, null)
            tempRepo.pairToScreen(code).fold(
                onSuccess = { screenId ->
                    DeviceManager.saveScreenId(getApplication(), screenId)
                    // screenIdFlow emits new value → init collector starts the repo
                },
                onFailure = { e ->
                    _pairingState.update { ScreenPairingState.PairingError(e.message ?: "Unknown error") }
                }
            )
        }
    }

    fun clearPairingError() {
        _pairingState.update { ScreenPairingState.Unpaired }
    }

    fun unpairFromThisDevice() {
        viewModelScope.launch {
            _unpairError.value = null
            runCatching { firestoreRepo?.unpairThisDevice() ?: error("This display is not paired.") }
                .onSuccess { onUnbound() }
                .onFailure { e ->
                    Log.w("DisplayViewModel", "Unpair failed: ${e.message}")
                    _unpairError.value = e.message ?: "Could not unpair this display"
                }
        }
    }

    fun clearUnpairError() {
        _unpairError.value = null
    }

    fun reportDisplaySize(widthPx: Int, heightPx: Int, density: Float, fontScale: Float) {
        firestoreRepo?.setDisplaySize(widthPx, heightPx, density, fontScale)
    }

    private fun seedDisplaySize(repo: FirestoreRepository) {
        val app = getApplication<Application>()
        val dm = app.resources.displayMetrics
        val fontScale = app.resources.configuration.fontScale
        val w = dm.widthPixels
        val h = dm.heightPixels
        repo.setDisplaySize(w, h, dm.density, fontScale)
    }

    private fun startIncomingPairListener() {
        if (currentDeviceId.isBlank() || incomingPairJob?.isActive == true) return
        incomingPairJob = viewModelScope.launch {
            authenticationReady.await()
            val tempRepo = FirestoreRepository(db, currentDeviceId, null)
            try {
                tempRepo.observeIncomingPair(currentDeviceId).collect { screenId ->
                    DeviceManager.saveScreenId(getApplication(), screenId)
                }
            } finally {
                tempRepo.cancel()
            }
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            launch {
                NtpTimeProvider.sync()
                refreshPrayerTimes()
            }
            while (true) {
                val bdNow = NtpTimeProvider.nowInBangladesh()
                _state.update { it.copy(currentTime = bdNow.toLocalTime(), currentDate = bdNow.toLocalDate()) }
                if (bdNow.hour == 0 && bdNow.minute == 0 && bdNow.second == 0) {
                    refreshPrayerTimes()
                }
                delay(1000)
            }
        }
    }

    private fun observeNetwork() {
        val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val initial = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _state.update { it.copy(isNetworkAvailable = initial) }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _state.update { it.copy(isNetworkAvailable = true) }
                viewModelScope.launch {
                    NtpTimeProvider.sync()
                    refreshPrayerTimes()
                }
            }
            override fun onLost(network: Network) {
                _state.update { it.copy(isNetworkAvailable = false) }
            }
        }
        networkCallback = callback
        cm.registerDefaultNetworkCallback(callback)
    }

    private fun observeDatabase(screenId: String) {
        // Merged config: global MosqueConfig (identity + prayer calc) + ScreenInfoEntity (display settings)
        viewModelScope.launch {
            // Prefer Firestore direct flow (immediate) over Room (startup cache)
            val screenInfoSource = firestoreRepo!!.screenInfoDirect
                .combine(db.screenInfoDao().observe(screenId)) { direct, cached -> direct ?: cached }
            val globalConfigSource = db.mosqueConfigDao().observe()
                .map { it ?: MosqueConfigEntity() }
            combine(
                globalConfigSource,
                screenInfoSource
            ) { global, screen ->
                Pair(global, screen)
            }.collect { (global, screen) ->
                val config = MosqueConfig(
                    id = global.id,
                    name = screen?.mosqueName?.takeIf { it.isNotBlank() } ?: global.name,
                    address = screen?.mosqueAddress?.takeIf { it.isNotBlank() } ?: global.address,
                    latitude = screen?.latitude?.takeIf { it != 0.0 } ?: global.latitude,
                    longitude = screen?.longitude?.takeIf { it != 0.0 } ?: global.longitude,
                    calculationMethod = screen?.calculationMethod?.takeIf { it.isNotBlank() } ?: global.calculationMethod,
                    madhab = screen?.madhab?.takeIf { it.isNotBlank() } ?: global.madhab,
                    language = screen?.language ?: global.language,
                    hadithInterval = screen?.hadithInterval ?: global.hadithInterval,
                    jamaatCountdownMins = screen?.jamaatCountdownMins ?: global.jamaatCountdownMins,
                    hijriDateOffset = screen?.hijriDateOffset ?: global.hijriDateOffset,
                    tableFontScale = screen?.tableFontScale ?: global.tableFontScale,
                    activeTheme = screen?.activeTheme ?: global.activeTheme
                )
                val layout = ScreenLayout(
                    headerScale = screen?.headerScale ?: ScreenLayout.DEFAULT_HEADER,
                    clockScale = screen?.clockScale ?: ScreenLayout.DEFAULT_CLOCK,
                    sunGridScale = screen?.sunGridScale ?: ScreenLayout.DEFAULT_SUN,
                    clockWidthFraction = screen?.clockWidthFraction ?: ScreenLayout.DEFAULT_CLOCK_WIDTH,
                    mainRowWeight = screen?.mainRowWeight ?: ScreenLayout.DEFAULT_MAIN_ROW,
                    noticeHeightFraction = screen?.noticeHeightFraction ?: ScreenLayout.DEFAULT_NOTICE,
                    jamaatColPrayer = screen?.jamaatColPrayer ?: ScreenLayout.DEFAULT_COL_PRAYER,
                    jamaatColWaqtStart = screen?.jamaatColWaqtStart ?: ScreenLayout.DEFAULT_COL_WAQT_START,
                    jamaatColWaqtEnd = screen?.jamaatColWaqtEnd ?: ScreenLayout.DEFAULT_COL_WAQT_END,
                    jamaatColJamaat = screen?.jamaatColJamaat ?: ScreenLayout.DEFAULT_COL_JAMAAT,
                ).coerced()
                _state.update {
                    it.copy(
                        mosqueConfig = config,
                        tableFontScale = config.tableFontScale,
                        activeTheme = AppTheme.fromKey(config.activeTheme),
                        layout = layout,
                    )
                }
                refreshPrayerTimes()
            }
        }
        viewModelScope.launch {
            db.jamaatTimesDao().observe().filterNotNull().collect { entity ->
                val jamaat = JamaatTimes(
                    fajr = entity.fajr,
                    dhuhr = entity.dhuhr,
                    asr = entity.asr,
                    maghrib = entity.maghrib,
                    isha = entity.isha,
                    fajrNote = entity.fajrNote,
                    dhuhrNote = entity.dhuhrNote,
                    asrNote = entity.asrNote,
                    maghribNote = entity.maghribNote,
                    ishaNote = entity.ishaNote
                )
                _state.update { it.copy(jamaatTimes = jamaat) }
            }
        }
        viewModelScope.launch {
            db.hadithDao().observeActive().collect { entities ->
                _state.update {
                    it.copy(hadiths = entities.map { e -> Hadith(e.id, e.translation, e.source, e.narrator, e.active) })
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                val intervalMs = _state.value.mosqueConfig.hadithInterval.coerceAtLeast(5) * 1000L
                delay(intervalMs)
                _state.update { s ->
                    if (s.hadiths.size > 1)
                        s.copy(currentHadithIndex = (s.currentHadithIndex + 1) % s.hadiths.size)
                    else s
                }
            }
        }
        viewModelScope.launch {
            db.noticeDao().observeActive().collect { entities ->
                _state.update {
                    it.copy(notices = entities.map { e -> Notice(e.id, e.text, e.active, e.priority) })
                }
            }
        }
        viewModelScope.launch {
            while (true) {
                updatePrayerCountdown()
                delay(30_000)
            }
        }
        viewModelScope.launch {
            firestoreRepo!!.screenInfoDirect
                .filterNotNull()
                .dropWhile { it.pairedDeviceId != currentDeviceId }
                .filter { it.pairedDeviceId != currentDeviceId }
                .first()
            onUnbound()
        }
    }

    private fun onUnbound() {
        viewModelScope.launch {
            firestoreRepo?.cancel()
            firestoreRepo = null
            displayObserving = false
            DeviceManager.clearScreenId(getApplication())
            _pairingState.update { ScreenPairingState.Unpaired }
            startIncomingPairListener()
        }
    }

    private fun refreshPrayerTimes() {
        val config = _state.value.mosqueConfig
        val times = PrayerCalculator.calculate(
            config.latitude, config.longitude, config.calculationMethod, config.madhab
        )
        val hijri = PrayerCalculator.getHijriDate(
            offsetDays = config.hijriDateOffset,
            maghribTime = times.maghrib
        )
        _state.update { it.copy(prayerTimes = times, hijriDate = hijri) }
        updatePrayerCountdown()
    }

    private fun updatePrayerCountdown() {
        val current = _state.value
        val (name, mins) = PrayerCalculator.getNextPrayer(
            current.prayerTimes,
            current.jamaatTimes.fajr, current.jamaatTimes.dhuhr,
            current.jamaatTimes.asr, current.jamaatTimes.maghrib,
            current.jamaatTimes.isha
        )
        val (curName, curMins) = PrayerCalculator.getCurrentPrayer(current.prayerTimes)
        val hijri = PrayerCalculator.getHijriDate(
            offsetDays = current.mosqueConfig.hijriDateOffset,
            maghribTime = current.prayerTimes.maghrib
        )
        _state.update {
            it.copy(
                nextPrayerName = name, nextPrayerMinsLeft = mins,
                currentPrayerName = curName, currentPrayerMinsLeft = curMins,
                hijriDate = hijri
            )
        }
    }
}
