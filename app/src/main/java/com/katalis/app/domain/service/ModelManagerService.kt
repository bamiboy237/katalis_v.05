package com.katalis.app.domain.service

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ModelManagerService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelFileName = "gemma-3n-E2B-it-int4.task"
    private val internalModelFile = File(context.filesDir, modelFileName)

    // Debug: Manual override path for development/testing
    private var manualModelPath: String? = null

    // Multiple possible model locations (in order of preference)
    private val modelPaths = listOf(
        // 1. App's external files directory (preferred for user-placed files)
        File(context.getExternalFilesDir(null), modelFileName),

        // 2. Internal storage (final location after copying)
        internalModelFile,

        // 3. Downloads folder (common user location) - using modern approach
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), modelFileName),
        
        // 4. Legacy downloads folder (fallback)
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), modelFileName)
    )

    suspend fun ensureModelAvailable(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Check available memory before attempting model operations
            val memoryInfo = getAvailableMemory()
            if (memoryInfo.availMem < 3_000_000_000L) { // Less than 3GB available
                return@withContext Result.failure(
                    Exception("Insufficient memory for Gemma 3n model. Available: ${memoryInfo.availMem / 1024 / 1024}MB, Required: ~3GB")
                )
            }

            // Debug: Check manual override first
            manualModelPath?.let { manualPath ->
                val manualFile = File(manualPath)
                if (manualFile.exists()) {
                    return@withContext validateAndReturnPath(manualFile, "Manual override")
                } else {
                    return@withContext Result.failure(
                        Exception("Manual model path does not exist: $manualPath")
                    )
                }
            }

            // First, check if we already have a valid model in internal storage
            if (internalModelFile.exists() && isValidModel(internalModelFile)) {
                return@withContext Result.success(internalModelFile.absolutePath)
            }

            // Look for model in various locations
            val sourceModelFile = findModelFile()

            if (sourceModelFile != null) {
                // Copy to internal storage if not already there
                if (sourceModelFile != internalModelFile) {
                    copyModelToInternalStorage(sourceModelFile)
                }

                if (isValidModel(internalModelFile)) {
                    Result.success(internalModelFile.absolutePath)
                } else {
                    Result.failure(Exception("Model file validation failed after copying"))
                }
            } else {
                // Fallback: try to copy from assets (original behavior)
                try {
                    copyModelFromAssets()
                    if (isValidModel(internalModelFile)) {
                        Result.success(internalModelFile.absolutePath)
                    } else {
                        Result.failure(Exception("Model file validation failed"))
                    }
                } catch (e: Exception) {
                    Result.failure(
                        Exception(
                            buildString {
                                appendLine("Gemma 3n model file not found. Please:")
                                appendLine("1. Download 'gemma-3n-E2B-it-int4.task' (~3GB)")
                                appendLine("2. Place it in one of these locations:")
                                modelPaths.forEach { path ->
                                    appendLine("   • ${path.absolutePath}")
                                }
                                appendLine("3. Or use Debug Settings to manually select the file")
                                appendLine()
                                appendLine("Requirements:")
                                appendLine("• File must be .task format (not .tflite)")
                                appendLine("• File size: 2.9-3.2GB")
                                appendLine("• Available device memory: 3GB+")
                                appendLine()
                                appendLine("Error: ${e.message}")
                            }
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getModelPath(): String? {
        return manualModelPath
            ?: if (internalModelFile.exists()) internalModelFile.absolutePath else null
    }

    // Debug functions
    fun setManualModelPath(path: String?): ModelValidationResult {
        if (path.isNullOrBlank()) {
            manualModelPath = null
            return ModelValidationResult.Success("Manual model path cleared")
        }

        val file = File(path)
        return when {
            !file.exists() -> ModelValidationResult.Error("File does not exist: $path")
            !file.canRead() -> ModelValidationResult.Error("Cannot read file: $path")
            !isValidModel(file) -> ModelValidationResult.Error(
                "Invalid model file. Expected size: 2.9-3.2GB, actual: ${file.length() / 1024 / 1024}MB"
            )

            else -> {
                manualModelPath = path
                ModelValidationResult.Success("Manual model path set successfully")
            }
        }
    }

    fun getManualModelPath(): String? = manualModelPath

    fun validateModelFile(path: String): ModelValidationResult {
        val file = File(path)
        return when {
            !file.exists() -> ModelValidationResult.Error("File does not exist")
            !file.canRead() -> ModelValidationResult.Error("Cannot read file - check permissions")
            !path.endsWith(".task", ignoreCase = true) -> ModelValidationResult.Error(
                "Invalid file format. MediaPipe GenAI requires .task files, not .tflite. Please use a properly converted Gemma 3n .task file."
            )
            !isValidModelSize(file) -> ModelValidationResult.Error(
                "Invalid model file size. Expected: 2.9-3.2GB for Gemma 3n, actual: ${file.length() / 1024 / 1024}MB"
            )

            !isReadableFile(file) -> ModelValidationResult.Error(
                "File appears to be corrupted or incomplete. Please re-download the model."
            )

            else -> ModelValidationResult.Success("Valid Gemma 3n model file (${file.length() / 1024 / 1024}MB)")
        }
    }

    fun getModelSearchPaths(): List<String> {
        return modelPaths.map { it.absolutePath }
    }

    fun getCurrentModelInfo(): ModelInfo {
        val activePath = manualModelPath
            ?: if (internalModelFile.exists()) internalModelFile.absolutePath else null
        return ModelInfo(
            activePath = activePath,
            manualOverride = manualModelPath,
            searchPaths = getModelSearchPaths(),
            modelFound = activePath != null && File(activePath).exists(),
            isValid = activePath?.let { isValidModel(File(it)) } ?: false
        )
    }

    private fun validateAndReturnPath(file: File, source: String): Result<String> {
        return if (isValidModel(file)) {
            Result.success(file.absolutePath)
        } else {
            Result.failure(
                Exception("$source model file validation failed. Size: ${file.length() / 1024 / 1024}MB, expected: ~3GB")
            )
        }
    }

    private fun findModelFile(): File? {
        return modelPaths.firstOrNull { file ->
            file.exists() && isValidModel(file)
        }
    }

    private suspend fun copyModelToInternalStorage(sourceFile: File) = withContext(Dispatchers.IO) {
        sourceFile.inputStream().use { input ->
            FileOutputStream(internalModelFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private suspend fun copyModelFromAssets() = withContext(Dispatchers.IO) {
        context.assets.open(modelFileName).use { input ->
            FileOutputStream(internalModelFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun isValidModel(file: File): Boolean {
        return file.exists() &&
                file.canRead() &&
                file.name.endsWith(".task", ignoreCase = true) &&
                isValidModelSize(file) &&
                isReadableFile(file)
    }

    private fun isValidModelSize(file: File): Boolean {
        // Gemma 3n int4 models are typically 2.9-3.2GB (expanded range for model variants)
        val expectedSizeRange = 2900_000_000L..3200_000_000L
        val fileSizeBytes = file.length()
        val fileSizeMB = fileSizeBytes / (1024 * 1024)
        val expectedMinMB = expectedSizeRange.first / (1024 * 1024)
        val expectedMaxMB = expectedSizeRange.last / (1024 * 1024)
        
        Log.d("ModelManager", "Size validation - File: ${file.name}")
        Log.d("ModelManager", "  Actual size: ${fileSizeBytes} bytes (${fileSizeMB}MB)")
        Log.d("ModelManager", "  Expected range: ${expectedSizeRange.first}-${expectedSizeRange.last} bytes (${expectedMinMB}-${expectedMaxMB}MB)")
        
        val isValid = fileSizeBytes in expectedSizeRange
        Log.d("ModelManager", "  Size validation result: $isValid")
        
        return isValid
    }

    private fun isReadableFile(file: File): Boolean {
        return try {
            // Try to read first few bytes to verify file isn't corrupted
            file.inputStream().use { stream ->
                val buffer = ByteArray(1024)
                stream.read(buffer) > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun getAvailableMemory(): android.app.ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return android.app.ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
            Log.d("ModelManager", "Memory info - Available: ${memoryInfo.availMem / (1024 * 1024)}MB, Total: ${memoryInfo.totalMem / (1024 * 1024)}MB, Low memory: ${memoryInfo.lowMemory}")
        }
    }

    sealed class ModelValidationResult {
        data class Success(val message: String) : ModelValidationResult()
        data class Error(val message: String) : ModelValidationResult()
    }

    data class ModelInfo(
        val activePath: String?,
        val manualOverride: String?,
        val searchPaths: List<String>,
        val modelFound: Boolean,
        val isValid: Boolean
    )
}