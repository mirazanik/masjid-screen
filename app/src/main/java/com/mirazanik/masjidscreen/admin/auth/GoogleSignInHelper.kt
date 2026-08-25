package com.mirazanik.masjidscreen.admin.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCustomCredentialOption
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mirazanik.masjidscreen.BuildConfig
import com.mirazanik.masjidscreen.R
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleSignInHelper {
    const val NOT_CONFIGURED_MESSAGE =
        "Google Sign-In is not configured. Enable Google in Firebase Authentication, " +
            "add this app's SHA-1 fingerprint, then replace " +
            "app/src/dev/google-services.json or app/src/prod/google-services.json."

    private const val PLAY_SIGNING_MESSAGE =
        "Google Sign-In didn't finish after you picked an account. Play Store builds " +
            "use Google's app-signing key. In Play Console copy App signing SHA-1 and " +
            "SHA-256, add both in Firebase (Project settings → Your apps), wait a few " +
            "minutes, then try again."

    suspend fun getIdToken(activity: Activity): String {
        val webClientId = resolveWebClientId(activity)
            ?: error(NOT_CONFIGURED_MESSAGE)
        val credentialManager = CredentialManager.create(activity)
        val nonce = newNonce()
        return try {
            requestIdToken(
                activity,
                credentialManager,
                GetSignInWithGoogleOption.Builder(webClientId)
                    .setNonce(nonce)
                    .build()
            )
        } catch (e: GetCredentialCancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            if (isPlaySigningMismatch(e)) throw e
            try {
                requestIdToken(
                    activity,
                    credentialManager,
                    GetGoogleIdOption.Builder()
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setNonce(nonce)
                        .build()
                )
            } catch (retry: GetCredentialException) {
                throw if (isPlaySigningMismatch(retry)) retry else e
            }
        }
    }

    private suspend fun requestIdToken(
        activity: Activity,
        credentialManager: CredentialManager,
        option: GetCustomCredentialOption
    ): String {
        if (activity.isFinishing || activity.isDestroyed) {
            error("Sign-in window closed. Try again.")
        }
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = withContext(Dispatchers.Main.immediate) {
            credentialManager.getCredential(activity, request)
        }
        return extractIdToken(result.credential)
    }

    private fun extractIdToken(credential: Credential): String {
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return GoogleIdTokenCredential.createFrom(credential.data).idToken
        }
        error("Google Sign-In returned an unexpected credential type")
    }

    fun resolveWebClientId(context: Context): String? {
        BuildConfig.GOOGLE_WEB_CLIENT_ID.trim().takeIf { it.isNotEmpty() }?.let { return it }
        // Use R.string (namespace), not getIdentifier(packageName). Dev builds use
        // applicationId com.mirazanik.masjidscreen.dev, so a packageName lookup misses
        // the resource. Release shrinking also strips dynamically looked-up strings.
        return runCatching {
            context.getString(R.string.default_web_client_id).trim().takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    fun userMessage(error: Throwable): String = when {
        error is NoCredentialException -> "No Google account found on this device."
        isPlaySigningMismatch(error) -> PLAY_SIGNING_MESSAGE
        error is GetCredentialException ->
            error.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                ?: "Google Sign-In failed. Try again."
        else -> error.message?.takeIf { it.isNotBlank() } ?: "Google Sign-In failed"
    }

    private fun isPlaySigningMismatch(error: Throwable): Boolean {
        val text = buildString {
            var current: Throwable? = error
            while (current != null) {
                append(current.message.orEmpty())
                append(' ')
                append(current.toString())
                append(' ')
                current = current.cause
            }
        }.lowercase()
        return "developer console" in text ||
            "not set up correctly" in text ||
            "[10]" in text ||
            " 10:" in text ||
            ": 10:" in text ||
            "[16]" in text ||
            " 16:" in text ||
            "reauth failed" in text
    }

    private fun newNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
