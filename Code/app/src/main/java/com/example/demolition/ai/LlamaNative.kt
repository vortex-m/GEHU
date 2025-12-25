package com.example.demolition.ai

import android.util.Log

object LlamaNative {

    private const val TAG = "LlamaNative"
    private var librariesLoaded = false
    private var loadError: String? = null

    init {
        try {
            System.loadLibrary("omp")
            System.loadLibrary("ggml-base")
            System.loadLibrary("ggml-cpu")
            System.loadLibrary("ggml")
            System.loadLibrary("llama")
            System.loadLibrary("llama_jni")
            librariesLoaded = true
            Log.d(TAG, "Native libraries loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            loadError = "Failed to load native libraries: ${e.message}"
            Log.e(TAG, loadError, e)
        } catch (e: Exception) {
            loadError = "Unexpected error loading native libraries: ${e.message}"
            Log.e(TAG, loadError, e)
        }
    }

    fun isLibraryLoaded(): Boolean = librariesLoaded

    fun getLoadError(): String? = loadError

    @Throws(IllegalStateException::class)
    external fun loadModel(path: String): Long

    @Throws(IllegalStateException::class)
    external fun createContext(modelPtr: Long): Long

    @Throws(IllegalStateException::class)
    external fun generateText(ctxPtr: Long, prompt: String): String

    external fun freeContext(ctxPtr: Long)
    
    external fun freeModel(modelPtr: Long)
    
    @Throws(IllegalStateException::class)
    external fun generate(modelPath: String, prompt: String): String
}
