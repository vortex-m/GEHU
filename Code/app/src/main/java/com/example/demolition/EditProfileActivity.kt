package com.example.demolition

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.demolition.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    
    private var selectedAvatarId = "avatar1"  // Default selection
    private val avatarList = listOf("avatar1", "avatar2", "avatar3", "avatar4", "avatar5", "avatar6")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {

        // Profile image click - show avatar selection dialog
        binding.ivProfile.setOnClickListener {
            showAvatarSelectionDialog()
        }

        // ⭐ Save to Firestore
        binding.btnSave.setOnClickListener {
            if (userId == null) {
                showErrorToast("User not logged in")
                return@setOnClickListener
            }

            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.etPhone.text.toString()

            val data = mapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
                "avatarId" to selectedAvatarId
            )

            db.collection("users")
                .document(userId)
                .update(data)
                .addOnSuccessListener {
                    showCorrectToast("Profile Updated Successfully")
                }
                .addOnFailureListener {
                    showErrorToast("Failed to update: ${it.message}")
                }
        }
    }

    private fun showAvatarSelectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_avatar_selector, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rvDialogAvatars)
        
        // Setup RecyclerView with GridLayoutManager (3 columns)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        
        // Create adapter with callback to handle selection
        val adapter = AvatarAdapter(avatarList, selectedAvatarId) { avatarId ->
            selectedAvatarId = avatarId
            // Update the profile image immediately
            val resourceId = resources.getIdentifier(avatarId, "drawable", packageName)
            binding.ivProfile.setImageResource(resourceId)
        }
        recyclerView.adapter = adapter
        
        // Build and show the dialog
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Done") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showCorrectToast(message: String) {
        val layout = layoutInflater.inflate(R.layout.correct_toast, findViewById(R.id.toast_container))
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    private fun showErrorToast(message: String) {
        val layout = layoutInflater.inflate(R.layout.error_toast, findViewById(R.id.toast_container))
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    private fun showInfoToast(message: String) {
        val layout = layoutInflater.inflate(R.layout.toast_info, findViewById(R.id.toast_container))
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(applicationContext)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }
}
