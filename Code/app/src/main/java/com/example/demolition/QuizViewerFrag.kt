package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentQuizViewerBinding
import com.example.demolition.models.QuizData
import com.example.demolition.quiz.QuizChapterAdapter
import com.google.gson.Gson

class QuizViewerFrag : Fragment() {

    private var _binding: FragmentQuizViewerBinding? = null
    private val binding get() = _binding!!

    private var subject: String = "math"   // default

    companion object {
        fun newInstance(subject: String): QuizViewerFrag {
            val frag = QuizViewerFrag()
            val bundle = Bundle()
            bundle.putString("subject", subject)
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subject = arguments?.getString("subject") ?: "math"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentQuizViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadQuizChapters()
    }

    private fun loadQuizChapters() {
        val fileName = "${subject}_quiz.json"

        val json = requireContext().assets.open(fileName)
            .bufferedReader()
            .use { it.readText() }

        val quizData = Gson().fromJson(json, QuizData::class.java)
        val chapterTitles = quizData.quiz.map { it.chapter_title }

        val adapter = QuizChapterAdapter(chapterTitles) { selectedChapter ->
            val intent = Intent(requireContext(), QuizQuestionsActivity::class.java)
            intent.putExtra("chapter_title", selectedChapter)
            intent.putExtra("subject", subject)
            startActivity(intent)
        }

        binding.rvQuizChapters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuizChapters.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
