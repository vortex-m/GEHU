package com.example.demolition.rag

import android.util.Log

/**
 * In-memory vector store for semantic search.
 * Stores document chunks with their TF-IDF vectors and provides similarity search.
 */
class VectorStore {

    private val TAG = "VectorStore"
    
    // Stored documents and their vectors
    private val documents = mutableListOf<DocumentChunk>()
    private val vectors = mutableListOf<Map<String, Double>>()
    
    // TF-IDF embedder
    private val embedder = TFIDFEmbedder()
    
    /**
     * Indexes a list of documents.
     * Fits the TF-IDF model and computes vectors for all documents.
     * 
     * @param chunks List of DocumentChunk objects to index
     */
    fun index(chunks: List<DocumentChunk>) {
        Log.d(TAG, "Indexing ${chunks.size} documents...")
        
        documents.clear()
        vectors.clear()
        
        // Fit TF-IDF on all document texts
        val texts = chunks.map { it.text }
        embedder.fit(texts)
        
        // Compute and store vectors
        for (chunk in chunks) {
            documents.add(chunk)
            vectors.add(embedder.transform(chunk.text))
        }
        
        Log.d(TAG, "Indexing complete. ${documents.size} documents ready for search.")
    }

    /**
     * Searches for the most relevant documents given a query.
     * 
     * @param query Search query text
     * @param topK Number of top results to return (default: 5)
     * @param minScore Minimum similarity score threshold (default: 0.0)
     * @return List of SearchResult sorted by relevance (highest first)
     */
    fun search(query: String, topK: Int = 5, minScore: Double = 0.0): List<SearchResult> {
        if (documents.isEmpty()) {
            Log.w(TAG, "No documents indexed. Returning empty results.")
            return emptyList()
        }
        
        // Transform query to vector
        val queryVector = embedder.transform(query)
        
        if (queryVector.isEmpty()) {
            Log.w(TAG, "Query produced empty vector. Returning empty results.")
            return emptyList()
        }
        
        // Compute similarities
        val results = mutableListOf<SearchResult>()
        for (i in documents.indices) {
            val score = embedder.similarity(queryVector, vectors[i])
            if (score >= minScore) {
                results.add(SearchResult(documents[i], score))
            }
        }
        
        // Sort by score (descending) and take top-K
        val topResults = results
            .sortedByDescending { it.score }
            .take(topK)
        
        Log.d(TAG, "Search for '$query' returned ${topResults.size} results (top-$topK)")
        
        return topResults
    }

    /**
     * Returns the total number of indexed documents
     */
    fun size(): Int = documents.size

    /**
     * Returns true if the vector store has been indexed
     */
    fun isIndexed(): Boolean = documents.isNotEmpty()
    
    /**
     * Load from cached data (fast initialization)
     */
    fun loadFromCache(cachedData: CachedRAGData) {
        Log.d(TAG, "Loading from cache: ${cachedData.chunks.size} documents...")
        
        documents.clear()
        vectors.clear()
        
        documents.addAll(cachedData.chunks)
        vectors.addAll(cachedData.vectors)
        
        // Restore embedder state
        embedder.loadState(cachedData.documentFrequency, cachedData.totalDocuments)
        
        Log.d(TAG, "Loaded from cache successfully")
    }
    
    /**
     * Get data for caching
     */
    fun getCacheData(): CachedRAGData {
        return CachedRAGData(
            chunks = documents.toList(),
            vectors = vectors.toList(),
            documentFrequency = embedder.getDocumentFrequency(),
            totalDocuments = embedder.getTotalDocuments(),
            version = 2 // Match RAGCache.CACHE_VERSION
        )
    }
}

/**
 * Represents a search result with a document and its relevance score.
 */
data class SearchResult(
    val chunk: DocumentChunk,
    val score: Double
) {
    /**
     * Returns a display string showing the relevance score
     */
    fun getScoreDisplay(): String = String.format("%.2f", score)
}
