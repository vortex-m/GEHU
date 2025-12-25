package com.example.demolition.models

data class QuizData(
    val quiz: List<QuizChapter>
)

data class QuizChapter(
    val chapter_number: String,
    val chapter_title: String,
    val questions: List<Question>
)

data class Question(
    val q: String,
    val options: List<String>,
    val answer: String
)
