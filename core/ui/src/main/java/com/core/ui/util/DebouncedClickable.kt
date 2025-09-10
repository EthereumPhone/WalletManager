package com.core.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import java.util.concurrent.atomic.AtomicLong

/**
 * Extension function for Modifier that adds debounced click behavior.
 * Prevents multiple rapid clicks from triggering multiple actions.
 * 
 * @param intervalMillis The minimum time interval between clicks in milliseconds
 * @param onClick The action to perform when clicked (after debouncing)
 */
fun Modifier.debouncedClickable(
    intervalMillis: Long = 1000L,
    onClick: () -> Unit
): Modifier = composed {
    val lastClickTime = remember { AtomicLong(0) }
    
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
    ) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime.get() > intervalMillis) {
            lastClickTime.set(now)
            onClick()
        }
    }
}

/**
 * Composable helper function to create a debounced click handler.
 * Can be used with components that don't directly accept Modifiers.
 * 
 * @param intervalMillis The minimum time interval between clicks in milliseconds
 * @return A lambda that wraps the provided action with debouncing logic
 */
@Composable
fun rememberDebouncedClickHandler(
    intervalMillis: Long = 1000L
): (action: () -> Unit) -> Unit {
    val lastClickTime = remember { AtomicLong(0) }
    
    return remember(intervalMillis) {
        { action ->
            val now = System.currentTimeMillis()
            if (now - lastClickTime.get() > intervalMillis) {
                lastClickTime.set(now)
                action()
            }
        }
    }
}
