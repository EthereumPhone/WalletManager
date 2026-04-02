package com.feature.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun CarouselScrollbar(
    itemCount: Int,
    currentPosition: Float,
    onScrollTo: (Float) -> Unit,
    primaryColor: Color,
    modifier: Modifier = Modifier,
) {
    if (itemCount <= 1) return

    val density = LocalDensity.current
    val trackWidthPx = with(density) { 5.dp.toPx() }
    val thumbHeightPx = with(density) { 32.dp.toPx() }
    val thumbWidthPx = with(density) { 5.dp.toPx() }
    val cornerRadius = with(density) { 3.dp.toPx() }

    var isTouching by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isTouching) 1f else 0f,
        animationSpec = tween(durationMillis = if (isTouching) 100 else 2000),
        label = "scrollbarAlpha"
    )

    fun yToPosition(y: Float, height: Float): Float {
        val topPad = thumbHeightPx / 2f
        val usable = height - topPad - topPad
        val fraction = ((y - topPad) / usable).coerceIn(0f, 1f)
        return fraction * (itemCount - 1)
    }

    Canvas(
        modifier = modifier
            .width(36.dp)
            .fillMaxHeight()
            .padding(end = 10.dp)
            .pointerInput(itemCount) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Press -> {
                                isTouching = true
                                val y = event.changes.first().position.y
                                onScrollTo(yToPosition(y, size.height.toFloat()))
                            }
                            PointerEventType.Move -> {
                                val y = event.changes.first().position.y
                                onScrollTo(yToPosition(y, size.height.toFloat()))
                                event.changes.forEach { it.consume() }
                            }
                            PointerEventType.Release -> {
                                isTouching = false
                            }
                        }
                    }
                }
            }
    ) {
        val h = size.height
        val centerX = size.width / 2f

        // Track
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.15f * alpha),
            topLeft = Offset(centerX - trackWidthPx / 2f, 0f),
            size = Size(trackWidthPx, h),
            cornerRadius = CornerRadius(cornerRadius)
        )

        // Thumb
        val fraction = if (itemCount <= 1) 0f
        else (currentPosition / (itemCount - 1)).coerceIn(0f, 1f)
        val thumbY = (fraction * (h - thumbHeightPx)).coerceIn(0f, h - thumbHeightPx)

        drawRoundRect(
            color = primaryColor.copy(alpha = 0.8f * alpha),
            topLeft = Offset(centerX - thumbWidthPx / 2f, thumbY),
            size = Size(thumbWidthPx, thumbHeightPx),
            cornerRadius = CornerRadius(cornerRadius)
        )
    }
}
