package com.example.demolition.models

data class TimetableEntry(
    val period: String,
    val time: String,
    val subject: String,
    val teacher: String
)

data class WeeklyTimetable(
    val monday: List<TimetableEntry>,
    val tuesday: List<TimetableEntry>,
    val wednesday: List<TimetableEntry>,
    val thursday: List<TimetableEntry>,
    val friday: List<TimetableEntry>
) {
    fun getTodaySchedule(): Pair<String, List<TimetableEntry>> {
        val calendar = java.util.Calendar.getInstance()
        val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
        
        return when (dayOfWeek) {
            java.util.Calendar.MONDAY -> "Monday" to monday
            java.util.Calendar.TUESDAY -> "Tuesday" to tuesday
            java.util.Calendar.WEDNESDAY -> "Wednesday" to wednesday
            java.util.Calendar.THURSDAY -> "Thursday" to thursday
            java.util.Calendar.FRIDAY -> "Friday" to friday
            else -> "Weekend" to emptyList() // Saturday/Sunday
        }
    }
}
