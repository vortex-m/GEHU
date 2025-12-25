package com.example.demolition


import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.demolition.utils.ToastUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var signupButton: Button
    private lateinit var loginText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        signupButton = findViewById(R.id.btnSignUp)
        loginText = findViewById(R.id.loginText)
        
        // Apply entrance animations
        applyEntranceAnimations()

        signupButton.setOnClickListener {
            // Apply button tap animation
            it.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_tap))
            
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                ToastUtils.showErrorToast(this, "All fields are required")
                return@setOnClickListener
            }
            
            // Email format validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ToastUtils.showErrorToast(this, "Please enter a valid email address")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                ToastUtils.showErrorToast(this, "Passwords do not match")
                return@setOnClickListener
            }
            
            // Password strength validation
            if (password.length < 8) {
                ToastUtils.showErrorToast(this, "Password must be at least 8 characters")
                return@setOnClickListener
            }
            
            if (!password.any { it.isDigit() } || !password.any { it.isLetter() }) {
                ToastUtils.showErrorToast(this, "Password must contain letters and numbers")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ToastUtils.showCorrectToast(this, "Signup Successful")
                        startActivity(Intent(this, UserData::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    } else {
                        ToastUtils.showErrorToast(this, "Signup Failed: ${task.exception?.message}")
                    }
                }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
    
    private fun applyEntranceAnimations() {
        // Animate the signup card
        val cardView = findViewById<CardView>(R.id.cardView)
        cardView?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.card_pop_in))
        
        // Animate the header
        val header = findViewById<TextView>(R.id.Header)
        header?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_slide_up))
    }
}

