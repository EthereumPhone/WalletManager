package com.feature.receive.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite

@Composable
fun TruncatedAddress(
    text: String,
    trimStart: Int = 4,
    trimEnd: Int = 4,
    animationDuration: Int = 300,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Decide what to display
    val displayText = remember(text, expanded) {
        if (expanded || text.length <= trimStart + trimEnd) {
            text
        } else {
            val startSegment = text.take(trimStart)
            val endSegment   = text.takeLast(trimEnd)
            "$startSegment…$endSegment"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.animateContentSize(tween(durationMillis = animationDuration)).width(350.dp)
    ) {
        Text(
            text = displayText,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 24.sp,
                letterSpacing = 1.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = modifier

                .clickable { expanded = !expanded }
                .animateContentSize(tween(durationMillis = animationDuration))
                //.padding(8.dp)
        )
        AnimatedVisibility(
            !expanded
        ) {
            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Address",
                tint = dgenTurqoise,
                modifier= Modifier.size(32.dp)
            )
        }
    }

}