package com.mirazanik.masjidscreen.admin.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Hosts Credential Manager outside the kiosk MainActivity.
 * singleTask + fullscreen on MainActivity swallows the account-picker result,
 * which looks like "tap an account and nothing happens".
 */
class GoogleSignInActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val result = Intent()
            try {
                val token = GoogleSignInHelper.getIdToken(this@GoogleSignInActivity)
                result.putExtra(EXTRA_ID_TOKEN, token)
                setResult(Activity.RESULT_OK, result)
            } catch (e: GetCredentialCancellationException) {
                setResult(Activity.RESULT_CANCELED)
            } catch (e: Exception) {
                result.putExtra(EXTRA_ERROR, GoogleSignInHelper.userMessage(e))
                setResult(Activity.RESULT_CANCELED, result)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_ID_TOKEN = "google_id_token"
        const val EXTRA_ERROR = "google_sign_in_error"

        fun createIntent(context: Context): Intent =
            Intent(context, GoogleSignInActivity::class.java)
    }
}
