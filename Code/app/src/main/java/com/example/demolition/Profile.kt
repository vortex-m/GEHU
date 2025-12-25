package com.example.demolition

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.demolition.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDB = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        loadUserDetails()
        loadUserProfilePic()   // from Firestore OR Storage
        setupClicks()

        return binding.root
    }

    /**
     * LOAD USER DETAILS (Realtime DB)
     */
    private fun loadUserDetails() {
        val uid = auth.currentUser?.uid ?: return

        val ref = realtimeDB.getReference("Users/$uid")
        ref.get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                val user = snap.getValue(User::class.java)
                if (user != null) {

                    // Full Name
                    binding.tvName.text = "${user.firstName} ${user.lastName}"

                    // Class & Section
                    binding.tvClass.text = "Class: ${user.studentClass}-${user.section}"

                    // Email
                    binding.tvEmail.text = "Email: ${auth.currentUser?.email}"

                    // Phone
                    binding.tvPhone.text = "Phone: ${user.phone}"

                    // Location
                    binding.tvLocation.text = "Location: ${user.location}"
                }
            }
        }
    }

    /**
     * LOAD USER PROFILE PIC FROM REALTIME DATABASE
     */
    private fun loadUserProfilePic() {
        val uid = auth.currentUser?.uid ?: return

        realtimeDB.getReference("Users/$uid")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)

                    user?.let {
                        if (it.avatarId.isNotEmpty()) {
                            val avatarRes = resources.getIdentifier(
                                it.avatarId,
                                "drawable",
                                requireContext().packageName
                            )

                            if (avatarRes != 0) {
                                binding.ivProfilePic.setImageResource(avatarRes)
                            } else {
                                binding.ivProfilePic.setImageResource(R.drawable.user)
                            }
                        } else {
                            binding.ivProfilePic.setImageResource(R.drawable.user)
                        }
                    }
                }
            }
    }


    /**
     * CLICK HANDLERS
     */
    private fun setupClicks() {

        // EDIT PROFILE → Open EditProfileActivity
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
            activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // LOGOUT with confirmation dialog
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun showLogoutConfirmationDialog() {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("🚪 Logout")
            .setMessage("Are you sure you want to logout?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Logout") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
    
    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
