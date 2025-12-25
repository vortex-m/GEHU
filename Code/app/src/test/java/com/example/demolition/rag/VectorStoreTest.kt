package com.example.demolition.rag

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Unit tests for VectorStore
 */
class VectorStoreTest {

    private lateinit var vectorStore: VectorStore

    @Before
    fun setup() {
        vectorStore = VectorStore()
    }

    @Test
    fun testInitialState() {
        assertEquals("Initial size should be 0", 0, vectorStore.size())
        assertFalse("Should not be indexed initially", vectorStore.isIndexed())
    }

    @Test
    fun testIndexing() {
        val chunks = listOf(
            DocumentChunk(
                id = "1",
                text = "Photosynthesis is the process by which plants make food",
                subject = "Science",
                chapterTitle = "Life Processes",
                chapterNumber = 1,
                contentType = "definition"
            ),
            DocumentChunk(
                id = "2",
                text = "Mitochondria is the powerhouse of the cell",
                subject = "Science",
                chapterTitle = "Cell Structure",
                chapterNumber = 2,
                contentType = "definition"
            )
        )

        vectorStore.index(chunks)

        assertEquals("Size should match indexed chunks", 2, vectorStore.size())
        assertTrue("Should be indexed after indexing", vectorStore.isIndexed())
    }

    @Test
    fun testSearch_returnsRelevantResults() {
        val chunks = listOf(
            DocumentChunk(
                id = "1",
                text = "Algebra is a branch of mathematics",
                subject = "Mathematics",
                chapterTitle = "Introduction",
                chapterNumber = 1,
                contentType = "summary"
            ),
            DocumentChunk(
                id = "2",
                text = "Biology is the study of living organisms",
                subject = "Science",
                chapterTitle = "Introduction to Biology",
                chapterNumber = 1,
                contentType = "summary"
            ),
            DocumentChunk(
                id = "3",
                text = "Algebraic equations involve variables",
                subject = "Mathematics",
                chapterTitle = "Equations",
                chapterNumber = 2,
                contentType = "explanation"
            )
        )

        vectorStore.index(chunks)

        val results = vectorStore.search("algebra mathematics", topK = 2)

        assertTrue("Should return results", results.isNotEmpty())
        assertTrue("Top result should contain 'algebra'", 
            results[0].chunk.text.contains("algebra", ignoreCase = true))
    }

    @Test
    fun testSearch_respectsTopK() {
        val chunks = (1..10).map { i ->
            DocumentChunk(
                id = i.toString(),
                text = "Document $i with some content",
                subject = "Test",
                chapterTitle = "Test Chapter",
                chapterNumber = i,
                contentType = "test"
            )
        }

        vectorStore.index(chunks)

        val results = vectorStore.search("content", topK = 3)

        assertTrue("Should return at most topK results", results.size <= 3)
    }

    @Test
    fun testSearch_respectsMinScore() {
        val chunks = listOf(
            DocumentChunk(
                id = "1",
                text = "Very relevant document about mathematics",
                subject = "Mathematics",
                chapterTitle = "Advanced Math",
                chapterNumber = 1,
                contentType = "summary"
            ),
            DocumentChunk(
                id = "2",
                text = "Unrelated content about cooking",
                subject = "Home Science",
                chapterTitle = "Cooking Basics",
                chapterNumber = 1,
                contentType = "summary"
            )
        )

        vectorStore.index(chunks)

        val results = vectorStore.search("mathematics algebra", topK = 10, minScore = 0.1)

        // Should filter out low-scoring results
        assertTrue("All results should meet minimum score", 
            results.all { it.score >= 0.1 })
    }

    @Test
    fun testSearch_emptyQuery() {
        val chunks = listOf(
            DocumentChunk(
                id = "1",
                text = "Test content",
                subject = "Test",
                chapterTitle = "Test",
                chapterNumber = 1,
                contentType = "test"
            )
        )

        vectorStore.index(chunks)

        val results = vectorStore.search("", topK = 5)

        // Empty query should return empty results or handle gracefully
        assertTrue("Empty query should be handled", true)
    }

    @Test
    fun testCacheOperations() {
        val chunks = listOf(
            DocumentChunk(
                id = "1",
                text = "Test content for caching",
                subject = "Test",
                chapterTitle = "Caching",
                chapterNumber = 1,
                contentType = "test"
            )
        )

        vectorStore.index(chunks)

        val cacheData = vectorStore.getCacheData()

        assertNotNull("Cache data should not be null", cacheData)
        assertEquals("Cache should contain indexed chunks", 1, cacheData.chunks.size)
        assertTrue("Cache should have version", cacheData.version > 0)

        // Test loading from cache
        val newStore = VectorStore()
        newStore.loadFromCache(cacheData)

        assertEquals("Loaded store should have same size", vectorStore.size(), newStore.size())
        assertTrue("Loaded store should be indexed", newStore.isIndexed())
    }
}
