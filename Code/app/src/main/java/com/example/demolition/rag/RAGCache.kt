package com.example.demolition.rag

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Manages persistent caching of RAG indexed data.
 * Saves TF-IDF vectors and document frequencies to disk for fast loading.
 */
object RAGCache {
    
    private const val TAG = "RAGCache"
    private const val CACHE_DIR = "rag_cache"
    private const val CACHE_FILE = "indexed_data.json"
    private const val VERSION_FILE = "cache_version.txt"
    private const val CACHE_VERSION = 2 // Bump when data structure changes
    
    /**
     * Save indexed data to cache
     */
    fun saveCache(
        context: Context,
        chunks: List<DocumentChunk>,
        vectors: List<Map<String, Double>>,
        docFreq: Map<String, Int>,
        totalDocs: Int
    ): Boolean {
        return try {
            Log.d(TAG, "Saving cache: ${chunks.size} chunks...")
            val start = System.currentTimeMillis()
            
            val cacheDir = File(context.filesDir, CACHE_DIR)
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val cachedData = CachedRAGData(
                chunks = chunks,
                vectors = vectors,
                documentFrequency = docFreq,
                totalDocuments = totalDocs,
                version = CACHE_VERSION
            )
            
            val gson = Gson()
            val json = gson.toJson(cachedData)
            
            val cacheFile = File(cacheDir, CACHE_FILE)
            cacheFile.writeText(json)
            
            // Save version separately for quick check
            val versionFile = File(cacheDir, VERSION_FILE)
            versionFile.writeText(CACHE_VERSION.toString())
            
            val elapsed = System.currentTimeMillis() - start
            Log.d(TAG, "Cache saved successfully in ${elapsed}ms")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cache", e)
            false
        }
    }
    
    /**
     * Load indexed data from cache
     */
    fun loadCache(context: Context): CachedRAGData? {
        return try {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            val cacheFile = File(cacheDir, CACHE_FILE)
            
            if (!cacheFile.exists()) {
                Log.d(TAG, "No cache file found")
                return null
            }
            
            Log.d(TAG, "Loading cache from ${cacheFile.absolutePath}...")
            val start = System.currentTimeMillis()
            
            val json = cacheFile.readText()
            val gson = Gson()
            val type = object : TypeToken<CachedRAGData>() {}.type
            val cachedData = gson.fromJson<CachedRAGData>(json, type)
            
            val elapsed = System.currentTimeMillis() - start
            Log.d(TAG, "Cache loaded in ${elapsed}ms: ${cachedData.chunks.size} chunks")
            
            cachedData
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load cache", e)
            null
        }
    }
    
    /**
     * Check if cache is valid
     */
    fun isCacheValid(context: Context): Boolean {
        try {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            val versionFile = File(cacheDir, VERSION_FILE)
            
            if (!versionFile.exists()) {
                return false
            }
            
            val cachedVersion = versionFile.readText().toIntOrNull() ?: 0
            return cachedVersion == CACHE_VERSION
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check cache validity", e)
            return false
        }
    }
    
    /**
     * Clear cache
     */
    fun clearCache(context: Context): Boolean {
        return try {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                Log.d(TAG, "Cache cleared successfully")
                true
            } else {
                Log.d(TAG, "No cache to clear")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache", e)
            false
        }
    }
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSize(context: Context): Long {
        return try {
            val cacheDir = File(context.filesDir, CACHE_DIR)
            if (cacheDir.exists()) {
                cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Data class for cached RAG data
 */
data class CachedRAGData(
    val chunks: List<DocumentChunk>,
    val vectors: List<Map<String, Double>>,
    val documentFrequency: Map<String, Int>,
    val totalDocuments: Int,
    val version: Int
)
