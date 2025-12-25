package com.example.demolition

import android.content.Context
import com.example.demolition.models.SubjectBook
import com.google.gson.Gson

object JsonLoader {

    /**
     * Load chapters for ANY subject.
     * Example subject names:
     *  - "math"
     *  - "science"
     *  - "sst"
     *  - "english"
     *
     * JSON file MUST be named:  <subject>_chapters.json
     */
    fun loadSubjectChapters(context: Context, subject: String): SubjectBook {
        val fileName = "${subject}_chapters.json"

        val json = context.assets.open(fileName)
            .bufferedReader()
            .use { it.readText() }

        return Gson().fromJson(json, SubjectBook::class.java)
    }
}
