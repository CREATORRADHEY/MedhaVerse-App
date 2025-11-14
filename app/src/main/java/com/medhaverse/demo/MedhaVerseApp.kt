package com.medhaverse.demo

import android.app.Application
import android.util.Log
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedhaVerseApp : Application() {

    companion object {
        private const val TAG = "MedhaVerse"
        const val WASTE_CLASSIFIER_MODEL = "waste_classifier"
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🚀 MedhaVerse App Starting...")

        // Initialize RunAnywhere SDK in background
        applicationScope.launch {
            initializeAI()
        }
    }

    private suspend fun initializeAI() {
        try {
            Log.d(TAG, "Initializing RunAnywhere SDK...")

            // Step 1: Initialize SDK
            RunAnywhere.initialize(
                context = this@MedhaVerseApp,
                apiKey = "medhaverse_hackathon_2025",
                environment = SDKEnvironment.DEVELOPMENT
            )

            // Step 2: Register LLM Service Provider
            LlamaCppServiceProvider.register()
            Log.d(TAG, "✅ LLM Service Provider registered")

            // Step 3: Register AI Model for waste classification
            // Using SmolLM2 360M - smallest and fastest (119 MB)
            addModelFromURL(
                url = "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                name = "Waste Classifier - SmolLM2",
                type = "LLM"
            )
            Log.d(TAG, "✅ Waste classification model registered")

            // Step 4: Scan for previously downloaded models
            RunAnywhere.scanForDownloadedModels()

            Log.i(TAG, "✅ RunAnywhere SDK initialized successfully!")

        } catch (e: Exception) {
            Log.e(TAG, "❌ SDK initialization failed", e)
        }
    }
}
