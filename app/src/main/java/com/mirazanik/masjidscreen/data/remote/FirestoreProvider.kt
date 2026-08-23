package com.mirazanik.masjidscreen.data.remote

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/**
 * Single configured Firestore handle for the whole app. Settings can only be applied before the
 * first Firestore call, so every repository must go through this instead of [Firebase.firestore].
 */
object FirestoreProvider {

    private const val TAG = "FirestoreProvider"

    val instance: FirebaseFirestore by lazy {
        Firebase.firestore.apply {
            runCatching {
                firestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                        PersistentCacheSettings.newBuilder()
                            .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build()
                    )
                    .build()
            }.onFailure { Log.w(TAG, "Firestore settings already applied", it) }
        }
    }

    /** Forces the client to drop and rebuild its backend connection. */
    suspend fun reconnect() {
        runCatching {
            instance.disableNetwork().await()
            instance.enableNetwork().await()
        }.onFailure { Log.w(TAG, "Firestore reconnect failed", it) }
    }
}

private val TRANSIENT_CODES = setOf(
    FirebaseFirestoreException.Code.UNAVAILABLE,
    FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
    FirebaseFirestoreException.Code.ABORTED,
    FirebaseFirestoreException.Code.INTERNAL,
    FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED
)

fun Throwable.isTransientFirestoreError(): Boolean =
    this is FirebaseFirestoreException && code in TRANSIENT_CODES

/**
 * Runs [block], retrying transient backend failures. Firestore reports
 * "Failed to get document because the client is offline" as [FirebaseFirestoreException.Code.UNAVAILABLE]
 * whenever its connection is not up yet and the document has never been cached, which is common right
 * after sign-in on a device that just joined the network.
 */
suspend fun <T> withFirestoreRetry(
    attempts: Int = 4,
    initialDelayMs: Long = 400L,
    block: suspend () -> T
): T {
    var delayMs = initialDelayMs
    var lastError: Throwable? = null
    repeat(attempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            if (!e.isTransientFirestoreError() || attempt == attempts - 1) throw e
            lastError = e
            // The first retry is usually enough for a slow connection; after that assume the
            // client's network state is stale and force it back online.
            if (attempt > 0) FirestoreProvider.reconnect()
            delay(delayMs)
            delayMs = (delayMs * 2).coerceAtMost(4_000L)
        }
    }
    throw lastError ?: IllegalStateException("withFirestoreRetry exhausted without a result")
}

fun firestoreEpochMs(value: Any?): Long = when (value) {
    is Number -> value.toLong()
    is Timestamp -> value.toDate().time
    else -> 0L
}
