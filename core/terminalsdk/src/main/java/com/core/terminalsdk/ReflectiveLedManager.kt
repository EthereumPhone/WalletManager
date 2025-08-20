package com.core.terminalsdk

import android.util.Log
import java.lang.reflect.Method


/**
 * A wrapper for the LedManager service that uses reflection to interact with it.
 * This class is designed to work with a LedManager object without
 * having compile-time access to it.
 */
class ReflectiveLedManager {
    private var mLedProxyInstance: Any? = null

    private var mSetupLedMethod: Method? = null
    private var mOpenLedMethod: Method? = null
    private var mCloseLedMethod: Method? = null
    private var mSetLedBrightnessMethod: Method? = null
    private var mIsRunningMethod: Method? = null

    /**
     * Constructs a new ReflectiveLedManager.
     * This constructor uses reflection to access the hidden class `android.os.LedProxy`,
     * obtain its singleton instance, and cache the relevant LED-control methods.
     */
    init {
        try {
            // 1. Load the hidden LedProxy class
            val proxyClass = Class.forName("android.os.LedProxy")

            // 2. Obtain the static getInstance() method
            val getInstance = proxyClass.getMethod("getInstance")

            // 3. Invoke getInstance() to retrieve the singleton LedProxy instance
            mLedProxyInstance = getInstance.invoke(null)

            if (mLedProxyInstance == null) {
                Log.e(TAG, "LedProxy.getInstance() returned null – LED service unavailable")
            } else {
                Log.d(TAG, "LedProxy instance obtained successfully")
                // 4. Cache the LED-control methods
                mSetupLedMethod = proxyClass.getMethod("setupLed", String::class.java)
                mOpenLedMethod = proxyClass.getMethod("openLed", String::class.java, String::class.java)
                mCloseLedMethod = proxyClass.getMethod("closeLed", String::class.java)
                mSetLedBrightnessMethod = proxyClass.getMethod("setLedBrightness", String::class.java)

                // Cache the isRunning method
                try {
                    mIsRunningMethod = proxyClass.getMethod("isRunning")
                } catch (e: NoSuchMethodException) {
                    // Older OS version doesn't expose this method
                    Log.w(TAG, "isRunning() method not available on this OS version")
                    mIsRunningMethod = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ReflectiveLedManager via reflection", e)
        }
    }

    /**
     * Calls the setupLed method on the service.
     * @param status The status string.
     */
    fun setupLed(status: String?) {
        try {
            if (mLedProxyInstance != null && mSetupLedMethod != null) {
                mSetupLedMethod?.invoke(mLedProxyInstance, status)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke setupLed", e)
        }
    }

    /**
     * Calls the openLed method on the service.
     * @param ledId The ID of the LED.
     * @param color The color to set.
     */
    fun openLed(ledId: String?, color: String?) {
        try {
            if (mLedProxyInstance != null && mOpenLedMethod != null) {
                mOpenLedMethod?.invoke(mLedProxyInstance, ledId, color)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke openLed", e)
        }
    }

    /**
     * Calls the closeLed method on the service.
     * @param ledId The ID of the LED.
     */
    fun closeLed(ledId: String?) {
        try {
            if (mLedProxyInstance != null && mCloseLedMethod != null) {
                mCloseLedMethod?.invoke(mLedProxyInstance, ledId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke closeLed", e)
        }
    }

    /**
     * Calls the setLedBrightness method on the service.
     * @param brightness The brightness value.
     */
    fun setLedBrightness(brightness: String?) {
        try {
            if (mLedProxyInstance != null && mSetLedBrightnessMethod != null) {
                mSetLedBrightnessMethod?.invoke(mLedProxyInstance, brightness)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke setLedBrightness", e)
        }
    }

    /**
     * Check if any LEDs are currently on.
     * @return true if any LED is on, false if all LEDs are off or if the method is unavailable
     */
    fun isRunning(): Boolean {
        return try {
            if (mLedProxyInstance != null && mIsRunningMethod != null) {
                val result = mIsRunningMethod?.invoke(mLedProxyInstance)
                if (result is Boolean) {
                    result
                } else {
                    false
                }
            } else {
                Log.w(TAG, "isRunning() not available - LedProxy instance or method is null")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to invoke isRunning", e)
            false
        }
    }

    companion object {
        private const val TAG = "ReflectiveLedManager"
    }
}