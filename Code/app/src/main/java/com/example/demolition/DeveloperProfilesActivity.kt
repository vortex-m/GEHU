package com.example.demolition

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class DeveloperProfilesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_profiles)
        
        setupToolbar()
        setupDeveloperProfiles()
    }
    
    private fun setupToolbar() {
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun setupDeveloperProfiles() {
        // Developer 1 - You can customize these
        findViewById<CardView>(R.id.cardDev1).setOnClickListener {
            openUrl("https://github.com/yourusername")
        }
        
        // Developer 2
        findViewById<CardView>(R.id.cardDev2).setOnClickListener {
            openUrl("https://linkedin.com/in/yourprofile")
        }
        
        // Email contact
        findViewById<CardView>(R.id.cardEmail).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:developers@demolitionapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for Demolition App")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
