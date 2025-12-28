package com.example.demolition.rag

import kotlin.math.ln

/**
 * TF-IDF (Term Frequency-Inverse Document Frequency) embedder.
 * Converts text documents into sparse vector representations for similarity search.
 */
class TFIDFEmbedder {

    // Document frequency: how many documents contain each term
    private val documentFrequency = mutableMapOf<String, Int>()
    
    // Total number of documents
    private var totalDocuments = 0
    
    // IDF cache
    private val idfCache = mutableMapOf<String, Double>()

    /**
     * Fits the embedder on a corpus of documents.
     * Computes document frequencies and IDF values.
     * 
     * @param documents List of text documents
     */
    fun fit(documents: List<String>) {
        totalDocuments = documents.size
        documentFrequency.clear()
        idfCache.clear()
        
        // Count document frequencies
        for (doc in documents) {
            val tokens = TextUtils.tokenize(doc).toSet()
            for (token in tokens) {
                documentFrequency[token] = documentFrequency.getOrDefault(token, 0) + 1
            }
        }
        
        // Pre-compute IDF values
        for ((term, df) in documentFrequency) {
            idfCache[term] = computeIDF(df)
        }
    }

    /**
     * Transforms a document into a TF-IDF vector.
     * 
     * @param text The document text
     * @return Sparse vector as map of term -> TF-IDF weight
     */
    fun transform(text: String): Map<String, Double> {
        val tokens = TextUtils.tokenize(text)
        if (tokens.isEmpty()) return emptyMap()
        
        // Compute term frequencies
        val termFreq = mutableMapOf<String, Int>()
        for (token in tokens) {
            termFreq[token] = termFreq.getOrDefault(token, 0) + 1
        }
        
        // Compute TF-IDF weights
        val vector = mutableMapOf<String, Double>()
        for ((term, freq) in termFreq) {
            val tf = freq.toDouble() / tokens.size
            val idf = idfCache[term] ?: computeIDF(1) // Use minimal IDF for unseen terms
            vector[term] = tf * idf
        }
        
        return vector
    }

    /**
     * Computes cosine similarity between a query and a document.
     * 
     * @param queryVector Query TF-IDF vector
     * @param docVector Document TF-IDF vector
     * @return Cosine similarity score [0.0, 1.0]
     */
    fun similarity(queryVector: Map<String, Double>, docVector: Map<String, Double>): Double {
        return TextUtils.cosineSimilarity(queryVector, docVector)
    }

    /**
     * Computes Inverse Document Frequency for a term.
     * IDF = log(total_docs / document_frequency)
     */
    private fun computeIDF(df: Int): Double {
        return ln((totalDocuments.toDouble() + 1) / (df + 1)) + 1.0
    }

    /**
     * Returns true if the embedder has been fitted on a corpus
     */
    fun isFitted(): Boolean {
        return totalDocuments > 0
    }
    
    /**
     * Load state from cached data
     */
    fun loadState(docFreq: Map<String, Int>, total: Int) {
        documentFrequency.clear()
        documentFrequency.putAll(docFreq)
        totalDocuments = total
        
        // Recompute IDF cache
        idfCache.clear()
        for ((term, df) in documentFrequency) {
            idfCache[term] = computeIDF(df)
        }
    }
    
    /**
     * Get document frequency map for caching
     */
    fun getDocumentFrequency(): Map<String, Int> = documentFrequency.toMap()
    
    /**
     * Get total documents count for caching
     */
    fun getTotalDocuments(): Int = totalDocuments
}
