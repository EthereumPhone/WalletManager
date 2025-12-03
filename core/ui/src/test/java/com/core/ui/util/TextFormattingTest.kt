package com.core.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for text formatting utilities in Textformating.kt
 */
class TextFormattingTest {

    // ===== formatWithSuffix tests =====

    @Test
    fun `formatWithSuffix returns 0 for zero value`() {
        assertEquals("0", 0.0.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix formats regular numbers without suffix`() {
        assertEquals("950", 950.0.formatWithSuffix())
        assertEquals("123.4568", 123.456789.formatWithSuffix())
        assertEquals("0.5", 0.5.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix adds K suffix for thousands`() {
        assertEquals("1.23K", 1_234.0.formatWithSuffix())
        assertEquals("1.23K", 1_234.56789.formatWithSuffix())
        assertEquals("10K", 10_000.0.formatWithSuffix())
        assertEquals("999.99K", 999_990.0.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix adds M suffix for millions`() {
        assertEquals("1M", 1_000_000.0.formatWithSuffix())
        assertEquals("2.5M", 2_500_000.0.formatWithSuffix())
        assertEquals("123.46M", 123_456_789.0.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix adds B suffix for billions`() {
        assertEquals("1B", 1_000_000_000.0.formatWithSuffix())
        assertEquals("7.89B", 7_890_123_456.0.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix adds T suffix for trillions`() {
        assertEquals("1T", 1_000_000_000_000.0.formatWithSuffix())
        assertEquals("5.5T", 5_500_000_000_000.0.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix handles negative numbers correctly`() {
        assertEquals("-15K", (-15_000.3456).formatWithSuffix())
        assertEquals("-1.5M", (-1_500_000.0).formatWithSuffix())
        assertEquals("-123.4568", (-123.456789).formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix handles very small numbers with scientific notation`() {
        // The specific test case: 0.000000004245753332 → "4.246×10⁻⁹"
        assertEquals("4.246×10⁻⁹", 0.000000004245753332.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix uses scientific notation for exponent less than -4`() {
        // exponent -6 → scientific notation
        assertEquals("1.234×10⁻⁶", 0.000001234.formatWithSuffix())
        // exponent -7 → scientific notation
        assertEquals("5×10⁻⁷", 0.0000005.formatWithSuffix())
        // exponent -8 → scientific notation
        assertEquals("1.235×10⁻⁸", 0.00000001234567.formatWithSuffix())
    }

    @Test
    fun `formatWithSuffix respects maxDecimals parameter`() {
        assertEquals("123.46", 123.456789.formatWithSuffix(maxDecimals = 2))
        assertEquals("123.5", 123.456789.formatWithSuffix(maxDecimals = 1))
        assertEquals("123.456789", 123.456789.formatWithSuffix(maxDecimals = 6))
    }

    @Test
    fun `formatWithSuffix respects maxSignificantDigits for scientific notation`() {
        assertEquals("4.25×10⁻⁹", 0.000000004245753332.formatWithSuffix(maxSignificantDigits = 3))
        assertEquals("4.2458×10⁻⁹", 0.000000004245753332.formatWithSuffix(maxSignificantDigits = 5))
    }

    @Test
    fun `formatWithSuffix strips trailing zeros`() {
        assertEquals("1", 1.0.formatWithSuffix())
        assertEquals("1.5", 1.50.formatWithSuffix())
        assertEquals("100", 100.0.formatWithSuffix())
    }

    // ===== formatWithScientificNotation tests =====

    @Test
    fun `formatWithScientificNotation formats the specific token balance correctly`() {
        // THE MAIN TEST CASE: 0.000000004245753332 → "4.246×10⁻⁹"
        assertEquals("4.246×10⁻⁹", formatWithScientificNotation(0.000000004245753332))
    }

    @Test
    fun `formatWithScientificNotation returns 0 for zero`() {
        assertEquals("0", formatWithScientificNotation(0.0))
    }

    @Test
    fun `formatWithScientificNotation uses superscript exponents`() {
        assertEquals("4.246×10⁻⁹", formatWithScientificNotation(0.000000004245753332))
        assertEquals("1.234×10⁻⁶", formatWithScientificNotation(0.000001234))
        assertEquals("5×10⁻⁷", formatWithScientificNotation(0.0000005))
        assertEquals("1×10⁻¹¹", formatWithScientificNotation(0.00000000001))
    }

    @Test
    fun `formatWithScientificNotation handles very small exponents`() {
        // exponent -11
        assertEquals("1.234×10⁻¹¹", formatWithScientificNotation(0.00000000001234))
        // exponent -13
        assertEquals("5.678×10⁻¹³", formatWithScientificNotation(0.0000000000005678))
    }

    @Test
    fun `formatWithScientificNotation respects significantDigits parameter`() {
        assertEquals("4.2×10⁻⁹", formatWithScientificNotation(0.000000004245753332, 2))
        assertEquals("4.2458×10⁻⁹", formatWithScientificNotation(0.000000004245753332, 5))
    }

    @Test
    fun `formatWithScientificNotation handles negative very small numbers`() {
        assertEquals("-4.246×10⁻⁹", formatWithScientificNotation(-0.000000004245753332))
    }

    @Test
    fun `formatWithScientificNotation falls back to regular format for exponent greater than or equal to -4`() {
        // Exponent >= -4 uses regular formatting
        assertEquals("0.0001", formatWithScientificNotation(0.0001))
        assertEquals("0.001234", formatWithScientificNotation(0.001234))
    }

    // ===== formatSmart tests =====

    @Test
    fun `formatSmart returns 0 for zero value`() {
        assertEquals("0", formatSmart(0.0))
    }

    @Test
    fun `formatSmart formats integer values without decimal`() {
        assertEquals("1", formatSmart(1.0))
        assertEquals("100", formatSmart(100.0))
        assertEquals("1000", formatSmart(1000.0))
    }

    @Test
    fun `formatSmart formats regular decimal values`() {
        assertEquals("123.46", formatSmart(123.456789))
        assertEquals("1.50", formatSmart(1.5))
    }

    // ===== abbreviateNumber tests =====

    @Test
    fun `abbreviateNumber formats regular numbers`() {
        assertEquals("950", abbreviateNumber(950.0))
        assertEquals("100", abbreviateNumber(100.0))
    }

    @Test
    fun `abbreviateNumber adds K suffix for thousands`() {
        assertEquals("1.23K", abbreviateNumber(1234.0))
        assertEquals("10K", abbreviateNumber(10000.0))
    }

    @Test
    fun `abbreviateNumber adds M suffix for millions`() {
        assertEquals("2.5M", abbreviateNumber(2500000.0))
    }

    @Test
    fun `abbreviateNumber adds B suffix for billions`() {
        assertEquals("7.89B", abbreviateNumber(7890000000.0))
    }

    // ===== formatAddress tests =====

    @Test
    fun `formatAddress truncates long addresses`() {
        assertEquals("0x12...5678", formatAddress("0x1234567890abcdef5678"))
        assertEquals("abcd...wxyz", formatAddress("abcdefghijklmnopqrstuvwxyz"))
    }

    @Test
    fun `formatAddress returns short addresses unchanged`() {
        assertEquals("12345678", formatAddress("12345678"))
        assertEquals("short", formatAddress("short"))
    }

    @Test
    fun `formatAddress respects visibleChars parameter`() {
        assertEquals("0x1234...ef5678", formatAddress("0x1234567890abcdef5678", visibleChars = 6))
        assertEquals("0x...78", formatAddress("0x1234567890abcdef5678", visibleChars = 2))
    }
}

