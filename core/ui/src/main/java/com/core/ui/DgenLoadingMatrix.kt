package com.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import kotlinx.coroutines.delay

@Composable
fun DgenLoadingMatrix(
    size: Dp = 120.dp,
    LEDSize: Dp = 30.dp,
    unactiveLEDColor: Color = dgenOcean,
    activeLEDColor: Color = dgenTurqoise
) {

    var activatedBox by remember { mutableIntStateOf(0) }

    val alignments = listOf(
        Alignment.CenterStart,
        Alignment.TopStart,
        Alignment.TopCenter,
        Alignment.TopEnd,
        Alignment.CenterEnd,
        Alignment.BottomEnd,
        Alignment.BottomCenter,
        Alignment.BottomStart
    )

    Box(Modifier.size(size)) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(LEDSize)
                .clip(CircleShape)
                .background(unactiveLEDColor))
        alignments.forEachIndexed { index, alignment ->

            Box(
                Modifier
                    .align(alignment)
                    .size(LEDSize)
                    .clip(CircleShape)
                    .background(if (activatedBox == index) activeLEDColor else unactiveLEDColor)
            )
        }
    }

    LaunchedEffect(null) {
        while (true) {
            delay(100L)
            activatedBox = (activatedBox + 1) % alignments.size
        }
    }
}