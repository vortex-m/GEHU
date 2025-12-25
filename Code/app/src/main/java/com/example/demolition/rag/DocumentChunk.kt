package com.example.demolition.rag

/**
 * Represents a searchable chunk of educational content.
 * Each chunk contains a piece of information from the curriculum
 * along with metadata for filtering and display.
 */
data class DocumentChunk(
    val id: String,              // Unique identifier
    val text: String,            // The actual content to search
    val subject: String,         // e.g., "Mathematics", "English"
    val chapterTitle: String,    // e.g., "Number Systems"
    val chapterNumber: Int?,     // Chapter number (nullable)
    val contentType: String      // e.g., "summary", "definition", "formula", "explanation"
) {
    /**
     * Returns a display-friendly representation of this chunk's source
     */
    fun getSourceDisplay(): String {
        return if (chapterNumber != null) {
            "$subject - Chapter $chapterNumber: $chapterTitle"
        } else {
            "$subject - $chapterTitle"
        }
    }
}
