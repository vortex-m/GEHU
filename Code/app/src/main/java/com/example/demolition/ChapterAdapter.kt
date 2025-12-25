package com.example.demolition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.demolition.databinding.ItemChapterBinding

class ChapterAdapter(
    private val chapters: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(val binding: ItemChapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        val title = chapters[position]

        holder.binding.tvChapterName.text = title

        holder.binding.root.setOnClickListener {
            onClick(title)
        }
    }

    override fun getItemCount(): Int = chapters.size
}
