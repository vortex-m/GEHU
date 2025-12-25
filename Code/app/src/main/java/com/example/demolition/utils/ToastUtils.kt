package com.example.demolition.utils

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.demolition.R

/**
 * Centralized toast utility to avoid code duplication.
 * Replaces duplicated toast functions across 6+ activities/fragments.
 */
object ToastUtils {

    /**
     * Shows a success/correct toast message
     */
    fun showCorrectToast(context: Context, message: String) {
        val layout = LayoutInflater.from(context).inflate(
            R.layout.correct_toast,
            null
        )
        layout.findViewById<TextView>(R.id.toast_text).text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    /**
     * Shows an error toast message
     */
    fun showErrorToast(context: Context, message: String) {
        val layout = LayoutInflater.from(context).inflate(
            R.layout.error_toast,
            null
        )
        layout.findViewById<TextView>(R.id.toast_text).text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }

    /**
     * Shows an informational toast message
     */
    fun showInfoToast(context: Context, message: String) {
        val layout = LayoutInflater.from(context).inflate(
            R.layout.toast_info,
            null
        )
        layout.findViewById<TextView>(R.id.toast_text).text = message

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        @Suppress("DEPRECATION")
        toast.view = layout
        toast.show()
    }
}
