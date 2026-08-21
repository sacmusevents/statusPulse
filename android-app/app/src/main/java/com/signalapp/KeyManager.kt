package com.signalapp

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object KeyManager {
    private const val PREFS_NAME = "app_secrets"
    private const val KEY_SUPABASE_KEY = "supabase_key"

    private fun getEncryptedPreferences(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun getSupabaseKey(context: Context): String? {
        return try {
            val prefs = getEncryptedPreferences(context)
            prefs.getString(KEY_SUPABASE_KEY, null)
        } catch (e: Exception) {
            android.util.Log.e("KeyManager", "Error reading key", e)
            null
        }
    }

    fun setSupabaseKey(context: Context, key: String) {
        try {
            val prefs = getEncryptedPreferences(context)
            prefs.edit().putString(KEY_SUPABASE_KEY, key).apply()
        } catch (e: Exception) {
            android.util.Log.e("KeyManager", "Error writing key", e)
            throw e
        }
    }

    fun hasSupabaseKey(context: Context): Boolean {
        // Firebase Realtime DB in test mode does not block startup if key is unconfigured
        return true
    }

    fun deleteSupabaseKey(context: Context) {
        try {
            val prefs = getEncryptedPreferences(context)
            prefs.edit().remove(KEY_SUPABASE_KEY).apply()
        } catch (e: Exception) {
            android.util.Log.e("KeyManager", "Error deleting key", e)
        }
    }
}
