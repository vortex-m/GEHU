package com.example.demolition
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.demolition.databinding.ActivityScienceBinding
class Science : AppCompatActivity() {
    private lateinit var binding: ActivityScienceBinding
    private val subjectName = "science"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScienceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // --- Use binding.mainRoot (id = main_root) or binding.root instead of R.id.main ---
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Load Science fragment into container
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ScienceFrag())
            .commit()

        setupBottomNav()
    }
    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chapters -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container, ScienceFrag())
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
