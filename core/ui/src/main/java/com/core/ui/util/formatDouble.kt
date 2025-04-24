package com.core.ui.util

import java.text.DecimalFormat
import java.math.BigDecimal
import java.math.RoundingMode

fun formatDouble(input: Double): String {
    // If it's exactly zero, just return "0"
    if (input == 0.0) return "0"
    
    // For small values, ensure we show at least 6 decimal places
    if (input < 0.0001) {
        val bd = BigDecimal(input).setScale(6, RoundingMode.HALF_UP)
        // If the value is still zero after rounding to 6 places, show a minimum value
        if (bd.compareTo(BigDecimal.ZERO) == 0) {
            return "0.000001"
        }
        // Custom format for very small numbers to avoid scientific notation
        val df = DecimalFormat("0.######")
        df.minimumFractionDigits = 6
        return df.format(input)
    }
    
    // For regular numbers with larger values
    val df = DecimalFormat("###,##0.######")
    // Adjust decimal places based on number size
    if (input >= 1000) {
        df.maximumFractionDigits = 2 
    } else if (input >= 1) {
        df.maximumFractionDigits = 4
    } else {
        df.maximumFractionDigits = 6
    }
    
    return df.format(input)
}