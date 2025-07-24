package com.katalis.app.domain.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerformanceMonitoringService @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    
    @Serializable
    data class PerformanceStats(
        val totalInferences: Long = 0,
        val averageInferenceTimeMs: Long = 0,
        val gpuInferences: Long = 0,
        val cpuInferences: Long = 0,
        val gpuAverageTimeMs: Long = 0,
        val cpuAverageTimeMs: Long = 0,
        val successfulInferences: Long = 0,
        val failedInferences: Long = 0,
        val lastUpdated: Long = System.currentTimeMillis()
    ) {
        val successRate: Float
            get() = if (totalInferences > 0) successfulInferences.toFloat() / totalInferences else 1.0f
        
        val preferredAcceleration: String
            get() = when {
                gpuInferences == 0L && cpuInferences > 0L -> "CPU Only"
                cpuInferences == 0L && gpuInferences > 0L -> "GPU Only"
                gpuAverageTimeMs < cpuAverageTimeMs -> "GPU Preferred"
                cpuAverageTimeMs < gpuAverageTimeMs -> "CPU Preferred"
                else -> "Balanced"
            }
    }
    
    @Serializable
    data class SessionStats(
        val sessionStart: Long = System.currentTimeMillis(),
        val inferences: Int = 0,
        val totalTimeMs: Long = 0,
        val accelerationType: String = "",
        val deviceThermalEvents: Int = 0,
        val batteryLevelStart: Float = 100f,
        val batteryLevelCurrent: Float = 100f
    ) {
        val averageInferenceTime: Long
            get() = if (inferences > 0) totalTimeMs / inferences else 0L
        
        val batteryUsage: Float
            get() = batteryLevelStart - batteryLevelCurrent
    }
    
    data class PerformanceInsights(
        val recommendation: String,
        val reasoning: String,
        val optimizationTips: List<String>,
        val statsummary: String
    )
    
    companion object {
        private val PERFORMANCE_STATS_KEY = stringPreferencesKey("performance_stats")
        private val LAST_ANALYSIS_KEY = longPreferencesKey("last_analysis_timestamp")
        private val SESSION_COUNT_KEY = longPreferencesKey("session_count")
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    val performanceStats: Flow<PerformanceStats> = dataStore.data.map { preferences ->
        val statsJson = preferences[PERFORMANCE_STATS_KEY]
        if (statsJson != null) {
            try {
                json.decodeFromString<PerformanceStats>(statsJson)
            } catch (e: Exception) {
                PerformanceStats()
            }
        } else {
            PerformanceStats()
        }
    }
    
    suspend fun recordInference(
        durationMs: Long,
        accelerationType: DeviceCapabilityService.AccelerationType,
        success: Boolean
    ) {
        dataStore.edit { preferences ->
            val currentStatsJson = preferences[PERFORMANCE_STATS_KEY]
            val currentStats = if (currentStatsJson != null) {
                try {
                    json.decodeFromString<PerformanceStats>(currentStatsJson)
                } catch (e: Exception) {
                    PerformanceStats()
                }
            } else {
                PerformanceStats()
            }
            
            val isGPU = accelerationType != DeviceCapabilityService.AccelerationType.CPU_ONLY
            
            val updatedStats = currentStats.copy(
                totalInferences = currentStats.totalInferences + 1,
                averageInferenceTimeMs = calculateNewAverage(
                    currentStats.averageInferenceTimeMs,
                    currentStats.totalInferences,
                    durationMs
                ),
                gpuInferences = if (isGPU) currentStats.gpuInferences + 1 else currentStats.gpuInferences,
                cpuInferences = if (!isGPU) currentStats.cpuInferences + 1 else currentStats.cpuInferences,
                gpuAverageTimeMs = if (isGPU) calculateNewAverage(
                    currentStats.gpuAverageTimeMs,
                    currentStats.gpuInferences,
                    durationMs
                ) else currentStats.gpuAverageTimeMs,
                cpuAverageTimeMs = if (!isGPU) calculateNewAverage(
                    currentStats.cpuAverageTimeMs,
                    currentStats.cpuInferences,
                    durationMs
                ) else currentStats.cpuAverageTimeMs,
                successfulInferences = if (success) currentStats.successfulInferences + 1 else currentStats.successfulInferences,
                failedInferences = if (!success) currentStats.failedInferences + 1 else currentStats.failedInferences,
                lastUpdated = System.currentTimeMillis()
            )
            
            preferences[PERFORMANCE_STATS_KEY] = json.encodeToString(updatedStats)
        }
    }
    
    private fun calculateNewAverage(currentAverage: Long, count: Long, newValue: Long): Long {
        return if (count == 0L) {
            newValue
        } else {
            ((currentAverage * count) + newValue) / (count + 1)
        }
    }
    
    suspend fun generatePerformanceInsights(
        currentStats: PerformanceStats,
        deviceCapabilities: DeviceCapabilityService.DeviceCapabilities
    ): PerformanceInsights {
        val insights = mutableListOf<String>()
        var recommendation = "Continue current usage"
        var reasoning = "Performance is optimal for your device."
        
        // Analyze performance patterns
        when {
            currentStats.totalInferences < 5 -> {
                recommendation = "Keep exploring"
                reasoning = "More usage data needed for accurate recommendations."
                insights.add("Try asking a few more questions to get personalized performance insights")
            }
            
            currentStats.gpuInferences > 0 && currentStats.cpuInferences > 0 -> {
                val gpuFaster = currentStats.gpuAverageTimeMs < currentStats.cpuAverageTimeMs
                val timeDifference = kotlin.math.abs(currentStats.gpuAverageTimeMs - currentStats.cpuAverageTimeMs)
                
                when {
                    timeDifference > 3000 && gpuFaster -> {
                        recommendation = "Use Performance Mode"
                        reasoning = "GPU acceleration is significantly faster on your device (${timeDifference}ms improvement)."
                        insights.add("Your device benefits greatly from GPU acceleration")
                        insights.add("Consider enabling Performance mode in settings")
                    }
                    
                    timeDifference > 3000 && !gpuFaster -> {
                        recommendation = "Use Battery Saver Mode"
                        reasoning = "CPU inference is faster on your device. GPU may be thermal throttling."
                        insights.add("Your device performs better with CPU-only inference")
                        insights.add("GPU acceleration may be causing thermal throttling")
                    }
                    
                    timeDifference <= 1000 -> {
                        recommendation = "Use Auto Mode"
                        reasoning = "Performance difference is minimal. Auto mode will optimize for battery and thermals."
                        insights.add("Both CPU and GPU perform similarly on your device")
                        insights.add("Auto mode will balance performance with battery life")
                    }
                }
            }
            
            currentStats.successRate < 0.8f -> {
                recommendation = "Switch to Battery Saver Mode"
                reasoning = "High failure rate suggests compatibility issues with current acceleration."
                insights.add("Try CPU-only mode for better stability")
                insights.add("Your device may have GPU compatibility issues")
            }
            
            currentStats.averageInferenceTimeMs > 8000 -> {
                recommendation = "Optimize performance"
                reasoning = "Responses are slower than optimal. Try adjusting settings or clearing background apps."
                insights.add("Close other apps to free up memory")
                insights.add("Make sure your device isn't in power saving mode")
                insights.add("Try shorter questions for faster responses")
            }
        }
        
        // Device-specific insights
        if (deviceCapabilities.totalRAM < 4L * 1024 * 1024 * 1024) {
            insights.add("Your device has limited RAM - CPU mode may be more stable")
        }
        
        if (deviceCapabilities.isLowPowerMode) {
            insights.add("Battery saver mode is active - performance may be reduced")
        }
        
        val statSummary = buildString {
            append("📊 Performance Summary:\n")
            append("• Total questions: ${currentStats.totalInferences}\n")
            append("• Average response time: ${currentStats.averageInferenceTimeMs / 1000.0}s\n")
            append("• Success rate: ${(currentStats.successRate * 100).toInt()}%\n")
            if (currentStats.gpuInferences > 0 && currentStats.cpuInferences > 0) {
                append("• GPU average: ${currentStats.gpuAverageTimeMs / 1000.0}s\n")
                append("• CPU average: ${currentStats.cpuAverageTimeMs / 1000.0}s\n")
            }
            append("• Recommendation: ${currentStats.preferredAcceleration}")
        }
        
        return PerformanceInsights(
            recommendation = recommendation,
            reasoning = reasoning,
            optimizationTips = insights,
            statsummary = statSummary
        )
    }
    
    suspend fun clearStats() {
        dataStore.edit { preferences ->
            preferences.remove(PERFORMANCE_STATS_KEY)
            preferences.remove(LAST_ANALYSIS_KEY)
        }
    }
    
    suspend fun incrementSessionCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[SESSION_COUNT_KEY] ?: 0L
            preferences[SESSION_COUNT_KEY] = currentCount + 1
        }
    }
    
    val sessionCount: Flow<Long> = dataStore.data.map { preferences ->
        preferences[SESSION_COUNT_KEY] ?: 0L
    }
}