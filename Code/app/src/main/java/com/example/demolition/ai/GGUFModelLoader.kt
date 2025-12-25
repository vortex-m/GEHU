package com.example.demolition.ai

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

object GGUFModelLoader {

    private const val TAG = "GGUFModelLoader"
    private const val MODEL_ASSET_PATH = "models/gemma1.gguf"
    private const val MODEL_FILE_NAME = "gemma1.gguf"

    /**
     * Loads the GGUF model from assets to app's internal storage
     * @return Absolute path to the model file
     * @throws IOException if model file cannot be loaded
     * @throws FileNotFoundException if model file doesn't exist in assets
     */
    @Throws(IOException::class, FileNotFoundException::class)
    suspend fun loadModel(context: Context): String {
        val outFile = File(context.filesDir, MODEL_FILE_NAME)

        // If already exists, verify it's valid and return path
        if (outFile.exists()) {
            if (outFile.length() > 0) {
                Log.d(TAG, "Model already exists: ${outFile.absolutePath} (${outFile.length()} bytes)")
                return outFile.absolutePath
            } else {
                Log.w(TAG, "Existing model file is empty, re-copying...")
                outFile.delete()
            }
        }

        // Ensure parent directory exists
        outFile.parentFile?.mkdirs()

        try {
            Log.d(TAG, "Loading model from assets: $MODEL_ASSET_PATH")
            
            context.assets.open(MODEL_ASSET_PATH).use { input ->
                outFile.outputStream().use { output ->
                    val bytesCopied = input.copyTo(output)
                    output.flush()
                    Log.d(TAG, "Model copied successfully: $bytesCopied bytes")
                }
            }

            // Verify the file was created successfully
            if (!outFile.exists() || outFile.length() == 0L) {
                throw IOException("Model file was not created properly")
            }

            Log.d(TAG, "Model ready at: ${outFile.absolutePath}")
            return outFile.absolutePath

        } catch (e: FileNotFoundException) {
            val errorMsg = "Model file not found in assets: $MODEL_ASSET_PATH. " +
                    "Please ensure the model file exists in app/src/main/assets/models/"
            Log.e(TAG, errorMsg, e)
            throw FileNotFoundException(errorMsg)
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy model file", e)
            // Clean up partial file if exists
            if (outFile.exists()) {
                outFile.delete()
            }
            throw IOException("Failed to load model: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error loading model", e)
            // Clean up partial file if exists
            if (outFile.exists()) {
                outFile.delete()
            }
            throw IOException("Unexpected error: ${e.message}", e)
        }
    }
}
