package com.example.demolition

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.demolition.databinding.ActivityChapterViewerBinding
import com.example.demolition.models.Chapter
import com.example.demolition.models.SubjectBook   // <-- This matches your chapters JSON
import com.google.gson.Gson

class ChapterViewer : AppCompatActivity() {

    private lateinit var binding: ActivityChapterViewerBinding
    private lateinit var selectedChapter: Chapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChapterViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val chapterTitle = intent.getStringExtra("chapter_title") ?: ""
        val subject = intent.getStringExtra("subject") ?: "math"

        val book = loadSubjectChapters(subject)

        selectedChapter = book.chapters.firstOrNull {
            it.chapter_title.equals(chapterTitle, ignoreCase = true)
        } ?: return

        showChapter()
    }

    private fun loadSubjectChapters(subject: String): SubjectBook {
        val json = assets.open("${subject}_chapters.json")   // FIXED HERE ✔
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(json, SubjectBook::class.java)
    }

    private fun showChapter() {
        binding.tvChapterTitle.text = selectedChapter.chapter_title
        binding.tvSummary.text = selectedChapter.summary

        // Student explanation
        val explanationText = selectedChapter.student_explanation
            ?.joinToString("\n• ", "• ")
            ?: "No explanation available."
        binding.tvConcepts.text = explanationText

        // Key definitions
        val definitionsText = selectedChapter.key_definitions?.joinToString("\n\n") {
            "• ${it.term}: ${it.definition}"
        } ?: "No definitions."
        binding.tvDefinitions.text = definitionsText

        // Important points
        val importantPoints = selectedChapter.important_points?.joinToString("\n• ", "• ")
            ?: "No important points."
        binding.tvPoints.text = importantPoints

        // Formulas handling
        val formulasText: String = when (val f = selectedChapter.formulas_and_identities) {
            is List<*> -> f.mapNotNull { it?.toString() }.joinToString("\n• ", "• ")
            is Map<*, *> -> f.entries.joinToString("\n") { "• ${it.key}: ${it.value}" }
            is String -> f
            else -> "No formulas available."
        }
        binding.tvFormulas.text = formulasText
    }
}
