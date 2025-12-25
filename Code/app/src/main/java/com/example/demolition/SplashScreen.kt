package com.example.demolition

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        auth = FirebaseAuth.getInstance()

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 1500) // 1.5 sec splash delay
    }

    private fun checkUserStatus() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // ✔ User already logged in → Go to main screen
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // ❌ No user → Go to login screen
            startActivity(Intent(this, Login::class.java))
        }

        finish() // Prevent going back to splash
    }
}
