package com.core.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

fun formatSmart(value: Double): String {
    // If it's exactly zero, just return "0"
    if (value == 0.0) return "0"
    
    // For integer values, show without decimal part
    if (value % 1.0 == 0.0) {
        return value.toInt().toString()
    }
    
    // For small values, ensure we show enough decimal places
    if (value < 0.0001 && value > 0) {
        val bd = BigDecimal(value).setScale(6, RoundingMode.HALF_UP)
        // If the value is still zero after rounding to 6 places, show a minimum value
        if (bd.compareTo(BigDecimal.ZERO) == 0) {
            return "0.000001"
        }
        // Custom format for very small numbers to avoid scientific notation
        val df = DecimalFormat("0.######")
        df.minimumFractionDigits = 6
        return df.format(value)
    }
    
    // For numbers between 0.0001 and 1, show up to 6 decimal places
    if (value < 1) {
        return String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
    }
    
    // For regular numbers, show 2 decimal places
    return String.format(Locale.US, "%.2f", value)
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