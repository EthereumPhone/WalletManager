package com.feature.send

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure unit tests for amount input validation logic.
 * 
 * These tests verify the regex pattern used in AmountTextField
 * and the removeDots sanitization function.
 * 
 * No Android dependencies required - runs on JVM only.
 */
class AmountInputValidationTest {

    // The regex pattern used in AmountTextField for input filtering
    private val amountInputPattern = "^\\d*\\.?\\d*$".toRegex()

    // ============================================
    // SECTION 1: VALID INPUT PATTERNS
    // ============================================

    @Test
    fun `regex should accept integer numbers`() {
        assertTrue(amountInputPattern.matches("0"))
        assertTrue(amountInputPattern.matches("1"))
        assertTrue(amountInputPattern.matches("123"))
        assertTrue(amountInputPattern.matches("999999999"))
    }

    @Test
    fun `regex should accept decimal numbers`() {
        assertTrue(amountInputPattern.matches("0.0"))
        assertTrue(amountInputPattern.matches("1.5"))
        assertTrue(amountInputPattern.matches("123.456"))
        assertTrue(amountInputPattern.matches("0.123456789"))
    }

    @Test
    fun `regex should accept leading decimal (no zero before dot)`() {
        assertTrue(amountInputPattern.matches(".5"))
        assertTrue(amountInputPattern.matches(".123"))
        assertTrue(amountInputPattern.matches(".0"))
    }

    @Test
    fun `regex should accept trailing decimal (no digits after dot)`() {
        assertTrue(amountInputPattern.matches("0."))
        assertTrue(amountInputPattern.matches("1."))
        assertTrue(amountInputPattern.matches("123."))
    }

    @Test
    fun `regex should accept empty string`() {
        assertTrue(amountInputPattern.matches(""))
    }

    @Test
    fun `regex should accept just a decimal point`() {
        assertTrue(amountInputPattern.matches("."))
    }

    // ============================================
    // SECTION 2: INVALID INPUT PATTERNS
    // ============================================

    @Test
    fun `regex should reject letters`() {
        assertFalse(amountInputPattern.matches("abc"))
        assertFalse(amountInputPattern.matches("12a3"))
        assertFalse(amountInputPattern.matches("1.5e"))
        assertFalse(amountInputPattern.matches("ETH"))
    }

    @Test
    fun `regex should reject negative numbers`() {
        assertFalse(amountInputPattern.matches("-5"))
        assertFalse(amountInputPattern.matches("-0.5"))
        assertFalse(amountInputPattern.matches("-123.456"))
    }

    @Test
    fun `regex should reject plus sign`() {
        assertFalse(amountInputPattern.matches("+5"))
        assertFalse(amountInputPattern.matches("+0.5"))
    }

    @Test
    fun `regex should reject commas (thousand separators)`() {
        assertFalse(amountInputPattern.matches("1,000"))
        assertFalse(amountInputPattern.matches("1,000.00"))
        assertFalse(amountInputPattern.matches("1,234,567.89"))
    }

    @Test
    fun `regex should reject currency symbols`() {
        assertFalse(amountInputPattern.matches("$100"))
        assertFalse(amountInputPattern.matches("€50"))
        assertFalse(amountInputPattern.matches("100$"))
    }

    @Test
    fun `regex should reject scientific notation`() {
        assertFalse(amountInputPattern.matches("1e5"))
        assertFalse(amountInputPattern.matches("1E5"))
        assertFalse(amountInputPattern.matches("1.5e-3"))
    }

    @Test
    fun `regex should reject spaces`() {
        assertFalse(amountInputPattern.matches(" 5"))
        assertFalse(amountInputPattern.matches("5 "))
        assertFalse(amountInputPattern.matches("1 000"))
        assertFalse(amountInputPattern.matches(" "))
    }

    @Test
    fun `regex should reject multiple decimal points`() {
        assertFalse(amountInputPattern.matches("1.2.3"))
        assertFalse(amountInputPattern.matches("1..2"))
        assertFalse(amountInputPattern.matches(".."))
        assertFalse(amountInputPattern.matches("1.2.3.4"))
    }

    @Test
    fun `regex should reject special characters`() {
        assertFalse(amountInputPattern.matches("1@2"))
        assertFalse(amountInputPattern.matches("1#2"))
        assertFalse(amountInputPattern.matches("1%2"))
        assertFalse(amountInputPattern.matches("1/2"))
        assertFalse(amountInputPattern.matches("1*2"))
    }

    // ============================================
    // SECTION 3: REMOVE DOTS FUNCTION TESTS
    // ============================================

    private fun removeDots(s: String): String {
        val firstDotIndex = s.indexOf('.')
        if (firstDotIndex == -1) return s

        val beforeFirstDot = s.substring(0, firstDotIndex + 1)
        val afterFirstDot = s.substring(firstDotIndex + 1).replace(".", "")

        return beforeFirstDot + afterFirstDot
    }

    @Test
    fun `removeDots should return same string when no dots present`() {
        assertEquals("123", removeDots("123"))
        assertEquals("0", removeDots("0"))
        assertEquals("", removeDots(""))
    }

    @Test
    fun `removeDots should return same string when only one dot present`() {
        assertEquals("1.5", removeDots("1.5"))
        assertEquals("0.123", removeDots("0.123"))
        assertEquals(".5", removeDots(".5"))
        assertEquals("5.", removeDots("5."))
    }

    @Test
    fun `removeDots should remove second dot when two dots present`() {
        assertEquals("1.23", removeDots("1.2.3"))
        assertEquals("0.12", removeDots("0.1.2"))
        assertEquals("12.34", removeDots("12.3.4"))
    }

    @Test
    fun `removeDots should handle consecutive dots`() {
        assertEquals("1.5", removeDots("1..5"))
        assertEquals("0.", removeDots("0.."))
        assertEquals(".5", removeDots("..5"))
    }

    @Test
    fun `removeDots should keep only the first dot when multiple dots are present`() {
        assertEquals("1.249", removeDots("1.2.4.9"))
        assertEquals("1.234", removeDots("1.2.3.4"))
        assertEquals(".12", removeDots(".1.2."))
    }

    // ============================================
    // SECTION 4: EDGE CASES
    // ============================================

    @Test
    fun `regex should handle very long numbers`() {
        val longNumber = "9".repeat(50)
        assertTrue(amountInputPattern.matches(longNumber))
    }

    @Test
    fun `regex should handle many decimal places`() {
        assertTrue(amountInputPattern.matches("0.123456789012345678901234567890"))
    }

    @Test
    fun `regex should handle zero variations`() {
        assertTrue(amountInputPattern.matches("0"))
        assertTrue(amountInputPattern.matches("00"))
        assertTrue(amountInputPattern.matches("000"))
        assertTrue(amountInputPattern.matches("0.0"))
        assertTrue(amountInputPattern.matches("0.00"))
        assertTrue(amountInputPattern.matches("00.00"))
    }

    // ============================================
    // SECTION 5: DOT NORMALIZATION LOGIC
    // ============================================

    /**
     * Tests the dot normalization logic (converting ".X" to "0.X")
     * This mirrors behavior in updateAmount()
     */
    private fun normalizeLeadingDot(amount: String): String {
        return if (amount.startsWith(".")) "0$amount" else amount
    }

    @Test
    fun `normalizeLeadingDot should convert lone dot to zero dot`() {
        assertEquals("0.", normalizeLeadingDot("."))
    }

    @Test
    fun `normalizeLeadingDot should add leading zero when starting with dot`() {
        assertEquals("0.5", normalizeLeadingDot(".5"))
        assertEquals("0.12", normalizeLeadingDot(".12"))
        assertEquals("0.123456", normalizeLeadingDot(".123456"))
    }

    @Test
    fun `normalizeLeadingDot should not modify inputs not starting with dot`() {
        assertEquals("1.5", normalizeLeadingDot("1.5"))
        assertEquals("0.5", normalizeLeadingDot("0.5"))
        assertEquals("", normalizeLeadingDot(""))
        assertEquals("123", normalizeLeadingDot("123"))
    }

    // ============================================
    // SECTION 6: COMBINED SANITIZATION FLOW
    // ============================================

    /**
     * Full sanitization pipeline as used in updateAmount()
     */
    private fun sanitizeAmount(amount: String): String {
        val normalized = if (amount.startsWith(".")) "0$amount" else amount
        return removeDots(normalized)
    }

    @Test
    fun `sanitizeAmount should handle typical user inputs`() {
        assertEquals("123", sanitizeAmount("123"))
        assertEquals("1.5", sanitizeAmount("1.5"))
        assertEquals("0.", sanitizeAmount("."))
        assertEquals("1.23", sanitizeAmount("1.2.3"))
    }

    @Test
    fun `sanitizeAmount should add leading zero for inputs starting with dot`() {
        assertEquals("0.12", sanitizeAmount(".12"))
        assertEquals("0.5", sanitizeAmount(".5"))
        assertEquals("0.123456", sanitizeAmount(".123456"))
    }

    @Test
    fun `sanitizeAmount should handle malformed inputs`() {
        assertEquals("1.5", sanitizeAmount("1..5"))
        assertEquals("0.12", sanitizeAmount("0.1.2"))
    }

    @Test
    fun `sanitizeAmount should handle leading dot with multiple dots`() {
        // ".1.2" -> "0.1.2" -> "0.12"
        assertEquals("0.12", sanitizeAmount(".1.2"))
        // ".1.2.3" -> "0.1.2.3" -> "0.123"
        assertEquals("0.123", sanitizeAmount(".1.2.3"))
    }
}

