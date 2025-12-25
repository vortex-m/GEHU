package com.example.demolition

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.InputStreamReader

/**
 * Data classes for timetable JSON structure
 */
data class TimetableData(
    @SerializedName("class") val className: String,
    val schedule: WeekSchedule
)

data class WeekSchedule(
    val monday: List<TimetablePeriod>,
    val tuesday: List<TimetablePeriod>,
    val wednesday: List<TimetablePeriod>,
    val thursday: List<TimetablePeriod>,
    val friday: List<TimetablePeriod>,
    val saturday: List<TimetablePeriod>,
    val sunday: List<TimetablePeriod>
)

data class TimetablePeriod(
    val period: String,
    val time: String,
    val subject: String,
    val type: String // "lecture" or "break"
)

/**
 * Utility class to load timetable from local JSON file
 */
object TimetableLoader {
    
    /**
     * Load timetable for a specific class
     */
    fun loadTimetable(context: Context, className: String = "9th"): TimetableData? {
        return try {
            val fileName = "timetable_${className}.json"
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val timetable = Gson().fromJson(reader, TimetableData::class.java)
            reader.close()
            timetable
        } catch (e: Exception) {
            android.util.Log.e("TimetableLoader", "Error loading timetable: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get schedule for a specific day
     */
    fun getScheduleForDay(timetable: TimetableData, day: String): List<TimetablePeriod> {
        return when (day.lowercase()) {
            "monday" -> timetable.schedule.monday
            "tuesday" -> timetable.schedule.tuesday
            "wednesday" -> timetable.schedule.wednesday
            "thursday" -> timetable.schedule.thursday
            "friday" -> timetable.schedule.friday
            "saturday" -> timetable.schedule.saturday
            "sunday" -> timetable.schedule.sunday
            else -> emptyList()
        }
    }
    
    /**
     * Get today's schedule
     */
    fun getTodaySchedule(context: Context, className: String = "9th"): List<TimetablePeriod>? {
        val timetable = loadTimetable(context, className) ?: return null
        val today = java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
            .format(java.util.Calendar.getInstance().time)
        return getScheduleForDay(timetable, today)
    }
    
    /**
     * Get only lectures (excluding breaks) for a specific day
     */
    fun getLecturesForDay(timetable: TimetableData, day: String): List<TimetablePeriod> {
        return getScheduleForDay(timetable, day).filter { it.type == "lecture" }
    }
    
    /**
     * Check if it's a weekend
     */
    fun isWeekend(): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        return dayOfWeek == java.util.Calendar.SATURDAY || dayOfWeek == java.util.Calendar.SUNDAY
    }
    
    /**
     * Get current day name
     */
    fun getCurrentDay(): String {
        return java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault())
            .format(java.util.Calendar.getInstance().time)
    }
}
