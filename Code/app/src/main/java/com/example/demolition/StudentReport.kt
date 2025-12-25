package com.example.demolition

data class StudentReport(
    val name: String,
    val studentClass: String,
    val subject: String,
    val chapter: String,
    val date: String,
    val time: String,
    val score: String,
    var synced: Boolean = false   // <-- IMPORTANT
)
