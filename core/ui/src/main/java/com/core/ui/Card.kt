package com.core.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
@Composable
fun Card(
    isFirst: Boolean = true,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    frontSide: @Composable () -> Unit = {},
    backSide: @Composable () -> Unit = {},
) {

    // ── neon pulse ────────────────────────────────────────────────────────────────
    val pulse = rememberInfiniteTransition(label = "border‑pulse")
    val glowAlpha by pulse.animateFloat(
        initialValue = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ), label = "alpha"
    )

    val baseColor   = if (isFirst) dgenOcean else dgenBlack
    val borderBrush = Brush.horizontalGradient(
        0f   to Color(0xFF00F6FF).copy(alpha = glowAlpha),
        0.33f to Color(0xFF18A8FF).copy(alpha = glowAlpha),
        0.66f to Color(0xFF7458FF).copy(alpha = glowAlpha),
        1f   to Color(0xFF9E2BFF).copy(alpha = glowAlpha),
    )

    // ── card ──────────────────────────────────────────────────────────────────────
    Surface(
        color = baseColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .aspectRatio(16f / 9f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .border(1.dp, dgenTurqoise, RoundedCornerShape(0.dp))
    ) {
        // subtle wire‑frame grid (matches the visual reference)
        Box(
            Modifier
                .fillMaxSize()
                .drawBehind { drawGrid(8.dp, Color.White.copy(.04f)) }
        ) {
            val frontVisible = rotation < 90f
            AnimatedVisibility(
                visible = frontVisible,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(300))
            ) { frontSide() }

            val backVisible = rotation > 90f
            AnimatedVisibility(
                visible = backVisible,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(300))
            ) { backSide() }
        }
    }
}

/*──────────────────────── helpers ───────────────────────*/

private fun Modifier.neonBorder(
    brush: Brush,
    stroke: Dp,
    blur: Dp
) = drawBehind {
    val strokePx = stroke.toPx()
    val blurPx   = blur.toPx()
    val rect     = size.toRect().deflate(strokePx / 2)  // keep inner content crisp
    // outer glow
    drawRoundRect(
        brush   = brush,
        topLeft = rect.topLeft,
        size    = rect.size,
        cornerRadius = CornerRadius(10.dp.toPx()),
        style   = Stroke(strokePx),
        alpha   = 0.9f,
        blendMode = BlendMode.SrcOver
    )
    // subtle diffuse
    drawRoundRect(
        color   = Color.White,
        topLeft = rect.topLeft,
        size    = rect.size,
        cornerRadius = CornerRadius(10.dp.toPx()),
        style   = Stroke(strokePx),
        alpha   = 0.12f,
        blendMode = BlendMode.SrcOver
    )
}

private fun DrawScope.drawGrid(spacing: Dp, color: Color) {
    val step   = spacing.toPx()
    val cols   = (size.width  / step).toInt()
    val rows   = (size.height / step).toInt()

    for (i in 0..cols) drawLine(
        color, Offset(i * step, 0f), Offset(i * step, size.height), 1f
    )
    for (j in 0..rows) drawLine(
        color, Offset(0f, j * step), Offset(size.width, j * step), 1f
    )
}

@Preview
@Composable
fun _Preview() {
    Card { /* content */ }
}
