package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages Google Sign-In authentication via Jetpack CredentialManager.
 * Strictly adheres to zero-trust security: only interactive Google Sign-In is supported.
 * Anonymous authentication and email/password are never used in production application code.
 */
class AuthManager(private val context: Context) {

    private val auth: FirebaseAuth?
        get() = try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FirebaseAuth instance", e)
            null
        }

    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        try {
            val initialUser = auth?.currentUser
            _currentUser.value = initialUser
            auth?.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AuthStateListener", e)
        }
    }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth?.addAuthStateListener(listener)
        awaitClose { auth?.removeAuthStateListener(listener) }
    }

    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    fun requireUserId(): String = auth?.currentUser?.uid
        ?: throw IllegalStateException("Operation requires an authenticated user")

    private fun getServerClientId(): String {
        return context.getString(R.string.default_web_client_id)
    }

    /**
     * Interactive Google Sign-In flow via Jetpack CredentialManager.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val firebaseAuth = auth ?: return Result.failure(
                IllegalStateException("Cloud sign-in is unavailable because Firebase is not configured for this app.")
            )

            val serverClientId = try {
                getServerClientId()
            } catch (e: Exception) {
                return Result.failure(
                    IllegalStateException("Google Sign-In configuration missing: default_web_client_id not found")
                )
            }

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign-in")
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("Unexpected credential format: ${credential::class.java.name}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in with Google failed", e)
            Result.failure(e)
        }
    }

    /**
     * Silent auto-sign-in check on app launch to restore credentials if available.
     */
    suspend fun attemptAutoSignIn(): Result<FirebaseUser?> {
        val existingUser = auth?.currentUser
        if (existingUser != null) {
            _currentUser.value = existingUser
            return Result.success(existingUser)
        }

        return try {
            val firebaseAuth = auth ?: return Result.success(null)
            val serverClientId = try {
                getServerClientId()
            } catch (_: Exception) {
                return Result.success(null)
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(true)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val user = authResult.user
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (_: Exception) {
            Result.success(null)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            auth?.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "AuthManager"
    }
}
