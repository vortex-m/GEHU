package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demolition.databinding.ActivityEnglishBinding

class English : AppCompatActivity() {

    private lateinit var binding: ActivityEnglishBinding
    private val subjectName = "english"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEnglishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use binding.mainRoot or binding.root instead of a missing R.id.main
        // Replace `mainRoot` with the actual id name from activity_english.xml if different
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // load fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, EnglishFrag())
            .commit()

        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_chapters -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, EnglishFrag())
                        .commit()
                    true
                }

                R.id.nav_quiz -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, QuizViewerFrag.newInstance(subjectName))
                        .commit()
                    true
                }

                R.id.nav_ai -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, AiChatterFrag())
                        .addToBackStack(null)
                        .commit()
                    true
                }

                else -> false
            }
        }
    }
}
