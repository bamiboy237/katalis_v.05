package com.katalis.app.domain.service

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES30
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceCapabilityService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    data class DeviceCapabilities(
        val hasGPUSupport: Boolean,
        val estimatedVRAM: Long, // in bytes
        val cpuCores: Int,
        val totalRAM: Long, // in bytes
        val availableStorage: Long, // in bytes
        val gpuVendor: String,
        val isLowPowerMode: Boolean,
        val thermalState: Int,
        val recommendedAcceleration: AccelerationType
    )
    
    enum class AccelerationType {
        CPU_ONLY,
        GPU_PREFERRED,
        GPU_FALLBACK_CPU,
        AUTO_ADAPTIVE
    }
    
    suspend fun analyzeDeviceCapabilities(): DeviceCapabilities = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Basic hardware detection
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalRAM = memoryInfo.totalMem
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val availableStorage = getAvailableInternalStorage()
        
        // GPU capability detection
        val gpuInfo = detectGPUCapabilities()
        val hasGPUSupport = gpuInfo.first
        val gpuVendor = gpuInfo.second
        val estimatedVRAM = estimateVRAM(totalRAM, gpuVendor)
        
        // Power and thermal state
        val isLowPowerMode = powerManager.isPowerSaveMode
        val thermalState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus
        } else {
            PowerManager.THERMAL_STATUS_NONE
        }
        
        // Intelligent recommendation
        val recommendedAcceleration = determineOptimalAcceleration(
            hasGPUSupport = hasGPUSupport,
            estimatedVRAM = estimatedVRAM,
            totalRAM = totalRAM,
            gpuVendor = gpuVendor,
            isLowPowerMode = isLowPowerMode,
            thermalState = thermalState
        )
        
        DeviceCapabilities(
            hasGPUSupport = hasGPUSupport,
            estimatedVRAM = estimatedVRAM,
            cpuCores = cpuCores,
            totalRAM = totalRAM,
            availableStorage = availableStorage,
            gpuVendor = gpuVendor,
            isLowPowerMode = isLowPowerMode,
            thermalState = thermalState,
            recommendedAcceleration = recommendedAcceleration
        )
    }
    
    private fun detectGPUCapabilities(): Pair<Boolean, String> {
        return try {
            // SAFE APPROACH: Use Android's built-in GL ES configuration instead of direct OpenGL calls
            // This avoids SIGSEGV crashes from accessing GL functions without a context

            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configInfo = activityManager.deviceConfigurationInfo

            // Check OpenGL ES version support through system APIs (safe)
            val hasBasicGLSupport = configInfo.reqGlEsVersion >= 0x30000 // OpenGL ES 3.0+

            // Check for Vulkan support (safe system feature check)
            val hasVulkanSupport = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.packageManager.hasSystemFeature("android.hardware.vulkan")
            } else false

            // Use system properties for GPU vendor detection (safe)
            val gpuVendor = detectGPUVendorSafe()

            val hasGPUSupport = hasBasicGLSupport || hasVulkanSupport
            
            Pair(hasGPUSupport, gpuVendor)
        } catch (e: Exception) {
            // Always fallback to safe defaults
            android.util.Log.w("DeviceCapability", "GPU detection failed safely: ${e.message}")
            Pair(false, "Unknown (Detection Disabled)")
        }
    }

    private fun detectGPUVendorSafe(): String {
        return try {
            // Safe detection using system properties instead of OpenGL calls
            val boardName = Build.BOARD.lowercase()
            val deviceName = Build.DEVICE.lowercase()
            val hardwareName = Build.HARDWARE.lowercase()

            when {
                boardName.contains("qcom") || deviceName.contains("qcom") || hardwareName.contains("qcom") -> "Qualcomm Adreno"
                boardName.contains("exynos") || deviceName.contains("exynos") -> "ARM Mali (Exynos)"
                boardName.contains("tegra") || deviceName.contains("tegra") -> "NVIDIA Tegra"
                boardName.contains("mtk") || deviceName.contains("mtk") -> "ARM Mali (MediaTek)"
                boardName.contains("kirin") || deviceName.contains("kirin") -> "ARM Mali (Kirin)"
                else -> "Unknown GPU (${Build.HARDWARE})"
            }
        } catch (e: Exception) {
            "Unknown GPU (Safe Mode)"
        }
    }

    private fun estimateVRAM(totalRAM: Long, gpuVendor: String): Long {
        // Estimate VRAM based on total RAM and GPU type
        return when {
            totalRAM >= 12L * 1024 * 1024 * 1024 -> 2L * 1024 * 1024 * 1024 // 2GB for high-end devices
            totalRAM >= 8L * 1024 * 1024 * 1024 -> 1L * 1024 * 1024 * 1024  // 1GB for mid-high devices
            totalRAM >= 6L * 1024 * 1024 * 1024 -> 512L * 1024 * 1024       // 512MB for mid devices
            totalRAM >= 4L * 1024 * 1024 * 1024 -> 256L * 1024 * 1024       // 256MB for budget devices
            else -> 128L * 1024 * 1024  // 128MB for very low-end devices
        }.let { baseVRAM ->
            // Adjust based on GPU vendor known capabilities
            when {
                gpuVendor.contains("Adreno 7", ignoreCase = true) -> (baseVRAM * 1.5).toLong()
                gpuVendor.contains("Adreno 6", ignoreCase = true) -> baseVRAM
                gpuVendor.contains("Mali-G", ignoreCase = true) -> (baseVRAM * 0.8).toLong()
                else -> baseVRAM
            }
        }
    }
    
    private fun determineOptimalAcceleration(
        hasGPUSupport: Boolean,
        estimatedVRAM: Long,
        totalRAM: Long,
        gpuVendor: String,
        isLowPowerMode: Boolean,
        thermalState: Int
    ): AccelerationType {
        // ULTRA-CONSERVATIVE: Force CPU-only until GPU detection is fully validated
        // This ensures app stability for Gemma 3n integration

        android.util.Log.d("DeviceCapability", "GPU Detection Results:")
        android.util.Log.d("DeviceCapability", "  hasGPUSupport: $hasGPUSupport")
        android.util.Log.d("DeviceCapability", "  gpuVendor: $gpuVendor")
        android.util.Log.d("DeviceCapability", "  estimatedVRAM: ${formatBytes(estimatedVRAM)}")
        android.util.Log.d("DeviceCapability", "  totalRAM: ${formatBytes(totalRAM)}")
        android.util.Log.d("DeviceCapability", "  Forcing CPU_ONLY for stability")

        return AccelerationType.CPU_ONLY

        // TODO: Re-enable GPU acceleration after implementing safe OpenGL context handling
        /*
        if (!hasGPUSupport || isLowPowerMode) {
            return AccelerationType.CPU_ONLY
        }
        
        if (thermalState >= PowerManager.THERMAL_STATUS_MODERATE) {
            return AccelerationType.CPU_ONLY  // Avoid GPU when device is already warm
        }
        
        val minVRAMForGPU = 512L * 1024 * 1024  // 512MB minimum
        val minRAMForGPU = 4L * 1024 * 1024 * 1024  // 4GB total RAM minimum
        
        return when {
            estimatedVRAM >= minVRAMForGPU && totalRAM >= minRAMForGPU -> {
                if (gpuVendor.contains("Adreno 7", ignoreCase = true) || 
                    gpuVendor.contains("Mali-G78", ignoreCase = true)) {
                    AccelerationType.GPU_PREFERRED
                } else {
                    AccelerationType.GPU_FALLBACK_CPU
                }
            }
            else -> AccelerationType.CPU_ONLY
        }
        */
    }
    
    private fun getAvailableInternalStorage(): Long {
        return try {
            val statFs = StatFs(context.filesDir.path)
            statFs.availableBytes
        } catch (e: Exception) {
            0L
        }
    }
    
    fun getCapabilityDescription(capabilities: DeviceCapabilities): String {
        return buildString {
            append("Device Analysis:\n")
            append("• RAM: ${formatBytes(capabilities.totalRAM)}\n")
            append("• CPU Cores: ${capabilities.cpuCores}\n")
            append("• GPU: ${capabilities.gpuVendor}\n")
            append("• Estimated VRAM: ${formatBytes(capabilities.estimatedVRAM)}\n")
            append("• Storage: ${formatBytes(capabilities.availableStorage)} available\n")
            append("• Power Mode: ${if (capabilities.isLowPowerMode) "Battery Saver" else "Normal"}\n")
            append("• Thermal State: ${getThermalDescription(capabilities.thermalState)}\n")
            append("• Recommended: ${getAccelerationDescription(capabilities.recommendedAcceleration)}")
        }
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)}GB"
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)}MB"
            else -> "${bytes / 1024}KB"
        }
    }
    
    private fun getThermalDescription(thermalState: Int): String {
        return when (thermalState) {
            PowerManager.THERMAL_STATUS_NONE -> "Cool"
            PowerManager.THERMAL_STATUS_LIGHT -> "Slightly Warm"
            PowerManager.THERMAL_STATUS_MODERATE -> "Warm"
            PowerManager.THERMAL_STATUS_SEVERE -> "Hot"
            PowerManager.THERMAL_STATUS_CRITICAL -> "Very Hot"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown Risk"
            else -> "Unknown"
        }
    }
    
    private fun getAccelerationDescription(acceleration: AccelerationType): String {
        return when (acceleration) {
            AccelerationType.CPU_ONLY -> "CPU Only (Safe)"
            AccelerationType.GPU_PREFERRED -> "GPU Acceleration (Optimal)"
            AccelerationType.GPU_FALLBACK_CPU -> "GPU with CPU Fallback"
            AccelerationType.AUTO_ADAPTIVE -> "Adaptive (Smart)"
        }
    }
}