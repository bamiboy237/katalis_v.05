package com.katalis.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {

    suspend fun setOnboardingComplete(completed: Boolean)

    fun isOnboardingComplete(): Flow<Boolean>

    suspend fun setUserPreference(key: String, value: String)

    fun getUserPreference(key: String, defaultValue: String = ""): Flow<String>

    suspend fun clearAllPreferences()
}