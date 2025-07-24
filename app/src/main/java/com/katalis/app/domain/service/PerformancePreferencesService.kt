package com.katalis.app.domain.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformancePreferencesService @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    enum class PerformanceMode {
        AUTO,           // Let the system decide based on device capabilities
        PERFORMANCE,    // Prefer GPU acceleration for speed
        BATTERY_SAVER,  // Prefer CPU for battery life
        MANUAL         // User controls GPU on/off manually
    }

    data class PerformanceSettings(
        val performanceMode: PerformanceMode = PerformanceMode.AUTO,
        val allowGPUWhenBatteryLow: Boolean = false,
        val allowGPUWhenHot: Boolean = false,
        val showPerformanceStats: Boolean = false,
        val adaptiveMode: Boolean = true, // Learn from usage patterns
        val maxInferenceTime: Int = 10000 // milliseconds, fallback to CPU if exceeded
    )

    companion object {
        private val PERFORMANCE_MODE_KEY = stringPreferencesKey("performance_mode")
        private val ALLOW_GPU_BATTERY_LOW_KEY = booleanPreferencesKey("allow_gpu_battery_low")
        private val ALLOW_GPU_HOT_KEY = booleanPreferencesKey("allow_gpu_hot")
        private val SHOW_PERFORMANCE_STATS_KEY = booleanPreferencesKey("show_performance_stats")
        private val ADAPTIVE_MODE_KEY = booleanPreferencesKey("adaptive_mode")
        private val MAX_INFERENCE_TIME_KEY = stringPreferencesKey("max_inference_time")
    }

    val performanceSettings: Flow<PerformanceSettings> = dataStore.data.map { preferences ->
        PerformanceSettings(
            performanceMode = PerformanceMode.valueOf(
                preferences[PERFORMANCE_MODE_KEY] ?: PerformanceMode.AUTO.name
            ),
            allowGPUWhenBatteryLow = preferences[ALLOW_GPU_BATTERY_LOW_KEY] ?: false,
            allowGPUWhenHot = preferences[ALLOW_GPU_HOT_KEY] ?: false,
            showPerformanceStats = preferences[SHOW_PERFORMANCE_STATS_KEY] ?: false,
            adaptiveMode = preferences[ADAPTIVE_MODE_KEY] ?: true,
            maxInferenceTime = preferences[MAX_INFERENCE_TIME_KEY]?.toIntOrNull() ?: 10000
        )
    }

    suspend fun updatePerformanceMode(mode: PerformanceMode) {
        dataStore.edit { preferences ->
            preferences[PERFORMANCE_MODE_KEY] = mode.name
        }
    }

    suspend fun updateBatteryLowGPUAllowed(allowed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALLOW_GPU_BATTERY_LOW_KEY] = allowed
        }
    }

    suspend fun updateHotGPUAllowed(allowed: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALLOW_GPU_HOT_KEY] = allowed
        }
    }

    suspend fun updateShowPerformanceStats(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PERFORMANCE_STATS_KEY] = show
        }
    }

    suspend fun updateAdaptiveMode(adaptive: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADAPTIVE_MODE_KEY] = adaptive
        }
    }

    suspend fun updateMaxInferenceTime(maxTimeMs: Int) {
        dataStore.edit { preferences ->
            preferences[MAX_INFERENCE_TIME_KEY] = maxTimeMs.toString()
        }
    }

    suspend fun updateAllSettings(settings: PerformanceSettings) {
        dataStore.edit { preferences ->
            preferences[PERFORMANCE_MODE_KEY] = settings.performanceMode.name
            preferences[ALLOW_GPU_BATTERY_LOW_KEY] = settings.allowGPUWhenBatteryLow
            preferences[ALLOW_GPU_HOT_KEY] = settings.allowGPUWhenHot
            preferences[SHOW_PERFORMANCE_STATS_KEY] = settings.showPerformanceStats
            preferences[ADAPTIVE_MODE_KEY] = settings.adaptiveMode
            preferences[MAX_INFERENCE_TIME_KEY] = settings.maxInferenceTime.toString()
        }
    }

    fun getPerformanceModeDescription(mode: PerformanceMode): String {
        return when (mode) {
            PerformanceMode.AUTO -> "Smart: Automatic based on device and conditions"
            PerformanceMode.PERFORMANCE -> "Speed: Prefer GPU for fastest responses"
            PerformanceMode.BATTERY_SAVER -> "Efficiency: CPU-only for longer battery life"
            PerformanceMode.MANUAL -> "Manual: User controls acceleration per session"
        }
    }

    fun getPerformanceModeIcon(mode: PerformanceMode): String {
        return when (mode) {
            PerformanceMode.AUTO -> "🧠" // Brain for smart
            PerformanceMode.PERFORMANCE -> "⚡" // Lightning for speed
            PerformanceMode.BATTERY_SAVER -> "🔋" // Battery
            PerformanceMode.MANUAL -> "⚙️" // Gear for manual
        }
    }
}