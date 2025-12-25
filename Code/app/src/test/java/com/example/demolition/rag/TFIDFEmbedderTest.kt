package com.example.demolition.rag

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TFIDFEmbedder
 */
class TFIDFEmbedderTest {

    @Test
    fun testFitAndTransform() {
        val embedder = TFIDFEmbedder()
        
        val documents = listOf(
            "natural numbers are counting numbers",
            "whole numbers include zero",
            "integers include positive and negative numbers"
        )
        
        embedder.fit(documents)
        assertTrue(embedder.isFitted())
        
        val vector = embedder.transform("natural numbers")
        assertTrue(vector.isNotEmpty())
        assertTrue(vector.containsKey("natural"))
        assertTrue(vector.containsKey("numbers"))
    }

    @Test
    fun testSimilarity_SimilarDocuments() {
        val embedder = TFIDFEmbedder()
        
        val documents = listOf(
            "mathematics is the study of numbers",
            "science is the study of nature",
            "english is the study of language"
        )
        
        embedder.fit(documents)
        
        val query = embedder.transform("mathematics study numbers")
        val doc1 = embedder.transform("mathematics is the study of numbers")
        val doc2 = embedder.transform("science is the study of nature")
        
        val sim1 = embedder.similarity(query, doc1)
        val sim2 = embedder.similarity(query, doc2)
        
        // Query should be more similar to doc1 than doc2
        assertTrue(sim1 > sim2)
    }

    @Test
    fun testEmptyDocument() {
        val embedder = TFIDFEmbedder()
        
        val documents = listOf("test document")
        embedder.fit(documents)
        
        val vector = embedder.transform("")
        assertTrue(vector.isEmpty())
    }
}
