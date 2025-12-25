package com.example.demolition

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ReportManager {

    private const val FILE_NAME = "Student_report.json"

    fun saveReport(context: Context, report: StudentReport) {
        val file = File(context.filesDir, FILE_NAME)

        val gson = Gson()
        val reportList: MutableList<StudentReport>

        if (file.exists()) {
            val oldJson = file.readText()
            val type = object : TypeToken<MutableList<StudentReport>>() {}.type
            reportList = gson.fromJson(oldJson, type)
        } else {
            reportList = mutableListOf()
        }

        reportList.add(report)

        val newJson = gson.toJson(reportList)
        file.writeText(newJson)
    }

    fun getReports(context: Context): MutableList<StudentReport> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return mutableListOf()

        val json = file.readText()
        val type = object : TypeToken<MutableList<StudentReport>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveReports(context: Context, reports: MutableList<StudentReport>) {
        val file = File(context.filesDir, FILE_NAME)
        val json = Gson().toJson(reports)
        file.writeText(json)
    }
}
