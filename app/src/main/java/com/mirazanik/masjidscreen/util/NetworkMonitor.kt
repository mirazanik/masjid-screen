package com.mirazanik.masjidscreen.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

object NetworkMonitor {

    fun isOnline(context: Context): Boolean {
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Suspends until a validated network is available. Returns false if none appears in time,
     * which happens on set-top boxes that launch the app before Wi-Fi has associated.
     */
    suspend fun awaitOnline(context: Context, timeoutMs: Long = 15_000): Boolean {
        if (isOnline(context)) return true
        val cm = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        var callback: ConnectivityManager.NetworkCallback? = null
        return try {
            withTimeout(timeoutMs) {
                suspendCancellableCoroutine { cont ->
                    val resumed = AtomicBoolean(false)
                    val cb = object : ConnectivityManager.NetworkCallback() {
                        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                            if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                                resumed.compareAndSet(false, true)
                            ) {
                                cont.resume(true)
                            }
                        }
                    }
                    callback = cb
                    cm.registerNetworkCallback(
                        NetworkRequest.Builder()
                            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build(),
                        cb
                    )
                    if (isOnline(context) && resumed.compareAndSet(false, true)) cont.resume(true)
                }
            }
        } catch (_: TimeoutCancellationException) {
            false
        } finally {
            callback?.let { runCatching { cm.unregisterNetworkCallback(it) } }
        }
    }
}
