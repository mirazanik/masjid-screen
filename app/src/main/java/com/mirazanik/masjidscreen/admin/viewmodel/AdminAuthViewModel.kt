package com.mirazanik.masjidscreen.admin.viewmodel

import android.app.Application
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mirazanik.masjidscreen.admin.auth.GoogleSignInHelper
import com.mirazanik.masjidscreen.admin.data.AdminRepository
import com.mirazanik.masjidscreen.data.local.AppDatabase
import com.mirazanik.masjidscreen.data.local.ThemePreferences
import com.mirazanik.masjidscreen.data.model.MosqueUser
import com.mirazanik.masjidscreen.data.model.UserStatus
import com.mirazanik.masjidscreen.data.remote.FirestoreProvider
import com.mirazanik.masjidscreen.data.remote.isTransientFirestoreError
import com.mirazanik.masjidscreen.util.NetworkMonitor
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AdminAuthState {
    object Loading : AdminAuthState()
    object Unauthenticated : AdminAuthState()
    data class Authenticated(val user: MosqueUser) : AdminAuthState()
    object PendingApproval : AdminAuthState()
    object Disabled : AdminAuthState()
    data class Error(val message: String, val retryable: Boolean = false) : AdminAuthState()

    fun isLoggedInAdmin(): Boolean = when (this) {
        is Authenticated, is PendingApproval, is Disabled -> true
        is Error -> retryable
        else -> false
    }
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}

class AdminAuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = Firebase.auth
    private val repo = AdminRepository()
    private val db = AppDatabase.getInstance(application)
    private val themePrefs = ThemePreferences(application)

    private val _authState = MutableStateFlow<AdminAuthState>(AdminAuthState.Loading)
    val authState: StateFlow<AdminAuthState> = _authState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()

    init {
        val current = auth.currentUser
        if (current != null && !current.isAnonymous) {
            viewModelScope.launch {
                resolveUserState(current.uid, current.email ?: "", current.displayName)
            }
        } else {
            _authState.value = AdminAuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            try {
                if (!NetworkMonitor.awaitOnline(getApplication())) {
                    _authState.value = AdminAuthState.Error(NO_CONNECTION_MESSAGE, retryable = false)
                    return@launch
                }
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No user after login")
                resolveUserState(uid, email)
            } catch (e: Exception) {
                _authState.value = AdminAuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun startGoogleSignIn() {
        _authState.value = AdminAuthState.Loading
    }

    fun abortGoogleSignIn(message: String? = null) {
        _authState.value = if (message.isNullOrBlank()) {
            AdminAuthState.Unauthenticated
        } else {
            AdminAuthState.Error(message)
        }
    }

    fun loginWithGoogle(getIdToken: suspend () -> String) {
        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            try {
                if (!NetworkMonitor.awaitOnline(getApplication())) {
                    _authState.value = AdminAuthState.Error(NO_CONNECTION_MESSAGE, retryable = false)
                    return@launch
                }
                val idToken = getIdToken()
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user ?: throw Exception("No user after Google sign-in")
                resolveUserState(user.uid, user.email ?: "", user.displayName)
            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {
                _authState.value = AdminAuthState.Unauthenticated
            } catch (e: Exception) {
                _authState.value = AdminAuthState.Error(GoogleSignInHelper.userMessage(e))
            }
        }
    }

    /** Re-runs profile loading for the already signed-in user after a connectivity failure. */
    fun retry() {
        val current = auth.currentUser?.takeUnless { it.isAnonymous } ?: run {
            _authState.value = AdminAuthState.Unauthenticated
            return
        }
        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            NetworkMonitor.awaitOnline(getApplication())
            FirestoreProvider.reconnect()
            resolveUserState(current.uid, current.email ?: "", current.displayName)
        }
    }

    fun register(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _authState.value = AdminAuthState.Loading
            try {
                if (!NetworkMonitor.awaitOnline(getApplication())) {
                    _authState.value = AdminAuthState.Error(NO_CONNECTION_MESSAGE)
                    return@launch
                }
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("No user after register")
                val user = repo.registerUser(uid, email, displayName)
                _authState.value = when (user.status) {
                    UserStatus.ACTIVE -> {
                        saveFcmToken(uid)
                        AdminAuthState.Authenticated(user)
                    }
                    UserStatus.PENDING -> AdminAuthState.PendingApproval
                    UserStatus.DISABLED -> AdminAuthState.Disabled
                }
            } catch (e: Exception) {
                _authState.value = if (e.isTransientFirestoreError()) {
                    AdminAuthState.Error(NO_CONNECTION_MESSAGE)
                } else {
                    AdminAuthState.Error(e.message ?: "Registration failed")
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching {
                CredentialManager.create(getApplication())
                    .clearCredentialState(ClearCredentialStateRequest())
            }
            auth.signOut()
            runCatching { auth.signInAnonymously().await() }
            _authState.value = AdminAuthState.Unauthenticated
            db.clearCache()
            themePrefs.clearTheme()
        }
    }

    fun clearError() {
        _authState.value = AdminAuthState.Unauthenticated
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _resetPasswordState.value = ResetPasswordState.Loading
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                _resetPasswordState.value = ResetPasswordState.Success
            } catch (e: Exception) {
                _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "Failed to send reset email")
            }
        }
    }

    fun clearResetState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }

    private suspend fun resolveUserState(uid: String, email: String, displayName: String? = null) {
        try {
            NetworkMonitor.awaitOnline(getApplication())
            val user = repo.bootstrapOrFetchUser(uid, email, displayName)
            _authState.value = when (user.status) {
                UserStatus.ACTIVE -> {
                    saveFcmToken(uid)
                    AdminAuthState.Authenticated(user)
                }
                UserStatus.PENDING -> AdminAuthState.PendingApproval
                UserStatus.DISABLED -> AdminAuthState.Disabled
            }
        } catch (e: Exception) {
            _authState.value = if (e.isTransientFirestoreError()) {
                AdminAuthState.Error(NO_CONNECTION_MESSAGE, retryable = true)
            } else {
                AdminAuthState.Error(e.message ?: "Failed to load user profile")
            }
        }
    }

    private fun saveFcmToken(uid: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            viewModelScope.launch { runCatching { repo.updateFcmToken(uid, token) } }
        }
    }

    companion object {
        private const val NO_CONNECTION_MESSAGE =
            "Can't reach the server. Check the internet connection and try again."
    }
}
