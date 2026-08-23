package com.mirazanik.masjidscreen

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MosqueFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Firebase messaging token refreshed")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Firebase message received from ${message.from.orEmpty()}")
    }

    private companion object {
        private const val TAG = "MosqueFcmService"
    }
}
