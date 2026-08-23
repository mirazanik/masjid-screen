package com.mirazanik.masjidscreen

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.FirebaseApp
import com.mirazanik.masjidscreen.admin.viewmodel.AdminPanelViewModel
import com.mirazanik.masjidscreen.data.remote.FirestoreProvider
import com.mirazanik.masjidscreen.util.DeviceManager
import com.mirazanik.masjidscreen.util.NtpTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MosqueApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var deviceId: String = ""
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Touch Firestore first so cache settings are applied before any repository query.
        FirestoreProvider.instance
        createNotificationChannels()
        applicationScope.launch {
            deviceId = DeviceManager.getOrCreateDeviceId(this@MosqueApplication)
        }
        startTimeSync()
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            AdminPanelViewModel.NOTIF_CHANNEL_ID,
            "Pending User Approvals",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "New users awaiting admin approval" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun startTimeSync() {
        applicationScope.launch {
            while (true) {
                NtpTimeProvider.sync()
                delay(6 * 60 * 60 * 1000L)
            }
        }
    }
}
