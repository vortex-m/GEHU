package com.example.demolition.rag

import java.util.Locale

/**
 * Text processing utilities for the RAG pipeline.
 * Handles tokenization, normalization, and stopword removal.
 */
object TextUtils {

    // Common English stopwords to filter out
    private val STOPWORDS = setOf(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "will", "with", "what", "when", "where", "who", "which"
    )

    /**
     * Tokenizes and normalizes text for vectorization.
     * - Converts to lowercase
     * - Removes punctuation
     * - Splits into words
     * - Filters out stopwords and short tokens
     * 
     * @param text The text to process
     * @param removeStopwords Whether to filter stopwords (default: true)
     * @return List of processed tokens
     */
    fun tokenize(text: String, removeStopwords: Boolean = true): List<String> {
        // Normalize: lowercase and remove punctuation
        val normalized = text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\s]"), " ")
        
        // Split into words
        val words = normalized.split(Regex("\\s+"))
            .filter { it.length > 1 } // Remove single-char tokens
        
        // Optionally remove stopwords
        return if (removeStopwords) {
            words.filter { it !in STOPWORDS }
        } else {
            words
        }
    }

    /**
     * Normalizes text for display/comparison.
     * Trims whitespace and removes extra spaces.
     */
    fun normalize(text: String): String {
        return text.trim().replace(Regex("\\s+"), " ")
    }

    /**
     * Computes cosine similarity between two sparse vectors.
     * 
     * @param vec1 First vector as a map of term -> weight
     * @param vec2 Second vector as a map of term -> weight
     * @return Cosine similarity score [0.0, 1.0]
     */
    fun cosineSimilarity(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        if (vec1.isEmpty() || vec2.isEmpty()) return 0.0
        
        // Compute dot product
        var dotProduct = 0.0
        for ((term, weight1) in vec1) {
            val weight2 = vec2[term] ?: continue
            dotProduct += weight1 * weight2
        }
        
        // Compute magnitudes
        val mag1 = kotlin.math.sqrt(vec1.values.sumOf { it * it })
        val mag2 = kotlin.math.sqrt(vec2.values.sumOf { it * it })
        
        if (mag1 == 0.0 || mag2 == 0.0) return 0.0
        
        return dotProduct / (mag1 * mag2)
    }
}
