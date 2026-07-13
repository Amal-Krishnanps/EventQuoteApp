package com.eventquote.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Date and time formatting utilities.
 */
object DateUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    /** Format millis to "12 Jun 2026" */
    fun formatDate(millis: Long): String =
        runCatching { dateFormat.format(Date(millis)) }.getOrDefault("--")

    /** Format millis to "12 Jun 2026, 11:30 AM" */
    fun formatDateTime(millis: Long): String =
        runCatching { dateTimeFormat.format(Date(millis)) }.getOrDefault("--")

    /** Format millis to "June 2026" */
    fun formatMonthYear(millis: Long): String =
        runCatching { monthYearFormat.format(Date(millis)) }.getOrDefault("--")

    /** Current date as formatted string */
    fun today(): String = dateFormat.format(Date())

    /** Millis to date string for PDF */
    fun toPdfDate(millis: Long): String = formatDate(millis)

    /** Calculate if a date is in the past */
    fun isPast(millis: Long): Boolean = millis < System.currentTimeMillis()

    /** Days until a future date (negative if past) */
    fun daysUntil(millis: Long): Long {
        val diff = millis - System.currentTimeMillis()
        return diff / (24 * 60 * 60 * 1000L)
    }
}
