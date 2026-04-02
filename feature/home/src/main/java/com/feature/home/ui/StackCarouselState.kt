package com.feature.home.ui

import androidx.compose.animation.core.Animatable
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

    suspend fun animateScrollTo(index: Int) {
        val target = index.toFloat().coerceIn(0f, (itemCount - 1).coerceAtLeast(0).toFloat())
        animatable.snapTo(scrollPosition)
        animatable.animateTo(target, spring(dampingRatio = 0.8f, stiffness = 300f)) {
            scrollPosition = value
        }
    }

    suspend fun snapTo(position: Float) {
        val clamped = position.coerceIn(0f, (itemCount - 1).coerceAtLeast(0).toFloat())
        animatable.snapTo(clamped)
        scrollPosition = clamped
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
