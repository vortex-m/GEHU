package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.demolition.databinding.ActivityMathBinding

class Math : AppCompatActivity() {

    private lateinit var binding: ActivityMathBinding   // for activity with drawer + container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMathBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load Math Fragment into container
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MathFrag())
            .commit()

        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_chapters -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, MathFrag())
                        .commit()
                    true
                }

                R.id.nav_quiz -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, QuizViewerFrag())
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
