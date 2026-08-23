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

object GoogleSignInHelper {
    const val NOT_CONFIGURED_MESSAGE =
        "Google Sign-In is not configured. Enable Google in Firebase Authentication, " +
            "add this app's SHA-1 fingerprint, then replace " +
            "app/src/dev/google-services.json or app/src/prod/google-services.json."

    suspend fun getIdToken(activity: Activity): String {
        val webClientId = resolveWebClientId(activity)
            ?: error(NOT_CONFIGURED_MESSAGE)
        val credentialManager = CredentialManager.create(activity)
        return try {
            requestIdToken(
                activity,
                credentialManager,
                GetSignInWithGoogleOption.Builder(webClientId).build()
            )
        } catch (e: GetCredentialCancellationException) {
            throw e
        } catch (e: GetCredentialException) {
            requestIdToken(
                activity,
                credentialManager,
                GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
            )
        }
    }

    private suspend fun requestIdToken(
        activity: Activity,
        credentialManager: CredentialManager,
        option: GetCustomCredentialOption
    ): String {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = credentialManager.getCredential(activity, request)
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
        val pluginId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        if (pluginId != 0) {
            context.getString(pluginId).trim().takeIf { it.isNotEmpty() }?.let { return it }
        }
        return null
    }

    fun userMessage(error: Throwable): String = when (error) {
        is NoCredentialException -> "No Google account found on this device."
        is GetCredentialException ->
            error.errorMessage?.toString()?.takeIf { it.isNotBlank() }
                ?: "Google Sign-In failed. Try again."
        else -> error.message?.takeIf { it.isNotBlank() } ?: "Google Sign-In failed"
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
