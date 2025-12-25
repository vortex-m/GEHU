package com.example.demolition.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * Tracks the user's daily study streak
 */
object StreakTracker {
    
    private const val PREFS_NAME = "streak_prefs"
    private const val KEY_LAST_VISIT = "last_visit_date"
    private const val KEY_CURRENT_STREAK = "current_streak"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Updates the streak based on current date
     * Call this when the app opens or user interacts
     */
    fun updateStreak(context: Context): Int {
        val prefs = getPrefs(context)
        val today = getTodayDate()
        val lastVisit = prefs.getString(KEY_LAST_VISIT, "") ?: ""
        var currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0)
        
        when {
            lastVisit.isEmpty() -> {
                // First time user
                currentStreak = 1
            }
            lastVisit == today -> {
                // Already visited today, keep streak
                return currentStreak
            }
            isYesterday(lastVisit) -> {
                // Consecutive day, increment streak
                currentStreak++
            }
            else -> {
                // Streak broken, reset to 1
                currentStreak = 1
            }
        }
        
        // Save updated values
        prefs.edit().apply {
            putString(KEY_LAST_VISIT, today)
            putInt(KEY_CURRENT_STREAK, currentStreak)
            apply()
        }
        
        return currentStreak
    }
    
    /**
     * Get current streak without updating
     */
    fun getCurrentStreak(context: Context): Int {
        return getPrefs(context).getInt(KEY_CURRENT_STREAK, 0)
    }
    
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
    
    private fun isYesterday(dateString: String): Boolean {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = sdf.parse(dateString) ?: return false
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)
            
            return dateString == yesterday
        } catch (e: Exception) {
            return false
        }
    }
}
