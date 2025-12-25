package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentScienceBinding
import com.example.demolition.models.SubjectBook

class ScienceFrag : Fragment() {

    private var _binding: FragmentScienceBinding? = null
    private val binding get() = _binding!!

    private lateinit var chapterAdapter: ChapterAdapter
    private val subjectName = "science"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentScienceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadChapters()
    }

    private fun loadChapters() {
        // Use the unified JsonLoader to load subject chapters
        val book: SubjectBook = JsonLoader.loadSubjectChapters(requireContext(), subjectName)
        val chapters = book.chapters.map { it.chapter_title }

        chapterAdapter = ChapterAdapter(chapters) { chapterTitle ->
            val intent = Intent(requireContext(), ChapterViewer::class.java).apply {
                putExtra("chapter_title", chapterTitle)
                putExtra("subject", subjectName) // tell ChapterViewer which subject to load
            }
            startActivity(intent)
        }

        // Ensure your FragmentScienceBinding layout has a RecyclerView with id `rvScienceChapters`
        binding.rvScienceChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScienceChapters.adapter = chapterAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
