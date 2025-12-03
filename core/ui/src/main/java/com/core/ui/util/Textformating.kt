package com.core.ui.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.floor
import kotlin.math.log10

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

    // scale down by thousands
    while (abs(num) >= 1000 && index < suffixes.size - 1) {
        num /= 1000
        index++
    }

    // US-style formatter, max 2 decimal places
    val symbols = DecimalFormatSymbols(Locale.US)
    val df = DecimalFormat("#,##0.##", symbols)
    val formatted = df.format(num)

    // if it rounded to "0" but was nonzero (e.g. 3.8E-4), fall back to full precision
    if (formatted == "0" && num != 0.0) {
        val plain = BigDecimal.valueOf(num)
            .stripTrailingZeros()
            .toPlainString()
        return "$plain${suffixes[index]}"
    }

    return "$formatted${suffixes[index]}"
}


fun Double.formatWithSuffix(maxDecimals: Int = 4, maxSignificantDigits: Int = 4): String {
    if (this == 0.0) return "0"
    
    val absValue = kotlin.math.abs(this)

    val (divisor, suffix) = when {
        absValue >= 1_000_000_000_000 -> 1_000_000_000_000.0 to "T"
        absValue >= 1_000_000_000     -> 1_000_000_000.0     to "B"
        absValue >= 1_000_000         -> 1_000_000.0         to "M"
        absValue >= 1_000             -> 1_000.0             to "K"
        else                          -> 1.0                 to ""
    }

    val decimals = if (suffix.isNotEmpty()) 2 else maxDecimals
    val scaled = this / divisor
    val bd = BigDecimal.valueOf(scaled)

    val scaledAndRounded = if (suffix.isNotEmpty()) {
        // With suffix (e.g., "K", "M") – we can safely round to the desired decimals
        bd.setScale(decimals, RoundingMode.HALF_UP).stripTrailingZeros()
    } else {
        // Without suffix (values < 1K): be careful not to round tiny numbers down to zero
        val candidate = bd.setScale(decimals, RoundingMode.HALF_UP)
        if (candidate.compareTo(BigDecimal.ZERO) == 0 && bd.compareTo(BigDecimal.ZERO) != 0) {
            // Rounding wiped out all significant digits – use scientific notation
            return formatWithScientificNotation(this, maxSignificantDigits)
        } else {
            candidate.stripTrailingZeros()
        }
    }

    return scaledAndRounded.toPlainString() + suffix
}

/**
 * Formats a very small number using scientific notation with superscript exponents.
 * For example, 0.000000004245753332 with 4 significant digits becomes "4.246×10⁻⁹"
 */
fun formatWithScientificNotation(value: Double, significantDigits: Int = 4): String {
    if (value == 0.0) return "0"
    
    val absValue = abs(value)
    val sign = if (value < 0) "-" else ""
    
    // Calculate the exponent (power of 10)
    val exponent = floor(log10(absValue)).toInt()
    
    // For numbers >= 0.0001 (exponent >= -4), use regular formatting
    if (exponent >= -4) {
        val bd = BigDecimal.valueOf(absValue)
        val rounded = roundToSignificantDigits(bd, significantDigits)
        return sign + rounded.toPlainString()
    }
    
    // Scale the number to get the mantissa (coefficient)
    val mantissa = absValue / 10.0.pow(exponent.toDouble())
    
    // Round the mantissa to the desired significant digits
    val mantissaBd = BigDecimal.valueOf(mantissa)
    val roundedMantissa = mantissaBd.setScale(significantDigits - 1, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
    
    // Convert exponent to superscript
    val superscriptExponent = exponent.toString().map { char ->
        when (char) {
            '-' -> '⁻'
            '0' -> '⁰'
            '1' -> '¹'
            '2' -> '²'
            '3' -> '³'
            '4' -> '⁴'
            '5' -> '⁵'
            '6' -> '⁶'
            '7' -> '⁷'
            '8' -> '⁸'
            '9' -> '⁹'
            else -> char
        }
    }.joinToString("")
    
    return "${sign}${roundedMantissa}×10${superscriptExponent}"
}

/**
 * Rounds a BigDecimal to the specified number of significant digits.
 */
private fun roundToSignificantDigits(value: BigDecimal, significantDigits: Int): BigDecimal {
    if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
    
    val currentPrecision = value.precision()
    val currentScale = value.scale()
    val newScale = currentScale - (currentPrecision - significantDigits)
    
    return if (newScale >= 0) {
        value.setScale(newScale, RoundingMode.HALF_UP).stripTrailingZeros()
    } else {
        value.setScale(0, RoundingMode.HALF_UP).stripTrailingZeros()
    }
}

// ——— Sample usage ———
fun main() {
    val examples = listOf(
        950.0,                    // no suffix → "950"
        123.456789,               // no suffix → "123.4568"
        1_234.0,                  // K suffix → "1.23K"
        1_234.56789,              // K suffix → "1.23K"
        2_500_000.0,              // M suffix → "2.5M"
        7_890_123_456.0,          // B suffix → "7.89B"
        -15_000.3456,             // negative K suffix → "-15K"
        0.00005,                  // small → "0.00005" (exponent >= -4)
        0.000000004245753332,     // very small → "4.246×10⁻⁹" (scientific notation)
        0.0                       // zero → "0"
    )
    examples.forEach { println("${it} → ${it.formatWithSuffix()}") }

    // you can also override non-suffix decimals:
    println(123.456789.formatWithSuffix(maxDecimals = 2))  // "123.46"
    
    // Direct scientific notation formatting:
    println(formatWithScientificNotation(0.000000004245753332))  // "4.246×10⁻⁹"
}

fun formatAddress(input: String, visibleChars: Int = 4): String {
    if (input.length <= visibleChars * 2) return input
    return input.take(visibleChars) + "..." + input.takeLast(visibleChars)
}