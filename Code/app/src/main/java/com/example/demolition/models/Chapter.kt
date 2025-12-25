package com.example.demolition.models

/**
 * Generic Chapter model used for all subjects.
 * - chapter_number is nullable because some files may not include it.
 * - summary and lists are nullable to allow flexible JSON.
 */
data class Chapter(
    val chapter_number: Int? = null,
    val chapter_title: String,
    val summary: String? = null,
    val student_explanation: List<String>? = null,
    val key_definitions: List<Definition>? = null,
    val important_points: List<String>? = null,
    val formulas_and_identities: Any? = null
)
