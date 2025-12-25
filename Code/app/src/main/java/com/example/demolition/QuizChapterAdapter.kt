package com.example.demolition.quiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.demolition.R

class QuizChapterAdapter(
    private val chapters: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<QuizChapterAdapter.QuizChapterHolder>() {

    inner class QuizChapterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvQuizChapterName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizChapterHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_chapter, parent, false)
        return QuizChapterHolder(view)
    }

    override fun onBindViewHolder(holder: QuizChapterHolder, position: Int) {
        val chapter = chapters[position]
        holder.tvTitle.text = chapter

        holder.itemView.setOnClickListener {
            onClick(chapter)
        }
    }

    override fun getItemCount(): Int = chapters.size
}
