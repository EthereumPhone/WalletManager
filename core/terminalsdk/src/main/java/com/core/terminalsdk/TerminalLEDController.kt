package com.core.terminalsdk

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*

/**
 * Lightweight singleton controller for Terminal LED and display operations.
 * More efficient than ViewModel for hardware-level operations.
 */
object TerminalLEDController {
    private const val TAG = "TerminalLEDController"

    private var applicationContext: Context? = null
    private var terminal: TerminalSDK? = null
    private var ledPattern: ReflectiveLedPattern? = null
    private var isInitialized = false
    private var systemColorHex: String? = null
    
    // Lightweight coroutine scope for async operations
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Initialize the controller with context. Call this once in Application.onCreate()
     * or MainActivity.onCreate()
     */
    @JvmStatic
    fun initialize(context: Context) {
        if (isInitialized) return
        
        try {
            // Store application context to avoid memory leaks
            applicationContext = context.applicationContext
            
            terminal = TerminalSDK(applicationContext!!)
            ledPattern = ReflectiveLedPattern()
            ledPattern?.setup()
            
            // Cache system color
            updateSystemColor()
            
            isInitialized = true
            Log.d(TAG, "TerminalLEDController initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TerminalLEDController", e)
        }
    }
    
    /**
     * Get system accent color as hex string
     */
    private fun getSystemColorHex(): String? {
        return applicationContext?.let { context ->
            val accentInt = android.provider.Settings.Secure.getInt(
                context.contentResolver,
                "systemui_accent_color",
                0xFFFF0000.toInt()  // Default red
            )
            String.format("0x%08X", accentInt)
        }
    }
    
    /**
     * Update system color (call when theme changes)
     */
    @JvmStatic
    fun updateSystemColor() {
        systemColorHex = getSystemColorHex()
    }
    
    // ========== LED Pattern Methods ==========

    /**
     * Display chad pattern with system color
     */
    @JvmStatic
    fun displayChadPattern() {
        updateSystemColor()  // Refresh system color before displaying
        ledPattern?.displayChad(systemColorHex)
        Log.d(TAG, "Chad pattern displayed with color: $systemColorHex")
    }

    
    /**
     * Display success pattern
     */
    @JvmStatic
    fun displaySuccessPattern() {
        ledPattern?.displaySuccess()
        Log.d(TAG, "Success pattern displayed")
    }
    
    /**
     * Display error pattern
     */
    @JvmStatic
    fun displayErrorPattern() {
        ledPattern?.displayError()
        Log.d(TAG, "Error pattern displayed")
    }
    
    /**
     * Flash success pattern for 1 second then return to chad
     */
    @JvmStatic
    fun flashSuccess() {
        scope.launch {
            withContext(Dispatchers.Main) {
                ledPattern?.displaySuccess()
            }
            delay(1000)
            withContext(Dispatchers.Main) {
                updateSystemColor()  // Refresh color in case it changed
                ledPattern?.displayChad(systemColorHex)
            }
            Log.d(TAG, "Success flash completed")
        }
    }
    
    /**
     * Flash error pattern for 1 second then return to chad
     */
    @JvmStatic
    fun flashError() {
        scope.launch {
            withContext(Dispatchers.Main) {
                ledPattern?.displayError()
            }
            delay(1000)
            withContext(Dispatchers.Main) {
                updateSystemColor()  // Refresh color in case it changed
                ledPattern?.displayChad(systemColorHex)
            }
            Log.d(TAG, "Error flash completed")
        }
    }

    /**
     * Display Info pattern
     */
    @JvmStatic
    fun displayInfoPattern() {
        ledPattern?.displayInfo()
        Log.d(TAG, "Info pattern displayed")
    }

    /**
     * Display Send pattern
     */
    @JvmStatic
    fun displaySendPattern() {
        ledPattern?.displayArrowUp()
        Log.d(TAG, "Send pattern displayed")
    }

    /**
     * Display Send pattern
     */
    @JvmStatic
    fun displayReceivePattern() {
        ledPattern?.displayArrowDown()
        Log.d(TAG, "Receive pattern displayed")
    }

    /**
     * Display Send pattern
     */
    @JvmStatic
    fun displaySignPattern() {
        updateSystemColor()
        ledPattern?.displaySign()
        Log.d(TAG, "Sign pattern displayed")
    }
    
    /**
     * Clear all LED patterns
     */
    @JvmStatic
    fun clearLED() {
        ledPattern?.clear()
        Log.d(TAG, "LED cleared")
    }
    
    // ========== Terminal Display Methods ==========
    
    /**
     * Display send terminal with action
     */
//    @JvmStatic
//    fun displaySendTerminal(action: () -> Unit) {
//        scope.launch {
//            terminal?.displaySend(action)
//            Log.d(TAG, "Send terminal displayed")
//        }
//    }
//
//    /**
//     * Remove send terminal
//     */
//    @JvmStatic
//    fun removeSendTerminal() {
//        scope.launch {
//            terminal?.removeSend()
//            Log.d(TAG, "Send terminal removed")
//        }
//    }
    
    /**
     * Display text on black background
     */
    @JvmStatic
    fun displayBlackText(text: String) {
        scope.launch {
            terminal?.displayBlackText(text)
            Log.d(TAG, "Black text displayed: $text")
        }
    }
    
    /**
     * Finish terminal screen
     */
    @JvmStatic
    fun finishScreen() {
        scope.launch {
            terminal?.finishScreen()
            Log.d(TAG, "Terminal screen finished")
        }
    }
    
    // ========== Cleanup Methods ==========
    
    /**
     * Synchronous cleanup (call in onDestroy)
     */
    @JvmStatic
    fun cleanupSync() {
        terminal?.destroyTouchHandlerSync()
        ledPattern?.clear()
        Log.d(TAG, "Sync cleanup completed")
    }
    
    /**
     * Full cleanup and reset
     */
    @JvmStatic
    fun destroy() {
        cleanupSync()
        scope.cancel()
        terminal = null
        ledPattern = null
        isInitialized = false
        Log.d(TAG, "Controller destroyed")
    }
    
    // ========== Status Methods ==========
    
    /**
     * Check if terminal is available
     */
    @JvmStatic
    suspend fun isAvailable(): Boolean = terminal?.isAvailable() ?: false
    
    /**
     * Check if terminal screen is on
     */
    @JvmStatic
    suspend fun isScreenOn(): Boolean = terminal?.isScreenOn() ?: false
}
