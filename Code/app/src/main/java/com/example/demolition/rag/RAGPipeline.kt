package com.example.demolition.rag

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class RAGPipeline {

    private val TAG = "RAGPipeline"
    
    private val vectorStore = VectorStore()
    private var isInitialized = false
    
    // LRU cache for query results (last 50 queries)
    private val queryCache = object : LinkedHashMap<String, RAGResult>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, RAGResult>): Boolean {
            return size > 50
        }
    }


    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "RAG pipeline already initialized")
            return@withContext
        }
        
        try {
            Log.d(TAG, "Initializing RAG pipeline...")
            
            // Try to load from cache first
            val cached = RAGCache.loadCache(context)
            if (cached != null && RAGCache.isCacheValid(context)) {
                Log.d(TAG, "Valid cache found, loading...")
                val startLoad = System.currentTimeMillis()
                
                vectorStore.loadFromCache(cached)
                
                val loadTime = System.currentTimeMillis() - startLoad
                Log.d(TAG, "✓ Cache loaded in ${loadTime}ms: ${cached.chunks.size} chunks")
                
                isInitialized = true
                return@withContext
            }
            
            // No valid cache, perform full indexing
            Log.d(TAG, "No valid cache, performing full indexing...")
            val startIndex = System.currentTimeMillis()
            
            // Step 1: Chunk the data
            val startChunk = System.currentTimeMillis()
            val chunks = DataChunker.chunkData(context)
            val chunkTime = System.currentTimeMillis() - startChunk
            Log.d(TAG, "Chunking completed in ${chunkTime}ms: ${chunks.size} chunks")
            
            // Step 2: Index the chunks
            val startVectorIndex = System.currentTimeMillis()
            vectorStore.index(chunks)
            val indexTime = System.currentTimeMillis() - startVectorIndex
            Log.d(TAG, "Indexing completed in ${indexTime}ms")
            
            // Step 3: Save to cache for next time
            val startCache = System.currentTimeMillis()
            val cacheData = vectorStore.getCacheData()
            val saved = RAGCache.saveCache(
                context,
                cacheData.chunks,
                cacheData.vectors,
                cacheData.documentFrequency,
                cacheData.totalDocuments
            )
            val cacheTime = System.currentTimeMillis() - startCache
            
            if (saved) {
                Log.d(TAG, "Cache saved in ${cacheTime}ms")
            } else {
                Log.w(TAG, "Failed to save cache")
            }
            
            val totalTime = System.currentTimeMillis() - startIndex
            Log.d(TAG, "✓ RAG pipeline initialized in ${totalTime}ms")
            
            isInitialized = true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RAG pipeline", e)
            isInitialized = false
            throw e
        }
    }


    suspend fun query(question: String, topK: Int = 5): RAGResult = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            throw IllegalStateException("RAG pipeline not initialized. Call initialize() first.")
        }
        
        // Generate cache key
        val cacheKey = "${question.lowercase().trim()}_$topK"
        
        // Check query cache first
        queryCache[cacheKey]?.let {
            Log.d(TAG, "✓ Returning cached result for: $question")
            return@withContext it
        }
        
        try {
            Log.d(TAG, "Querying: '$question'")
            
            // Retrieve relevant chunks with HIGH minimum score to avoid irrelevant content
            val startSearch = System.currentTimeMillis()
            val searchResults = vectorStore.search(question, topK, minScore = 0.10) // Balanced threshold - filters junk but allows relevant content
            val searchTime = System.currentTimeMillis() - startSearch
            
            Log.d(TAG, "Search completed in ${searchTime}ms: ${searchResults.size} results")
            
            // Log top results
            searchResults.take(3).forEachIndexed { idx, result ->
                Log.d(TAG, "  #${idx + 1} [${result.getScoreDisplay()}] ${result.chunk.getSourceDisplay()}")
            }
            
            // Only include chunks with good relevance scores (above 0.12)
            val relevantResults = searchResults.filter { it.score >= 0.12 }.take(4)
            Log.d(TAG, "Using ${relevantResults.size} highly relevant chunks")
            
            // Build context string from ONLY the most relevant chunks
            val context = buildContext(relevantResults)
            
            // Create augmented prompt
            val augmentedPrompt = buildPrompt(question, context)
            
            val result = RAGResult(
                retrievedChunks = relevantResults.map { it.chunk },
                scores = relevantResults.map { it.score },
                context = context,
                augmentedPrompt = augmentedPrompt
            )
            
            // Cache the result
            queryCache[cacheKey] = result
            
            return@withContext result
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during query", e)
            throw e
        }
    }

    /**
     * Builds a context string from retrieved chunks with their scores.
     */
    private fun buildContext(results: List<SearchResult>): String {
        if (results.isEmpty()) return ""
        
        // Include the most relevant content, cleaned of excessive markdown
        return results.joinToString("\n\n") { result ->
            val source = result.chunk.getSourceDisplay()
            val content = cleanMarkdown(result.chunk.text)
            "[From: $source]\n$content"
        }
    }
    
    /**
     * Cleans markdown formatting from text aggressively.
     * Removes ALL asterisks and converts to readable format.
     */
    private fun cleanMarkdown(text: String): String {
        return text
            // First, handle bullet points with multiple asterisks
            .replace(Regex("^\\s*\\*+\\s*", RegexOption.MULTILINE), "• ")
            // Remove bold/italic markers by removing ALL asterisks
            .replace("**", "")
            .replace("*", "")
            // Remove citation markers [cite: 12345]
            .replace(Regex("\\[cite:\\s*\\d+\\]"), "")
            // Remove other markdown symbols
            .replace("__", "")
            .replace("~~", "")
            // Clean up multiple spaces
            .replace(Regex("\\s+"), " ")
            // Clean up multiple line breaks
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    /**
     * Builds an augmented prompt with context and question.
     * Ultra-strict teacher persona - explicitly prevents conversation format.
     */
    private fun buildPrompt(question: String, context: String): String {
        return if (context.isNotBlank()) {
            """PRIMARY ROLE:
You are a human teacher teaching from a fixed syllabus.
You are NOT a chatbot, demo assistant, or conversational AI.

❌ ABSOLUTE OUTPUT BAN (CRITICAL - READ THIS):
You must NEVER output ANY of these:
- "user"
- "model"  
- "assistant"
- "User:"
- "Model:"
- "Assistant:"
- Any dialogue format
- Any conversation examples
- Any Q&A transcript format

If your draft response contains ANY of the above, you MUST rewrite it completely.

📚 KNOWLEDGE SOURCE RULE (MANDATORY):
- Answer ONLY from the curriculum information provided below
- Do NOT use general knowledge or external information
- Do NOT guess or extend beyond the context
- If context doesn't fully answer the question, say the fallback message ONLY

🎓 TEACHING STYLE (REQUIRED):
When valid context exists:
1. Start DIRECTLY with the explanation (no preamble)
2. Explain like a classroom teacher
3. Keep academic, exam-oriented tone
4. Do NOT say: "Absolutely", "Okay let's break it down", "Here's the answer"
5. Do NOT use conversational fillers
6. Do NOT refer to yourself in first person

⚡ CONCISENESS RULE (CRITICAL):
- Maximum 3-4 sentences ONLY
- Answer the EXACT question asked - nothing more
- Do NOT repeat information
- Do NOT add extra examples unless asked
- Be brief and direct

FORMATTING RULES:
- Use plain text with bullet points (•)
- No markdown: **, *, __, ~~
- Short paragraphs only (2-3 sentences)
- Professional, clean output

CURRICULUM INFORMATION:
$context

STUDENT'S QUESTION: $question

RESPOND AS A TEACHER NOW - Start with the content directly:"""
        } else {
            // No context found - ONLY output the fallback message
            """PRIMARY ROLE: You are a human teacher bound to the prescribed curriculum.

The student asked: $question

You do not have this information in your curriculum materials.

OUTPUT EXACTLY THIS MESSAGE (word-for-word, nothing else):
"No relevant context found in the provided study material. Please consult your teacher or refer to your textbook."

DO NOT add commentary.
DO NOT answer from general knowledge.
OUTPUT ONLY THE MESSAGE ABOVE."""
        }
    }

    /**
     * Returns true if the pipeline is initialized and ready to use
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Returns the number of indexed document chunks
     */
    fun getIndexSize(): Int = if (isInitialized) vectorStore.size() else 0
}

/**
 * Result of a RAG query operation.
 */
data class RAGResult(
    val retrievedChunks: List<DocumentChunk>,
    val scores: List<Double>,
    val context: String,
    val augmentedPrompt: String
) {
    /**
     * Returns true if relevant context was found
     */
    fun hasContext(): Boolean = context.isNotBlank()
    
    /**
     * Returns the number of retrieved chunks
     */
    fun getChunkCount(): Int = retrievedChunks.size
}
