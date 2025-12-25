package com.example.demolition

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.demolition.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val realtimeDB = FirebaseDatabase.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val timetableList = ArrayList<TimetableItem>()
    private lateinit var timetableAdapter: TimetableAdapter

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): android.view.View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupCardClicks()
        setupSyncButton()
        loadStudentClass()
        loadChapterCounts() // Load chapter counts for each subject
        
        // Apply entrance animations
        applyEntranceAnimations()

        return binding.root
    }
    
    // ----------------- LOAD CHAPTER COUNTS -------------------------
    
    private fun loadChapterCounts() {
        try {
            // Load Math chapters
            val mathBook = JsonLoader.loadSubjectChapters(requireContext(), "math")
            binding.tvMathChapters.text = "${mathBook.chapters.size} Chapters"
            
            // Load Science chapters
            val scienceBook = JsonLoader.loadSubjectChapters(requireContext(), "science")
            binding.tvScienceChapters.text = "${scienceBook.chapters.size} Chapters"
            
            // Load English chapters
            val englishBook = JsonLoader.loadSubjectChapters(requireContext(), "english")
            binding.tvEnglishChapters.text = "${englishBook.chapters.size} Chapters"
            
            // Load SST chapters
            val sstBook = JsonLoader.loadSubjectChapters(requireContext(), "sst")
            binding.tvSstChapters.text = "${sstBook.chapters.size} Chapters"
        } catch (e: Exception) {
            Log.e("HOME", "Error loading chapter counts", e)
        }
    }
    
    // ----------------- ENTRANCE ANIMATIONS -------------------
    
    private fun applyEntranceAnimations() {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val cards = listOf(binding.cardMath, binding.cardScience, binding.cardGeo, binding.cardHistory)
        
        cards.forEachIndexed { index, card ->
            card.alpha = 0f
            handler.postDelayed({
                card.alpha = 1f
                card.startAnimation(android.view.animation.AnimationUtils.loadAnimation(requireContext(), R.anim.card_pop_in))
            }, (index * 100L))
        }
    }

    // ----------------- TIMETABLE RECYCLER -------------------

    private fun setupRecyclerView() {
        timetableAdapter = TimetableAdapter(timetableList)
        binding.rvTimetable.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTimetable.adapter = timetableAdapter
    }

    // ----------------- COURSE CARDS -------------------------

    private fun setupCardClicks() {
        binding.cardMath.setOnClickListener {
            startActivity(Intent(requireContext(), Math::class.java))
        }

        binding.cardScience.setOnClickListener {
            startActivity(Intent(requireContext(), Science::class.java))
        }

        binding.cardGeo.setOnClickListener {
            startActivity(Intent(requireContext(), English::class.java))
        }

        binding.cardHistory.setOnClickListener {
            startActivity(Intent(requireContext(), sst::class.java))
        }
    }

    // ----------------- LOAD TIMETABLE FROM LOCAL JSON -------------------------

    private fun loadStudentClass() {
        // Load timetable from local JSON file
        loadLocalTimetable()
    }

    private fun loadLocalTimetable() {
        // Guard against view being destroyed
        if (_binding == null) return
        
        try {
            // Check if it's weekend
            if (TimetableLoader.isWeekend()) {
                showWeekendMessage()
                return
            }
            
            // Load today's schedule
            val todaySchedule = TimetableLoader.getTodaySchedule(requireContext(), "9th")
            
            if (todaySchedule.isNullOrEmpty()) {
                showError("No classes scheduled for today")
                return
            }
            
            // Filter only lectures (exclude breaks)
            val lectures = todaySchedule.filter { it.type == "lecture" }
            
            if (lectures.isEmpty()) {
                showError("No classes today.")
                return
            }
            
            // Convert to TimetableItem
            timetableList.clear()
            lectures.forEach { period ->
                timetableList.add(TimetableItem(period.subject, period.time))
            }
            
            // Update today's class count
            binding.tvClassCount.text = lectures.size.toString()
            
            // Load and update streak
            updateStreak()
            
            // Update UI
            timetableAdapter.notifyDataSetChanged()
            
        } catch (e: Exception) {
            Log.e("HOME", "Error loading timetable", e)
            showError("Failed to load timetable")
        }
    }
    
    private fun showWeekendMessage() {
        timetableList.clear()
        timetableList.add(TimetableItem("🎉 It's the Weekend!", "No classes today - Enjoy!"))
        binding.tvClassCount.text = "0"
        updateStreak()
        timetableAdapter.notifyDataSetChanged()
    }
    
    private fun updateStreak() {
        // Load streak from SharedPreferences (simulated streak system)
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val lastOpenDate = prefs.getString("last_open_date", "")
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        var streak = prefs.getInt("streak", 1)
        
        if (lastOpenDate != currentDate) {
            // New day, increment streak
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday.time)
            
            if (lastOpenDate == yesterdayStr) {
                streak++
            } else if (lastOpenDate.isNullOrEmpty()) {
                streak = 1
            } else {
                // Streak broken
                streak = 1
            }
            
            prefs.edit()
                .putString("last_open_date", currentDate)
                .putInt("streak", streak)
                .apply()
        }
        
        binding.tvStreakCount.text = streak.toString()
    }


    // ----------------- FILTER TODAY CLASSES -------------------------

    private fun showError(msg: String) {
        timetableList.clear()
        timetableList.add(TimetableItem(msg, ""))
        timetableAdapter.notifyDataSetChanged()
    }

    // ----------------- CLOUD SYNC BUTTON -------------------------

    private fun setupSyncButton() {
        binding.SyncWithCloud.setOnClickListener {
            syncReportsToCloud()
        }
    }

    // ----------------- FINAL FIRESTORE SYNC -------------------------

    private fun syncReportsToCloud() {
        val context = requireContext()
        val reports = ReportManager.getReports(context)
        val unsynced = reports.filter { !it.synced }

        if (unsynced.isEmpty()) {
            showCorrectToast("✓ Everything is already synced!")
            return
        }

        // Show syncing feedback
        binding.SyncWithCloud.isEnabled = false

        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        
        if (uid == null) {
            showErrorToast("Please login to sync")
            resetSyncButton()
            return
        }

        var uploaded = 0
        var failed = 0
        val total = unsynced.size

        for (report in unsynced) {

            val data = hashMapOf(
                "name" to report.name,
                "class" to report.studentClass,
                "subject" to report.subject,
                "chapter" to report.chapter,
                "date" to report.date,
                "time" to report.time,
                "score" to report.score
            )

            db.collection("quiz_reports")
                .document(uid)
                .collection("reports")
                .add(data)
                .addOnSuccessListener {
                    report.synced = true
                    uploaded++

                    ReportManager.saveReports(context, reports)

                    if (uploaded + failed == total) {
                        if (failed == 0) {
                            showCorrectToast("✓ All $total reports synced successfully!")
                        } else {
                            showInfoToast("Synced $uploaded/$total reports")
                        }
                        resetSyncButton()
                    }
                }
                .addOnFailureListener { e ->
                    failed++
                    Log.e("FIRESTORE_SYNC", "Failed to sync report: ${e.message}")
                    
                    if (uploaded + failed == total) {
                        if (uploaded > 0) {
                            showInfoToast("Synced $uploaded/$total reports")
                        } else {
                            showErrorToast("✗ Sync failed: ${e.message}")
                        }
                        resetSyncButton()
                    }
                }
        }
    }

    private fun resetSyncButton() {
        binding.SyncWithCloud.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showCorrectToast(message: String) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.correct_toast, null)
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    private fun showErrorToast(message: String) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.error_toast, null)
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    private fun showInfoToast(message: String) {
        val layout = LayoutInflater.from(requireContext()).inflate(R.layout.toast_info, null)
        layout.findViewById<TextView>(R.id.toast_text).text = message
        val toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }
}
