package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentEnglishBinding
import com.example.demolition.models.SubjectBook

class EnglishFrag : Fragment() {

    private var _binding: FragmentEnglishBinding? = null
    private val binding get() = _binding!!

    private lateinit var chapterAdapter: ChapterAdapter
    private val subjectName = "english"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEnglishBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadChapters()
    }

    private fun loadChapters() {
        // Load English chapters via unified JSON loader
        val book: SubjectBook = JsonLoader.loadSubjectChapters(requireContext(), subjectName)
        val chapters = book.chapters.map { it.chapter_title }

        chapterAdapter = ChapterAdapter(chapters) { chapterTitle ->
            val intent = Intent(requireContext(), ChapterViewer::class.java).apply {
                putExtra("chapter_title", chapterTitle)
                putExtra("subject", subjectName) // REQUIRED for loading correct subject
            }
            startActivity(intent)
        }

        binding.rvEnglishChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEnglishChapters.adapter = chapterAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
