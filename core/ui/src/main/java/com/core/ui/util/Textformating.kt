package com.core.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun formatSmart(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString() // No decimal part, show as integer
    } else {
        String.format(Locale.US, "%.2f", value) // Show with two decimals
    }
}

fun abbreviateNumber(value: Double): String {
    val suffixes = arrayOf("", "K", "M", "B")
    var num = value
    var index = 0

    while (num >= 1000 && index < suffixes.size - 1) {
        num /= 1000
        index++
    }

    // Ensure US number format with commas and dots
    val symbols = DecimalFormatSymbols(Locale.US)
    val decimalFormat = DecimalFormat("#,##0.##", symbols)

    return "${decimalFormat.format(num)}${suffixes[index]}"
}

fun formatAddress(input: String, visibleChars: Int = 4): String {
    if (input.length <= visibleChars * 2) return input
    return input.take(visibleChars) + "..." + input.takeLast(visibleChars)
}