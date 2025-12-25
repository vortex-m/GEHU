package com.example.demolition.models

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Data class for storing quiz results in Firestore
 */
data class QuizResult(
    val userId: String = "",
    val studentName: String = "",
    val studentClass: String = "",
    val section: String = "",
    val subject: String = "",
    val chapterTitle: String = "",
    val chapterNumber: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val percentage: Double = 0.0,
    @ServerTimestamp
    val timestamp: Date? = null,
    val dateString: String = "",
    val timeString: String = ""
) {
    /**
     * Convert to map for Firestore storage
     */
    fun toMap(): HashMap<String, Any> {
        return hashMapOf(
            "userId" to userId,
            "studentName" to studentName,
            "studentClass" to studentClass,
            "section" to section,
            "subject" to subject,
            "chapterTitle" to chapterTitle,
            "chapterNumber" to chapterNumber,
            "score" to score,
            "totalQuestions" to totalQuestions,
            "percentage" to percentage,
            "timestamp" to FieldValue.serverTimestamp(),
            "dateString" to dateString,
            "timeString" to timeString
        )
    }
}
