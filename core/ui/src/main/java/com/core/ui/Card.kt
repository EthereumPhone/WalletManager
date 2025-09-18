package com.core.ui

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise

@SuppressLint("SuspiciousIndentation")
@Composable
fun Card(
    isFirst: Boolean = true,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    frontSide: @Composable () -> Unit = {},
    backSide: @Composable () -> Unit = {},
    primaryColor: Color,
    secondaryColor: Color
) {

    val baseColor by animateColorAsState(if (isFirst) secondaryColor else dgenBlack, tween(300))

    Surface(
        color = baseColor,
        shape = RoundedCornerShape(0.dp),
        modifier = modifier
            .aspectRatio(16f / 9f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .border(2.dp, primaryColor, RoundedCornerShape(0.dp))
    ) {

        Box(
            Modifier
                .fillMaxSize()
        ) {
            val frontVisible = rotation < 90f
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = frontVisible,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(300))
            ) { frontSide() }

            val backVisible = rotation > 90f
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = backVisible,
                enter = fadeIn(tween(300)),
                exit  = fadeOut(tween(300))
            ) { backSide() }
        }
    }
}


@Preview
@Composable
fun _Preview() {
    //Card { /* content */ }
}
