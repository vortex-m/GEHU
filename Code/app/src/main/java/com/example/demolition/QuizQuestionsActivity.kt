package com.example.demolition

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.demolition.databinding.ActivityQuizQuestionsBinding
import com.example.demolition.models.Question
import com.example.demolition.models.QuizData
import com.example.demolition.models.QuizResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class QuizQuestionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizQuestionsBinding
    private lateinit var questionList: List<Question>

    private var currentIndex = 0
    private var score = 0
    private var selectedOption: String = ""
    private var subject: String = "math"
    private var chapterTitle: String = ""
    private var chapterNumber: String = ""

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val realtimeDB = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizQuestionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chapterTitle = intent.getStringExtra("chapter_title") ?: ""
        subject = intent.getStringExtra("subject") ?: "math"

        loadQuestions(chapterTitle)
        loadQuestion()

        setOptionClickListeners()
        setNextClickListener()
    }

    private fun loadQuestions(chapterTitle: String) {
        val fileName = "${subject}_quiz.json"

        val json = assets.open(fileName).bufferedReader().use { it.readText() }
        val quizData = Gson().fromJson(json, QuizData::class.java)

        val chapter = quizData.quiz.firstOrNull {
            it.chapter_title == chapterTitle
        }
        
        val allQuestions = chapter?.questions ?: emptyList()
        chapterNumber = chapter?.chapter_number?.toString() ?: ""

        questionList = allQuestions.shuffled().take(5)
    }

    private fun loadQuestion() {
        if (currentIndex >= questionList.size) {
            showFinalScore()
            return
        }

        val q = questionList[currentIndex]
        resetOptionStyles()

        binding.tvQuizQuestion.text = "Q${currentIndex + 1}. ${q.q}"
        binding.option1.text = q.options[0]
        binding.option2.text = q.options[1]
        binding.option3.text = q.options[2]
        binding.option4.text = q.options[3]
    }

    private fun setOptionClickListeners() {
        binding.option1.setOnClickListener { selectOption(binding.option1.text.toString()) }
        binding.option2.setOnClickListener { selectOption(binding.option2.text.toString()) }
        binding.option3.setOnClickListener { selectOption(binding.option3.text.toString()) }
        binding.option4.setOnClickListener { selectOption(binding.option4.text.toString()) }
    }

    private fun selectOption(option: String) {
        selectedOption = option
        resetOptionStyles()

        when (option) {
            binding.option1.text -> binding.option1.setBackgroundColor(Color.parseColor("#D0E6FF"))
            binding.option2.text -> binding.option2.setBackgroundColor(Color.parseColor("#D0E6FF"))
            binding.option3.text -> binding.option3.setBackgroundColor(Color.parseColor("#D0E6FF"))
            binding.option4.text -> binding.option4.setBackgroundColor(Color.parseColor("#D0E6FF"))
        }
    }

    private fun resetOptionStyles() {
        val white = Color.WHITE
        binding.option1.setBackgroundColor(white)
        binding.option2.setBackgroundColor(white)
        binding.option3.setBackgroundColor(white)
        binding.option4.setBackgroundColor(white)
    }

    private fun setNextClickListener() {
        binding.btnNext.setOnClickListener {
            if (selectedOption.isEmpty()) {
                showErrorToast("Please select an option!")
                return@setOnClickListener
            }

            val correctAnswer = questionList[currentIndex].answer
            if (selectedOption == correctAnswer) score++

            selectedOption = ""
            currentIndex++
            loadQuestion()
        }
    }

    private fun showFinalScore() {
        val percentage = (score.toDouble() / questionList.size) * 100

        // Save quiz result to Firestore and local storage
        saveQuizResult(score, questionList.size, percentage)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quiz Completed")
            .setMessage("Your Score: $score / ${questionList.size}\nPercentage: ${String.format("%.1f", percentage)}%")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun saveQuizResult(score: Int, totalQuestions: Int, percentage: Double) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            showErrorToast("User not logged in")
            return
        }

        // Get current date and time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentDate = Date()
        val dateString = dateFormat.format(currentDate)
        val timeString = timeFormat.format(currentDate)

        // Get student information from Realtime Database
        realtimeDB.getReference("Users/$uid").get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Log.e("QUIZ", "User data not found")
                    return@addOnSuccessListener
                }

                val studentName = snapshot.child("name").value?.toString() ?: "Unknown"
                val studentClass = snapshot.child("studentClass").value?.toString() ?: ""
                val section = snapshot.child("section").value?.toString() ?: ""

                // Create quiz result object
                val quizResult = QuizResult(
                    userId = uid,
                    studentName = studentName,
                    studentClass = studentClass,
                    section = section,
                    subject = subject,
                    chapterTitle = chapterTitle,
                    chapterNumber = chapterNumber,
                    score = score,
                    totalQuestions = totalQuestions,
                    percentage = percentage,
                    dateString = dateString,
                    timeString = timeString
                )

                // Save to Firestore
                firestore.collection("quiz_results")
                    .document(uid)
                    .collection("results")
                    .add(quizResult.toMap())
                    .addOnSuccessListener { docRef ->
                        Log.d("QUIZ", "Quiz result saved to Firestore: ${docRef.id}")
                        showCorrectToast("Quiz result saved to cloud!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("QUIZ", "Failed to save quiz result to Firestore", e)
                        showErrorToast("Failed to sync with cloud: ${e.message}")
                    }

                // Also save to local storage using ReportManager
                val report = StudentReport(
                    name = studentName,
                    studentClass = "$studentClass-$section",
                    subject = subject,
                    chapter = chapterTitle,
                    date = dateString,
                    time = timeString,
                    score = "$score/${totalQuestions}",
                    synced = true  // Already synced to Firestore
                )
                
                ReportManager.saveReport(this, report)
                Log.d("QUIZ", "Quiz result saved locally")
            }
            .addOnFailureListener { e ->
                Log.e("QUIZ", "Failed to load student data", e)
                showErrorToast("Failed to get student information")
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
