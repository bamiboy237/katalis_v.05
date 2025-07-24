package com.katalis.app.presentation.viewmodels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.katalis.app.domain.engine.Gemma3nEngine
import com.katalis.app.domain.service.ModelManagerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DebugSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelManager: ModelManagerService,
    private val gemma3nEngine: Gemma3nEngine
) : ViewModel() {

    @Immutable
    data class DebugState(
        val modelInfo: ModelManagerService.ModelInfo? = null,
        val validationResult: String? = null,
        val testResult: String? = null,
        val isLoading: Boolean = false
    )

    sealed class DebugEvent {
        data class SelectModelFile(val uri: String) : DebugEvent()
        object ClearManualPath : DebugEvent()
        object TestModel : DebugEvent()
        object DismissTestResult : DebugEvent()
        object RefreshModelInfo : DebugEvent()
    }

    private val _state = MutableStateFlow(DebugState())
    val state: StateFlow<DebugState> = _state.asStateFlow()

    init {
        refreshModelInfo()
    }

    fun onEvent(event: DebugEvent) {
        when (event) {
            is DebugEvent.SelectModelFile -> selectModelFile(event.uri)
            DebugEvent.ClearManualPath -> clearManualPath()
            DebugEvent.TestModel -> testModel()
            DebugEvent.DismissTestResult -> dismissTestResult()
            DebugEvent.RefreshModelInfo -> refreshModelInfo()
        }
    }

    private fun selectModelFile(uriString: String) {
        viewModelScope.launch {
            try {
                Log.d("DebugSettingsVM", "Selecting model file: $uriString")

                // Show progress during file copy
                _state.value = _state.value.copy(
                    validationResult = "Processing model file for MediaPipe GenAI compatibility..."
                )

                // Convert URI to file path
                val uri = Uri.parse(uriString)
                val filePath = getFilePathFromUri(uri)

                if (filePath == null) {
                    _state.value = _state.value.copy(
                        validationResult = "❌ Cannot access selected file. MediaPipe requires accessible file locations. Try placing the model in Downloads folder."
                    )
                    return@launch
                }

                Log.d("DebugSettingsVM", "File path resolved: $filePath")

                // Update progress
                _state.value = _state.value.copy(
                    validationResult = "Validating .task file format and MediaPipe compatibility..."
                )

                // Validate the selected file
                val validationResult = modelManager.validateModelFile(filePath)

                when (validationResult) {
                    is ModelManagerService.ModelValidationResult.Success -> {
                        _state.value = _state.value.copy(
                            validationResult = "✅ Valid Gemma 3n .task file detected. Configuring for MediaPipe GenAI..."
                        )

                        // Set the manual model path
                        val setResult = modelManager.setManualModelPath(filePath)
                        when (setResult) {
                            is ModelManagerService.ModelValidationResult.Success -> {
                                _state.value = _state.value.copy(
                                    validationResult = "✅ Model configured successfully! The model will be deployed to external storage for MediaPipe access during initialization."
                                )
                                refreshModelInfo()
                            }
                            is ModelManagerService.ModelValidationResult.Error -> {
                                _state.value = _state.value.copy(
                                    validationResult = "❌ Error configuring model: ${setResult.message}"
                                )
                            }
                        }
                    }
                    is ModelManagerService.ModelValidationResult.Error -> {
                        _state.value = _state.value.copy(
                            validationResult = "❌ ${validationResult.message}\n\nFor MediaPipe GenAI:\n• Use .task format (not .tflite)\n• File size: 2.9-3.1GB\n• Ensure file is not corrupted"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DebugSettingsVM", "Error selecting model file", e)
                _state.value = _state.value.copy(
                    validationResult = "❌ Error selecting file: ${e.message}\n\nMediaPipe GenAI requires accessible file locations and proper .task format."
                )
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    // Copy content URI file to internal storage for MediaPipe access
                    copyContentUriToInternal(uri)
                }
                "file" -> uri.path
                else -> null
            }
        } catch (e: Exception) {
            Log.e("DebugSettingsVM", "Error getting file path from URI", e)
            null
        }
    }

    private fun copyContentUriToInternal(contentUri: Uri): String? {
        return try {
            Log.d("DebugSettingsVM", "Starting content URI copy process")
            Log.d("DebugSettingsVM", "Content URI: $contentUri")

            // Get memory info before operation
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            activityManager.getMemoryInfo(memoryInfo)
            Log.d(
                "DebugSettingsVM",
                "Pre-copy memory - Available: ${memoryInfo.availMem / (1024 * 1024)}MB, Total: ${memoryInfo.totalMem / (1024 * 1024)}MB"
            )

            val inputStream = context.contentResolver.openInputStream(contentUri)
                ?: return null.also {
                    Log.e(
                        "DebugSettingsVM",
                        "Failed to open input stream from content URI"
                    )
                }

            // Create a temp file with .task extension in internal storage
            val tempFile = File(context.filesDir, "debug_model_${System.currentTimeMillis()}.task")

            Log.d("DebugSettingsVM", "Copying content URI to: ${tempFile.absolutePath}")
            Log.d(
                "DebugSettingsVM",
                "Available internal storage: ${context.filesDir.freeSpace / (1024 * 1024)}MB"
            )

            val startTime = System.currentTimeMillis()
            var bytesProcessed = 0L

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    // DIAGNOSTIC: This is where the OOM likely occurs for 3GB files
                    Log.d("DebugSettingsVM", "Starting file copy operation...")
                    try {
                        input.copyTo(output)
                        Log.d("DebugSettingsVM", "File copy completed successfully")
                    } catch (e: OutOfMemoryError) {
                        Log.e(
                            "DebugSettingsVM",
                            "OutOfMemoryError during file copy - file too large for current method",
                            e
                        )
                        throw e
                    } catch (e: Exception) {
                        Log.e("DebugSettingsVM", "Exception during file copy", e)
                        throw e
                    }
                }
            }

            val copyDuration = System.currentTimeMillis() - startTime
            Log.d("DebugSettingsVM", "Copy operation completed in ${copyDuration}ms")

            // Get memory info after operation
            activityManager.getMemoryInfo(memoryInfo)
            Log.d(
                "DebugSettingsVM",
                "Post-copy memory - Available: ${memoryInfo.availMem / (1024 * 1024)}MB"
            )

            // Verify the copied file exists and has reasonable size
            if (tempFile.exists() && tempFile.length() > 1_000_000_000L) { // At least 1GB
                Log.d(
                    "DebugSettingsVM",
                    "File copied successfully, size: ${tempFile.length() / 1024 / 1024}MB"
                )
                tempFile.absolutePath
            } else {
                Log.e(
                    "DebugSettingsVM",
                    "Copied file is too small or doesn't exist: ${tempFile.length()}"
                )
                tempFile.delete() // Clean up failed copy
                null
            }
        } catch (e: OutOfMemoryError) {
            Log.e(
                "DebugSettingsVM",
                "OutOfMemoryError: File too large for current copy method. Need streaming implementation.",
                e
            )
            null
        } catch (e: Exception) {
            Log.e("DebugSettingsVM", "Error copying content URI to internal storage", e)
            null
        }
    }

    private fun clearManualPath() {
        viewModelScope.launch {
            try {
                val result = modelManager.setManualModelPath(null)
                when (result) {
                    is ModelManagerService.ModelValidationResult.Success -> {
                        _state.value = _state.value.copy(
                            validationResult = result.message
                        )
                        refreshModelInfo()
                    }

                    is ModelManagerService.ModelValidationResult.Error -> {
                        _state.value = _state.value.copy(
                            validationResult = "Error clearing manual path: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DebugSettingsVM", "Error clearing manual path", e)
                _state.value = _state.value.copy(
                    validationResult = "Error: ${e.message}"
                )
            }
        }
    }

    private fun testModel() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(
                    isLoading = true,
                    testResult = null
                )

                Log.d("DebugSettingsVM", "Starting model test...")

                // Clean up existing engine
                gemma3nEngine.cleanup()

                // Try to initialize the engine with current model
                val initResult = gemma3nEngine.initialize()

                if (initResult.isSuccess) {
                    // Test with a simple generation
                    val response = gemma3nEngine.generateTextResponse("Test: What is 2+2?")

                    when (response) {
                        is Gemma3nEngine.EngineResult.Success -> {
                            _state.value = _state.value.copy(
                                testResult = "Model test successful! Response generated in ${response.metrics.durationMs}ms",
                                isLoading = false
                            )
                        }

                        is Gemma3nEngine.EngineResult.Error -> {
                            _state.value = _state.value.copy(
                                testResult = "Model loaded but generation failed: ${response.message}",
                                isLoading = false
                            )
                        }

                        is Gemma3nEngine.EngineResult.Loading -> {
                            _state.value = _state.value.copy(
                                testResult = "Model test timed out",
                                isLoading = false
                            )
                        }
                    }
                } else {
                    val error = initResult.exceptionOrNull()
                    _state.value = _state.value.copy(
                        testResult = "Model initialization failed: ${error?.message ?: "Unknown error"}",
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                Log.e("DebugSettingsVM", "Error testing model", e)
                _state.value = _state.value.copy(
                    testResult = "Test failed with exception: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun dismissTestResult() {
        _state.value = _state.value.copy(testResult = null)
    }

    private fun refreshModelInfo() {
        viewModelScope.launch {
            try {
                val modelInfo = modelManager.getCurrentModelInfo()
                _state.value = _state.value.copy(modelInfo = modelInfo)

                Log.d("DebugSettingsVM", "Model info refreshed: $modelInfo")
            } catch (e: Exception) {
                Log.e("DebugSettingsVM", "Error refreshing model info", e)
            }
        }
    }
}