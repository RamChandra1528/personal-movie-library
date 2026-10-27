package com.example.service

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
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
 * Anonymous authentication and email/password are never used.
 */
class AuthManager(private val context: Context) {

    // A locally installable APK may intentionally omit google-services.json. Firebase SDKs
    // throw during getInstance() in that case, so keep cloud sign-in optional instead of
    // preventing the offline movie library from launching.
    private val auth: FirebaseAuth? = runCatching {
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseAuth.getInstance()
    }.getOrNull()
    private val credentialManager = CredentialManager.create(context)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth?.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth?.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
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
        return try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                context.getString(resId)
            } else {
                "710389448956-vgeeap22quhe9dbcal6q49134nhvhmv5.apps.googleusercontent.com"
            }
        } catch (e: Exception) {
            "710389448956-vgeeap22quhe9dbcal6q49134nhvhmv5.apps.googleusercontent.com"
        }
    }

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            val firebaseAuth = auth ?: return Result.failure(
                IllegalStateException("Cloud sign-in is unavailable because Firebase is not configured for this app.")
            )
            val serverClientId = getServerClientId()
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
        } catch (e: Exception) {
            Log.e(TAG, "Sign in with Google failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Please enter your password."))
        }
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Cloud authentication is unavailable because Firebase is not configured.")
        )
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after sign-in")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email sign in failed", e)
            Result.failure(e)
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Cloud authentication is unavailable because Firebase is not configured.")
        )
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after registration")
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Email registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val firebaseAuth = auth ?: return Result.failure(
            IllegalStateException("Cloud authentication is unavailable because Firebase is not configured.")
        )
        return try {
            firebaseAuth.sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password reset failed", e)
            Result.failure(e)
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
