package com.example.demolition.rag

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TextUtils
 */
class TextUtilsTest {

    @Test
    fun testTokenization() {
        val text = "This is a test sentence with numbers 123!"
        val tokens = TextUtils.tokenize(text)
        
        // Should lowercase and remove punctuation
        assertTrue(tokens.contains("test"))
        assertTrue(tokens.contains("sentence"))
        assertTrue(tokens.contains("numbers"))
        assertTrue(tokens.contains("123"))
        
        // Should remove stopwords
        assertFalse(tokens.contains("is"))
        assertFalse(tokens.contains("a"))
        assertFalse(tokens.contains("with"))
    }

    @Test
    fun testCosineSimilarity_IdenticalVectors() {
        val vec1 = mapOf("hello" to 1.0, "world" to 1.0)
        val vec2 = mapOf("hello" to 1.0, "world" to 1.0)
        
        val similarity = TextUtils.cosineSimilarity(vec1, vec2)
        assertEquals(1.0, similarity, 0.01)
    }

    @Test
    fun testCosineSimilarity_OrthogonalVectors() {
        val vec1 = mapOf("hello" to 1.0)
        val vec2 = mapOf("world" to 1.0)
        
        val similarity = TextUtils.cosineSimilarity(vec1, vec2)
        assertEquals(0.0, similarity, 0.01)
    }

    @Test
    fun testCosineSimilarity_PartialOverlap() {
        val vec1 = mapOf("hello" to 1.0, "world" to 1.0)
        val vec2 = mapOf("hello" to 1.0, "goodbye" to 1.0)
        
        val similarity = TextUtils.cosineSimilarity(vec1, vec2)
        assertTrue(similarity > 0.0 && similarity < 1.0)
    }

    @Test
    fun testNormalize() {
        val text = "  Multiple   spaces   here  "
        val normalized = TextUtils.normalize(text)
        
        assertEquals("Multiple spaces here", normalized)
    }
}
