package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentSstfragBinding
import com.example.demolition.models.SubjectBook
import com.google.gson.Gson

class sstfrag : Fragment() {

    private var _binding: FragmentSstfragBinding? = null
    private val binding get() = _binding!!

    private lateinit var chapterAdapter: ChapterAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSstfragBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadChapters()
    }

    private fun loadChapters() {
        // Load SST chapters JSON
        val json = requireContext().assets.open("sst_chapters.json")
            .bufferedReader()
            .use { it.readText() }

        val book = Gson().fromJson(json, SubjectBook::class.java)
        val chapters = book.chapters.map { it.chapter_title }

        chapterAdapter = ChapterAdapter(chapters) { chapterTitle ->
            val intent = Intent(requireContext(), ChapterViewer::class.java)
            intent.putExtra("chapter_title", chapterTitle)
            intent.putExtra("subject", "sst")  // IMPORTANT
            startActivity(intent)
        }

        binding.rvSstChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSstChapters.adapter = chapterAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
