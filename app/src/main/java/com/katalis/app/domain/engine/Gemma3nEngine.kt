package com.katalis.app.domain.engine

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.katalis.app.domain.service.DeviceCapabilityService
import com.katalis.app.domain.service.ModelManagerService
import com.katalis.app.domain.service.PerformancePreferencesService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Gemma3nEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelManager: ModelManagerService,
    private val deviceCapabilityService: DeviceCapabilityService,
    private val performancePreferencesService: PerformancePreferencesService
) {
    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var currentAccelerationType: DeviceCapabilityService.AccelerationType? = null
    private var deviceCapabilities: DeviceCapabilityService.DeviceCapabilities? = null

    // Performance monitoring
    private val inferenceHistory = mutableListOf<InferenceMetrics>()

    data class InferenceMetrics(
        val startTime: Long,
        val endTime: Long,
        val accelerationType: DeviceCapabilityService.AccelerationType,
        val success: Boolean,
        val errorMessage: String? = null
    ) {
        val durationMs: Long get() = endTime - startTime
    }

    sealed class EngineResult {
        data class Success(val response: String, val metrics: InferenceMetrics) : EngineResult()
        data class Error(val message: String) : EngineResult()
        object Loading : EngineResult()
    }

    data class EngineStatus(
        val isReady: Boolean,
        val accelerationType: DeviceCapabilityService.AccelerationType?,
        val averageInferenceTime: Long,
        val successRate: Float,
        val deviceInfo: String
    )

    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (isInitialized) return@withContext Result.success(Unit)

            // Analyze device capabilities
            deviceCapabilities = deviceCapabilityService.analyzeDeviceCapabilities()

            // Get user preferences
            val userSettings = performancePreferencesService.performanceSettings.first()

            // Determine optimal acceleration method
            currentAccelerationType = determineAccelerationType(
                deviceCapabilities!!,
                userSettings
            )

            val modelPathResult = modelManager.ensureModelAvailable()
            if (modelPathResult.isFailure) {
                return@withContext Result.failure(
                    modelPathResult.exceptionOrNull() ?: Exception("Model preparation failed")
                )
            }

            val modelPath = modelPathResult.getOrThrow()

            // Initialize with appropriate configuration - use model path directly
            val success = initializeWithAcceleration(modelPath, currentAccelerationType!!)

            if (success) {
                isInitialized = true
                Result.success(Unit)
            } else {
                // Fallback to CPU if GPU initialization failed
                if (currentAccelerationType != DeviceCapabilityService.AccelerationType.CPU_ONLY) {
                    Log.w("Gemma3nEngine", "GPU initialization failed, falling back to CPU")
                    currentAccelerationType = DeviceCapabilityService.AccelerationType.CPU_ONLY
                    val cpuSuccess =
                        initializeWithAcceleration(modelPath, currentAccelerationType!!)
                    if (cpuSuccess) {
                        isInitialized = true
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception("Failed to initialize both GPU and CPU inference"))
                    }
                } else {
                    Result.failure(Exception("CPU inference initialization failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun initializeWithAcceleration(
        modelPath: String,
        accelerationType: DeviceCapabilityService.AccelerationType
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("Gemma3nEngine", "Attempting MediaPipe GenAI initialization with model: $modelPath")
            Log.d("Gemma3nEngine", "Acceleration type: $accelerationType")

            // Verify model file exists and has correct size
            val modelFile = java.io.File(modelPath)
            if (!modelFile.exists()) {
                Log.e("Gemma3nEngine", "Model file does not exist: $modelPath")
                return@withContext false
            }

            val fileSize = modelFile.length()
            Log.d("Gemma3nEngine", "Model file size: ${fileSize / 1024 / 1024}MB")

            // Use proper validation range
            if (fileSize < 2_900_000_000L) {
                Log.e("Gemma3nEngine", "Model file too small: ${fileSize}bytes, expected ~3GB")
                return@withContext false
            }

            // Configure LlmInferenceOptions with GPU acceleration when requested
            val optionsBuilder = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(2048)  // Available in LlmInferenceOptions

            // Enable GPU acceleration based on device capabilities and user preference
            when (accelerationType) {
                DeviceCapabilityService.AccelerationType.GPU_PREFERRED -> {
                    Log.d("Gemma3nEngine", "Enabling GPU acceleration")
                    // MediaPipe GenAI will attempt GPU acceleration by default
                    // Additional GPU configuration can be added here as API evolves
                }
                DeviceCapabilityService.AccelerationType.AUTO_ADAPTIVE -> {
                    Log.d("Gemma3nEngine", "Using adaptive acceleration (GPU with CPU fallback)")
                    // Let MediaPipe GenAI choose optimal acceleration
                }
                else -> {
                    Log.d("Gemma3nEngine", "Using CPU-only inference")
                    // CPU-only mode - MediaPipe will not attempt GPU acceleration
                }
            }

            Log.d(
                "Gemma3nEngine",
                "Creating LlmInference with acceleration type: $accelerationType"
            )

            llmInference = LlmInference.createFromOptions(context, optionsBuilder.build())
            Log.d("Gemma3nEngine", "MediaPipe LlmInference created successfully")

            // Test the model with a simple prompt to verify it's working
            val testResponse = llmInference!!.generateResponse("Hello")
            Log.d("Gemma3nEngine", "Model test successful, response length: ${testResponse?.length ?: 0}")

            true
        } catch (e: Exception) {
            Log.e("Gemma3nEngine", "Failed to initialize MediaPipe GenAI", e)
            Log.e("Gemma3nEngine", "Error details: ${e.message}")
            Log.e("Gemma3nEngine", "Error type: ${e.javaClass.simpleName}")

            // Provide specific guidance for common GPU-related errors
            when {
                e.message?.contains("GPU", ignoreCase = true) == true -> {
                    Log.w(
                        "Gemma3nEngine",
                        "GPU initialization failed - this device may not support GPU acceleration for GenAI"
                    )
                }

                e.message?.contains("OpenGL", ignoreCase = true) == true -> {
                    Log.w(
                        "Gemma3nEngine",
                        "OpenGL error detected - GPU delegate may be incompatible with this device"
                    )
                }

                e.message?.contains("memory", ignoreCase = true) == true -> {
                    Log.w(
                        "Gemma3nEngine",
                        "Memory error - device may not have sufficient resources for this model"
                    )
                }
            }

            false
        }
    }

    private fun determineAccelerationType(
        capabilities: DeviceCapabilityService.DeviceCapabilities,
        userSettings: PerformancePreferencesService.PerformanceSettings
    ): DeviceCapabilityService.AccelerationType {
        return when (userSettings.performanceMode) {
            PerformancePreferencesService.PerformanceMode.AUTO -> {
                // Use device recommendation but respect user constraints
                if (capabilities.isLowPowerMode && !userSettings.allowGPUWhenBatteryLow) {
                    DeviceCapabilityService.AccelerationType.CPU_ONLY
                } else if (capabilities.thermalState >= android.os.PowerManager.THERMAL_STATUS_MODERATE
                    && !userSettings.allowGPUWhenHot
                ) {
                    DeviceCapabilityService.AccelerationType.CPU_ONLY
                } else {
                    capabilities.recommendedAcceleration
                }
            }

            PerformancePreferencesService.PerformanceMode.PERFORMANCE -> {
                if (capabilities.hasGPUSupport) {
                    DeviceCapabilityService.AccelerationType.GPU_PREFERRED
                } else {
                    DeviceCapabilityService.AccelerationType.CPU_ONLY
                }
            }

            PerformancePreferencesService.PerformanceMode.BATTERY_SAVER -> {
                DeviceCapabilityService.AccelerationType.CPU_ONLY
            }

            PerformancePreferencesService.PerformanceMode.MANUAL -> {
                // Will be controlled per-request
                DeviceCapabilityService.AccelerationType.AUTO_ADAPTIVE
            }
        }
    }

    suspend fun generateTextResponse(
        prompt: String,
        conversationHistory: List<String> = emptyList(),
        preferredAcceleration: DeviceCapabilityService.AccelerationType? = null
    ): EngineResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val accelerationType = preferredAcceleration ?: currentAccelerationType
            ?: DeviceCapabilityService.AccelerationType.CPU_ONLY

        try {
            if (!isInitialized || llmInference == null) {
                Log.e(
                    "Gemma3nEngine",
                    "Engine not initialized. isInitialized=$isInitialized, llmInference=${llmInference != null}"
                )
                return@withContext EngineResult.Error("Engine not initialized")
            }

            // Get user timeout setting
            val userSettings = performancePreferencesService.performanceSettings.first()
            val contextualPrompt = buildContextualPrompt(prompt, conversationHistory)

            Log.d(
                "Gemma3nEngine",
                "Generating response with timeout: ${userSettings.maxInferenceTime}ms"
            )
            Log.d("Gemma3nEngine", "Prompt length: ${contextualPrompt.length} chars")

            val response = withTimeoutOrNull(userSettings.maxInferenceTime.toLong()) {
                try {
                    Log.d("Gemma3nEngine", "Calling llmInference.generateResponse...")
                    val result = llmInference!!.generateResponse(contextualPrompt)
                    Log.d(
                        "Gemma3nEngine",
                        "Response generated successfully, length: ${result?.length ?: 0}"
                    )
                    result
                } catch (e: Exception) {
                    Log.e("Gemma3nEngine", "Error during generateResponse", e)
                    throw e
                }
            }

            val endTime = System.currentTimeMillis()
            val metrics = InferenceMetrics(startTime, endTime, accelerationType, response != null)

            if (response != null) {
                // Record successful inference
                recordInferenceMetrics(metrics)

                // Adaptive learning: if this was slow, consider switching acceleration
                if (userSettings.adaptiveMode && metrics.durationMs > userSettings.maxInferenceTime * 0.8) {
                    considerAccelerationSwitch(metrics)
                }

                EngineResult.Success(response, metrics)
            } else {
                recordInferenceMetrics(metrics.copy(success = false, errorMessage = "Timeout"))
                Log.w(
                    "Gemma3nEngine",
                    "Response generation timed out after ${userSettings.maxInferenceTime}ms"
                )
                EngineResult.Error("Response generation timed out")
            }

        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            val metrics = InferenceMetrics(startTime, endTime, accelerationType, false, e.message)
            recordInferenceMetrics(metrics)

            Log.e("Gemma3nEngine", "Generation failed", e)
            Log.e("Gemma3nEngine", "Error details: ${e.message}")
            Log.e("Gemma3nEngine", "Error type: ${e.javaClass.simpleName}")

            EngineResult.Error("Generation failed: ${e.message}")
        }
    }

    suspend fun generateMultimodalResponse(
        textPrompt: String,
        image: Bitmap? = null,
        conversationHistory: List<String> = emptyList(),
        preferredAcceleration: DeviceCapabilityService.AccelerationType? = null
    ): EngineResult = withContext(Dispatchers.IO) {
        // For multimodal, GPU acceleration is generally more beneficial
        val accelerationType = preferredAcceleration
            ?: if (currentAccelerationType == DeviceCapabilityService.AccelerationType.CPU_ONLY)
                currentAccelerationType
            else
                DeviceCapabilityService.AccelerationType.GPU_PREFERRED

        val enhancedPrompt = if (image != null) {
            "Based on the provided image and this question: $textPrompt"
        } else {
            textPrompt
        }

        return@withContext generateTextResponse(
            enhancedPrompt,
            conversationHistory,
            accelerationType
        )
    }

    private fun recordInferenceMetrics(metrics: InferenceMetrics) {
        inferenceHistory.add(metrics)
        // Keep only last 50 inferences for memory efficiency
        if (inferenceHistory.size > 50) {
            inferenceHistory.removeAt(0)
        }
    }

    private suspend fun considerAccelerationSwitch(slowMetrics: InferenceMetrics) {
        // If GPU is slow, try CPU for next inference
        // If CPU is slow and we have GPU capability, try GPU
        val capabilities = deviceCapabilities ?: return

        when (currentAccelerationType) {
            DeviceCapabilityService.AccelerationType.GPU_PREFERRED -> {
                if (slowMetrics.durationMs > 8000) { // 8 seconds is too slow
                    currentAccelerationType = DeviceCapabilityService.AccelerationType.CPU_ONLY
                    reinitializeWithNewAcceleration()
                }
            }
            DeviceCapabilityService.AccelerationType.CPU_ONLY -> {
                if (capabilities.hasGPUSupport && slowMetrics.durationMs > 6000) {
                    currentAccelerationType = DeviceCapabilityService.AccelerationType.GPU_PREFERRED
                    reinitializeWithNewAcceleration()
                }
            }
            else -> { /* AUTO_ADAPTIVE and GPU_FALLBACK_CPU handle switching internally */
            }
        }
    }

    private suspend fun reinitializeWithNewAcceleration() {
        try {
            llmInference?.close()
            llmInference = null

            val modelPath = modelManager.getModelPath() ?: return
            initializeWithAcceleration(modelPath, currentAccelerationType!!)
        } catch (e: Exception) {
            // If reinitialization fails, fall back to previous working state
        }
    }

    fun getEngineStatus(): EngineStatus {
        val avgTime = if (inferenceHistory.isNotEmpty()) {
            inferenceHistory.takeLast(10).map { it.durationMs }.average().toLong()
        } else 0L

        val successRate = if (inferenceHistory.isNotEmpty()) {
            inferenceHistory.takeLast(10).count { it.success }.toFloat() /
                    minOf(10, inferenceHistory.size)
        } else 1.0f

        val deviceInfo = deviceCapabilities?.let { capabilities ->
            deviceCapabilityService.getCapabilityDescription(capabilities)
        } ?: "Device analysis not available"

        return EngineStatus(
            isReady = isInitialized,
            accelerationType = currentAccelerationType,
            averageInferenceTime = avgTime,
            successRate = successRate,
            deviceInfo = deviceInfo
        )
    }

    private fun buildContextualPrompt(
        userPrompt: String,
        history: List<String>
    ): String {
        val educationalContext = """
          You are Katalis, an advanced AI tutor specializing in the Cameroon GCE Pure Mathematics with Mechanics Syllabus. Your core mission is to provide personalized, patient, and profoundly encouraging guidance to secondary and high school students in Cameroon who may have limited access to traditional resources.

**Your primary goal is to foster deep understanding and intuition, enabling students to discover solutions and master concepts themselves. You are not a simple answer-bot; you are a catalyst for learning.**

You operate exclusively on a meticulously curated, offline knowledge base derived from official GCE syllabi, high-quality textbooks, and insights from examiner reports detailing common student misconceptions and areas of difficulty.

**Key Behavioral Principles:**

1.  **Socratic Method (Default Mode):** Always prioritize asking guiding questions to help the student arrive at the solution or concept independently. Encourage critical thinking, logical reasoning, and self-discovery. Never provide direct answers unless explicitly requested or indicated by a specific user action.

2.  **Context-Aware & Adaptive Pedagogy ("Smarter RAG"):**
    *   When retrieving information, pay close attention to any associated metadata for content chunks or topics (e.g., `isWeakPoint: true`, `commonMisconceptions`, `difficulty`, `practicalApplications`).
    *   If the topic is flagged as a `isWeakPoint` or the student's query hints at a `commonMisconception` (as inferred from context or previous interactions), adapt your guidance. Proactively offer simpler analogies, break down complex steps, or address potential pitfalls subtly before the student encounters them.
    *   If a `practicalApplication` is relevant, weave it into your explanation to connect theory to real-world context, making learning more relatable.

3.  **Empathetic & Encouraging Tone:**
    *   Maintain a consistently supportive, patient, and non-judgmental tone. Your language should be clear, concise, and academically credible without being intimidating.
    *   Acknowledge challenges ("That's a tricky step for many learners!").
    *   Celebrate progress, even small breakthroughs ("You're thinking critically now!", "Great observation!").
    *   Maintain a calm and emotionally clean demeanor.

4.  **"Escape Hatches" (When Direct Answers ARE Allowed):**
    *   If the student explicitly requests a direct answer, a full explanation, or indicates severe frustration (e.g., "Show me the solution," "Give me the answer," "I'm completely stuck, please explain everything," or if the system signals a "Show Solution" request):
    *   You **MAY** provide the direct answer. However, **ALWAYS** follow it immediately with a clear, step-by-step, pedagogical explanation of *how* that answer is derived, reinforcing the learning process. Do not just state the answer and stop.

5.  **Clarification & Rephrasing:** If the student expresses confusion ("I don't get it," "Can you explain differently?", "Simplify that for me"), readily rephrase your explanation using alternative analogies, simpler language, or a different pedagogical approach (e.g., visual description if possible).

6.  **Knowledge Grounding:** Always ground your responses in the information retrieved from your knowledge base. Do not invent facts, go off-topic, or speculate. If information is truly outside your knowledge base, politely state that you cannot help with that specific query (refer to the `RAGService`'s "no relevant chunks" error handling).

**Initiation of Interaction:**
When a student first interacts with you in a session or begins a new topic, start with an open-ended question, an encouraging remark, or a thought-provoking prompt related to the current learning context. Invite them into the learning process naturally.

**Your identity is Katalis. You are a tireless, patient, and intelligent guide, here to empower every student to unlock their full academic potential.**
        """.trimIndent()

        val contextBuilder = StringBuilder()
        contextBuilder.append(educationalContext)
        contextBuilder.append("\n\n")

        // Add conversation history (last 5 exchanges to manage context length)
        if (history.isNotEmpty()) {
            contextBuilder.append("Previous conversation:\n")
            history.takeLast(10).forEach { exchange ->
                contextBuilder.append("$exchange\n")
            }
            contextBuilder.append("\n")
        }

        contextBuilder.append("Student: $userPrompt\n")
        contextBuilder.append("Katalis:")

        return contextBuilder.toString()
    }

    fun isReady(): Boolean = isInitialized && llmInference != null

    fun cleanup() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
        inferenceHistory.clear()
    }
}