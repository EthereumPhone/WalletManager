package com.feature.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt

@Stable
class StackCarouselState(
    initialPosition: Float = 0f,
) {
    var scrollPosition by mutableFloatStateOf(initialPosition)
        internal set

    val currentIndex: Int get() = scrollPosition.roundToInt()

    internal val animatable = Animatable(initialPosition)

    /** Total number of items — set by the carousel composable. */
    internal var itemCount: Int = 0

    private val maxPos: Float get() = (itemCount - 1).coerceAtLeast(0).toFloat()

    suspend fun animateScrollTo(index: Int) {
        val target = index.toFloat().coerceIn(0f, maxPos)
        animatable.snapTo(scrollPosition)
        animatable.animateTo(target, spring(dampingRatio = 1f, stiffness = 300f)) {
            scrollPosition = value
        }
    }

    suspend fun snapTo(position: Float) {
        val clamped = position.coerceIn(0f, maxPos)
        animatable.snapTo(clamped)
        scrollPosition = clamped
    }

    /**
     * Fling with the given velocity (in scroll-position units per second),
     * coast via exponential decay, then snap to the nearest card.
     */
    suspend fun fling(velocity: Float) {
        animatable.snapTo(scrollPosition)
        // Coast: let the scroll decelerate naturally
        animatable.animateDecay(
            initialVelocity = velocity,
            animationSpec = exponentialDecay(frictionMultiplier = 2.5f)
        ) {
            scrollPosition = value.coerceIn(0f, maxPos)
        }
        // Clamp final position after decay finishes
        scrollPosition = scrollPosition.coerceIn(0f, maxPos)
        animatable.snapTo(scrollPosition)
        // Snap to nearest card
        val snapTarget = scrollPosition.roundToInt().toFloat().coerceIn(0f, maxPos)
        animatable.animateTo(snapTarget, spring(dampingRatio = 1f, stiffness = 300f)) {
            scrollPosition = value
        }
    }

    companion object {
        val Saver: Saver<StackCarouselState, Float> = Saver(
            save = { it.scrollPosition },
            restore = { StackCarouselState(initialPosition = it) }
        )
    }
}

@Composable
fun rememberStackCarouselState(initialPosition: Float = 0f): StackCarouselState {
    return rememberSaveable(saver = StackCarouselState.Saver) {
        StackCarouselState(initialPosition)
    }
}
