package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.demolition.databinding.ActivityMathBinding
import com.example.demolition.databinding.ActivitySstBinding

class sst : AppCompatActivity() {

    private lateinit var binding: ActivitySstBinding   // for activity with drawer + container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySstBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load SST Fragment into container
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, sstfrag())
            .commit()

        setupBottomNav()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {

                R.id.nav_chapters -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, sstfrag())
                        .commit()
                    true
                }

                R.id.nav_quiz -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, QuizViewerFrag.newInstance("sst"))
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
