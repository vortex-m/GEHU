package com.example.demolition.models

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Detects if the message contains LaTeX mathematical notation.
     * Looks for patterns like $$...$$, \(...\), \[...\], or common LaTeX commands.
     */
    fun containsMath(): Boolean {
        return text.contains(Regex("""\$\$.*?\$\$""")) ||  // Block math $$...$$
               text.contains(Regex("""\\\(.*?\\\)""")) ||   // Inline math \(...\)
               text.contains(Regex("""\\\[.*?\\\]""")) ||   // Display math \[...\]
               text.contains(Regex("""\\frac\{""")) ||       // Fractions
               text.contains(Regex("""\\sqrt\{""")) ||       // Square roots
               text.contains(Regex("""\\pm""")) ||           // Plus-minus
               text.contains("\\frac") ||
               text.contains("\\sqrt") ||
               text.contains("\\pm")
    }
}
