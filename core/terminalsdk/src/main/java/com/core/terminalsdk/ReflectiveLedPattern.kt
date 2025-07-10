package com.core.terminalsdk

import android.util.Log
import java.lang.reflect.Method
import kotlin.math.max
import kotlin.math.min

class ReflectiveLedPattern {
    // No instance required – all methods in LedPatterns are static.
    private var mPlusMethod: Method? = null
    private var mMinusMethod: Method? = null
    private var mArrowUpMethod: Method? = null
    private var mArrowDownMethod: Method? = null
    private var mSwapMethod: Method? = null
    private var mSignMethod: Method? = null
    private var mSuccessMethod: Method? = null
    private var mErrorMethod: Method? = null
    private var mWarningMethod: Method? = null
    private var mChadMethod: Method? = null
    private var mInfoMethod: Method? = null
    private var mClearMethod: Method? = null

    private lateinit var reflectiveLedManager: ReflectiveLedManager

    init {
        try {
            // 1. Load the LedPatterns utility class (hidden in the OS)
            val patternsClass = Class.forName("android.os.LedPatterns")

            // 2. Cache the plus() method with a String argument (static)
            //    The OS implementation handles a null argument gracefully.
            mPlusMethod = patternsClass.getMethod("plus", String::class.java)
            mMinusMethod = patternsClass.getMethod("minus", String::class.java)
            mArrowUpMethod = patternsClass.getMethod("arrowUp", String::class.java)
            mArrowDownMethod = patternsClass.getMethod("arrowDown", String::class.java)
            mSwapMethod = patternsClass.getMethod("swap", String::class.java)
            mSignMethod = patternsClass.getMethod("sign", String::class.java)
            mSuccessMethod = patternsClass.getMethod("success", String::class.java)
            mErrorMethod = patternsClass.getMethod("error", String::class.java)
            mWarningMethod = patternsClass.getMethod("warning", String::class.java)
            mChadMethod = patternsClass.getMethod("chad", String::class.java)
            mInfoMethod = patternsClass.getMethod("info", String::class.java)

            // clear() has no parameters
            mClearMethod = patternsClass.getMethod("clear")

            reflectiveLedManager = ReflectiveLedManager()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ReflectiveLedPattern via reflection", e)
        }
    }

    /**
     * Displays the built-in "plus" pattern defined in android.os.LedPatterns.
     * @param color Hex ARGB colour string. If null, the default colour is used.
     */
    /**
     * Displays the built-in "plus" pattern defined in android.os.LedPatterns.
     * Uses the no-argument overload which renders the pattern in the default colour.
     */
    @JvmOverloads
    fun displayPlus(color: String? = null) {
        try {
            if (mPlusMethod != null) {
                mPlusMethod?.invoke(null, color) // static invocation – no receiver
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke LedPatterns.plus()", e)
        }
    }

    // Convenience wrappers for additional patterns *************************************
    @JvmOverloads
    fun displayMinus(color: String? = null) {
        invokePattern(mMinusMethod, "minus", color)
    }

    @JvmOverloads
    fun displayArrowUp(color: String? = null) {
        invokePattern(mArrowUpMethod, "arrowUp", color)
    }

    @JvmOverloads
    fun displayArrowDown(color: String? = null) {
        invokePattern(mArrowDownMethod, "arrowDown", color)
    }

    @JvmOverloads
    fun displaySwap(color: String? = null) {
        invokePattern(mSwapMethod, "swap", color)
    }

    @JvmOverloads
    fun displaySign(color: String? = null) {
        invokePattern(mSignMethod, "sign", color)
    }

    @JvmOverloads
    fun displaySuccess(color: String? = null) {
        invokePattern(mSuccessMethod, "success", color)
    }

    @JvmOverloads
    fun displayError(color: String? = null) {
        invokePattern(mErrorMethod, "error", color)
    }

    @JvmOverloads
    fun displayWarning(color: String? = null) {
        invokePattern(mWarningMethod, "warning", color)
    }

    @JvmOverloads
    fun displayChad(color: String? = null) {
        invokePattern(mChadMethod, "chad", color)
    }

    @JvmOverloads
    fun displayInfo(color: String? = null) {
        invokePattern(mInfoMethod, "info", color)
    }

    /** Clears all LEDs regardless of previous pattern.  */
    fun clear() {
        try {
            if (mClearMethod != null) {
                mClearMethod?.invoke(null) // no args
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke LedPatterns.clear()", e)
        }
    }

    fun setup() {
        reflectiveLedManager.setupLed("1")
    }

    // Internal helper for common reflection pattern
    private fun invokePattern(method: Method?, name: String?, color: String?) {
        try {
            method?.invoke(null, color)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke LedPatterns.$name()", e)
        }
    }

    /**
     * Adjusts the brightness of an RGB colour.
     *
     * @param hexColor  The colour string in the form "0xRRGGBB" or "#RRGGBB" (case-insensitive).
     * Alpha (ARGB) formats of length 8 characters (e.g. "0xAARRGGBB") are also accepted –
     * in this case the alpha portion is preserved and only the RGB channels are scaled.
     * @param brightnessPercent An integer from 0–100 representing the desired brightness where 0 produces
     * black and 100 leaves the colour unchanged. Values outside the range are
     * clamped.
     * @return A new colour string in the same "0xRRGGBB" format (alpha is discarded) that reflects the
     * requested brightness. If parsing fails, the original input is returned.
     */
    fun applyBrightness(hexColor: String?, brightnessPercent: Int): String? {
        if (hexColor == null) {
            return null
        }

        // Clamp brightness to the 0–100 range.
        val clamped = max(0.0, min(brightnessPercent.toDouble(), 100.0)).toInt()

        // Strip common prefixes.
        var hex = if (hexColor.startsWith("0x") || hexColor.startsWith("0X"))
            hexColor.substring(2)
        else
            hexColor
        if (hex.startsWith("#")) {
            hex = hex.substring(1)
        }

        // Accept either RRGGBB (6) or AARRGGBB (8) length strings.
        if (hex.length != 6 && hex.length != 8) {
            // Unrecognised format – return the original string untouched.
            return hexColor
        }

        // If ARGB, drop the alpha part for brightness scaling but remember it to prepend later.
        var alphaPart = ""
        if (hex.length == 8) {
            alphaPart = hex.substring(0, 2)
            hex = hex.substring(2)
        }

        val rgb: Int
        try {
            rgb = hex.toInt(16)
        } catch (nfe: NumberFormatException) {
            // Parsing failed – return original string for safety.
            return hexColor
        }

        var r = (rgb shr 16) and 0xFF
        var g = (rgb shr 8) and 0xFF
        var b = rgb and 0xFF

        val factor = clamped / 100f
        r = Math.round(r * factor)
        g = Math.round(g * factor)
        b = Math.round(b * factor)

        // Ensure values stay within 0–255.
        r = max(0.0, min(r.toDouble(), 255.0)).toInt()
        g = max(0.0, min(g.toDouble(), 255.0)).toInt()
        b = max(0.0, min(b.toDouble(), 255.0)).toInt()

        val adjusted = String.format("0x%s%02X%02X%02X", alphaPart, r, g, b)
        return adjusted
    }

    companion object {
        private const val TAG = "reflectiveLedPattern"
    }
}