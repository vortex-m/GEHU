package com.example.demolition.models

/**
 * Generic file wrapper for your <subject>_chapters.json files.
 * JSON for each subject should map to this structure:
 * {
 *   "classX":"10",
 *   "board":"CBSE",
 *   "subject":"math",
 *   "chapters":[ ... ]
 * }
 *
 * If your JSON lacks classX/board/subject, that's okay — fields are nullable.
 */
data class SubjectBook(
    val classX: String? = null,
    val board: String? = null,
    val subject: String? = null,
    val chapters: List<Chapter>
)
