package com.example.demolition

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserData : AppCompatActivity() {

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var locationEditText: TextInputEditText
    private lateinit var genderSpinner: Spinner
    private lateinit var classSpinner: Spinner
    private lateinit var sectionSpinner: Spinner
    private lateinit var continueButton: Button
    private lateinit var avatarRecyclerView: RecyclerView

    private lateinit var firebaseAuth: FirebaseAuth
    private val databaseRef = FirebaseDatabase.getInstance().getReference("Users")
    
    private var selectedAvatarId = "avatar1"  // Default selection
    private val avatarList = listOf("avatar1", "avatar2", "avatar3", "avatar4", "avatar5", "avatar6")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)

        firebaseAuth = FirebaseAuth.getInstance()

        firstNameEditText = findViewById(R.id.editTextF_Name)
        lastNameEditText = findViewById(R.id.editTextL_Name)
        phoneEditText = findViewById(R.id.editTextPhone)
        ageEditText = findViewById(R.id.editTextCompany)
        locationEditText = findViewById(R.id.editTextLocation)
        genderSpinner = findViewById(R.id.spinnerGender)
        classSpinner = findViewById(R.id.spinnerClass)
        sectionSpinner = findViewById(R.id.spinnerSection)
        continueButton = findViewById(R.id.btnContinue)
        avatarRecyclerView = findViewById(R.id.rvAvatars)

        setupAvatarSelection()

        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other")
        genderSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)

        val classOptions = (1..12).map { it.toString() }
        classSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, classOptions)

        val sectionOptions = listOf("A", "B", "C", "D")
        sectionSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sectionOptions)

        continueButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun setupAvatarSelection() {
        avatarRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = AvatarAdapter(avatarList, selectedAvatarId) { avatarId ->
            selectedAvatarId = avatarId
        }
        avatarRecyclerView.adapter = adapter
    }

    private fun saveUserData() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val age = ageEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val studentClass = classSpinner.selectedItem.toString()
        val section = sectionSpinner.selectedItem.toString()

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() ||
            age.isEmpty() || location.isEmpty() || gender == "Select Gender"
        ) {
            showErrorToast("Please fill in all fields")
            return
        }

        val userId = firebaseAuth.currentUser?.uid ?: return
        val fullName = "$firstName $lastName"
        val user = User(firstName, lastName, phone, age, location, gender, studentClass, section, selectedAvatarId, fullName)

        databaseRef.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showCorrectToast("Profile created successfully!")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showErrorToast("Failed to save data: ${task.exception?.message}")
            }
        }
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
}

// Avatar Adapter for RecyclerView
class AvatarAdapter(
    private val avatars: List<String>,
    private var selectedAvatar: String,
    private val onAvatarSelected: (String) -> Unit
) : RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder>() {

    inner class AvatarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatarImage: ImageView = view.findViewById(R.id.ivAvatar)
        val selectionIndicator: View = view.findViewById(R.id.vSelection)
        val checkmark: ImageView = view.findViewById(R.id.ivCheckmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_avatar_selector, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val avatarId = avatars[position]
        val context = holder.itemView.context
        
        // Load avatar image
        val resourceId = context.resources.getIdentifier(avatarId, "drawable", context.packageName)
        holder.avatarImage.setImageResource(resourceId)

        // Show/hide selection indicator and checkmark
        val isSelected = avatarId == selectedAvatar
        holder.selectionIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
        holder.checkmark.visibility = if (isSelected) View.VISIBLE else View.GONE

        // Handle click
        holder.itemView.setOnClickListener {
            val oldSelection = selectedAvatar
            selectedAvatar = avatarId
            onAvatarSelected(avatarId)
            
            // Refresh both items
            notifyItemChanged(avatars.indexOf(oldSelection))
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = avatars.size
}
