package com.katalis.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.katalis.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val context: Context
) : UserProfileRepository {

    private object PreferenceKeys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    override suspend fun setOnboardingComplete(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETE] = completed
        }
    }

    override fun isOnboardingComplete(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETE] ?: false
        }
    }

    override suspend fun setUserPreference(key: String, value: String) {
        val preferenceKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferenceKey] = value
        }
    }

    override fun getUserPreference(key: String, defaultValue: String): Flow<String> {
        val preferenceKey = stringPreferencesKey(key)
        return context.dataStore.data.map { preferences ->
            preferences[preferenceKey] ?: defaultValue
        }
    }

    override suspend fun clearAllPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}