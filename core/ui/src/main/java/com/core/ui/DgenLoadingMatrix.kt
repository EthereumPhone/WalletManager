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
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import kotlinx.coroutines.delay

@Composable
fun DgenLoadingMatrix() {

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

    Box(Modifier.size(120.dp)) {
        Box(
            Modifier
                .align(Alignment.Center)
                .size(30.dp)
                .clip(CircleShape)
                .background(dgenOcean))
        alignments.forEachIndexed { index, alignment ->

            Box(
                Modifier
                    .align(alignment)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (activatedBox == index) dgenTurqoise else dgenOcean)
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