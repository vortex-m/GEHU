package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentMathBinding
import com.example.demolition.models.SubjectBook

class MathFrag : Fragment() {

    private var _binding: FragmentMathBinding? = null
    private val binding get() = _binding!!

    private lateinit var subjectBook: SubjectBook
    private lateinit var adapter: ChapterAdapter

    private val subjectName = "math"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMathBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load JSON for math using the unified loader
        subjectBook = JsonLoader.loadSubjectChapters(requireContext(), subjectName)

        setupRecycler()
    }

    private fun setupRecycler() {
        val chapterTitles = subjectBook.chapters.map { it.chapter_title }

        adapter = ChapterAdapter(chapterTitles) { chapterTitle ->
            val intent = Intent(requireContext(), ChapterViewer::class.java).apply {
                putExtra("chapter_title", chapterTitle)
                putExtra("subject", subjectName) // important: tell ChapterViewer which subject
            }
            startActivity(intent)
        }

        binding.rvChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChapters.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
