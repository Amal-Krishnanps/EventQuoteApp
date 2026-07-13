package com.eventquote.app.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Formats numbers in Indian currency format: ₹1,25,000.00
 * Indian numbering system: groups of 2 after the first 3 digits.
 */
object CurrencyFormatter {

    private val indianLocale = Locale("en", "IN")

    /**
     * Full format: ₹1,25,000.00
     */
    fun format(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(indianLocale)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter.format(amount)
    }

    /**
     * Compact format for dashboard: ₹1.25L or ₹12.5K
     */
    fun formatCompact(amount: Double): String {
        return when {
            amount >= 10_00_000 -> "₹${String.format("%.1f", amount / 10_00_000)}Cr"
            amount >= 1_00_000 -> "₹${String.format("%.1f", amount / 1_00_000)}L"
            amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
            else -> format(amount)
        }
    }

    /**
     * Format without symbol: 1,25,000
     */
    fun formatWithoutSymbol(amount: Double): String {
        return format(amount).removePrefix("₹").trim()
    }

    /**
     * Convert Double to Indian number string representation
     * E.g.: 125000.0 → "1,25,000"
     */
    fun toIndianString(amount: Double): String {
        val longPart = amount.toLong()
        val fracPart = ((amount - longPart) * 100).toInt()
        val intFormatted = formatIndianInt(longPart)
        return if (fracPart > 0) "$intFormatted.${fracPart.toString().padStart(2, '0')}"
        else intFormatted
    }

    private fun formatIndianInt(number: Long): String {
        val str = number.toString()
        if (str.length <= 3) return str
        // First group of 3, then groups of 2 from right
        val firstGroup = str.length - 3
        val result = StringBuilder()
        result.append(str.substring(0, firstGroup % 2).ifEmpty { str.substring(0, 1) })
        // Actually use the standard Indian format
        return NumberFormat.getNumberInstance(indianLocale).format(number)
    }
}
