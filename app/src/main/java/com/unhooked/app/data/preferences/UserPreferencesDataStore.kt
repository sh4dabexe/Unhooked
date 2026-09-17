package com.unhooked.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unhooked.app.domain.model.ProtectionMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unhooked_preferences")

class UserPreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_PROTECTION_MODE = stringPreferencesKey("protection_mode")
        val KEY_PASSWORD_HASH = stringPreferencesKey("password_hash")
        val KEY_PASSWORD_SALT = stringPreferencesKey("password_salt")
        val KEY_OVERALL_LIMIT_MIN = intPreferencesKey("overall_daily_limit_min")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val isOnboardedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDED] ?: false
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_NAME] ?: "Friend"
    }

    val protectionModeFlow: Flow<ProtectionMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_PROTECTION_MODE] ?: ProtectionMode.NORMAL.name
        try {
            ProtectionMode.valueOf(raw)
        } catch (e: Exception) {
            ProtectionMode.NORMAL
        }
    }

    val overallLimitMinFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_OVERALL_LIMIT_MIN] ?: 180 // Default 3 hours
    }

    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: false
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDED] = onboarded }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name }
    }

    suspend fun setProtectionMode(mode: ProtectionMode) {
        context.dataStore.edit { it[KEY_PROTECTION_MODE] = mode.name }
    }

    suspend fun setPassword(hash: String, salt: String) {
        context.dataStore.edit {
            it[KEY_PASSWORD_HASH] = hash
            it[KEY_PASSWORD_SALT] = salt
        }
    }

    suspend fun getPasswordCredentials(): Pair<String, String>? {
        var credentials: Pair<String, String>? = null
        context.dataStore.edit { prefs ->
            val hash = prefs[KEY_PASSWORD_HASH]
            val salt = prefs[KEY_PASSWORD_SALT]
            if (!hash.isNullOrBlank() && !salt.isNullOrBlank()) {
                credentials = Pair(hash, salt)
            }
        }
        return credentials
    }

    suspend fun setOverallLimitMin(minutes: Int) {
        context.dataStore.edit { it[KEY_OVERALL_LIMIT_MIN] = minutes }
    }

    suspend fun setDarkMode(darkMode: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = darkMode }
    }
}
