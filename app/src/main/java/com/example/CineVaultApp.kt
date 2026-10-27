package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class CineVaultApp : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d(TAG, "FirebaseApp initialized in CineVaultApp")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing FirebaseApp", e)
        }
    }

    companion object {
        private const val TAG = "CineVaultApp"
    }
}
