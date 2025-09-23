package com.core.ui.util

import android.os.SystemClock
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.util.concurrent.atomic.AtomicLong

/**
 * Composable helper function to create a debounced click handler.
 * Can be used with components that don't directly accept Modifiers.
 * 
 * @param intervalMillis The minimum time interval between clicks in milliseconds
 * @return A lambda that wraps the provided action with debouncing logic
 */
@Composable
fun rememberDebouncedClickHandler(
    intervalMillis: Long = 300L,
): (action: () -> Unit) -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    return { action ->
        val now = SystemClock.uptimeMillis()
        if (now - lastClickTime >= intervalMillis) {
            lastClickTime = now
            action()
        }
    }
}