package com.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeButton(
    text: String,
    icon: ImageVector,
    completeIcon: ImageVector,
    modifier: Modifier = Modifier,
    //enabled: Boolean,
//    result: String,
//    swipeableState: SwipeableState<Int>,
//    swipeComplete: Boolean,
//    setSwipeComplete: (Boolean) -> Unit,
    onSwipe: () -> Unit
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val (swipeComplete, setSwipeComplete) = remember {
        mutableStateOf(false)
    }
    val alpha: Float by animateFloatAsState(
        if(swipeableState.offset.value > 10f) (1 - swipeableState.progress.fraction) else 1f
    )

    LaunchedEffect(key1 = swipeableState.currentValue) {
        if(swipeableState.currentValue == 1) {
            setSwipeComplete(true)
            onSwipe()
        }
    }
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(if (swipeComplete) PaddingValues(SwipeButtonDefaults.verticalPadding) else SwipeButtonDefaults.paddingValues)
            .animateContentSize()
            .then(
                if (swipeComplete) {
                    Modifier.size(SwipeButtonDefaults.iconContainer)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
    ) {
        val iconSize = SwipeButtonDefaults.iconContainer
        val boxWidth = this.maxWidth
        val paddingWidth = SwipeButtonDefaults.horizontalPadding
        val swipeWidth = with(LocalDensity.current) {
            boxWidth.toPx() - iconSize.toPx() - paddingWidth.toPx()
        }
        val horizontalAnchors = mapOf(0f to 0, swipeWidth to 1)

        SwipeIndicator(
            icon = icon,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(swipeableState.offset.value.roundToInt(), 0)
                }
                .alpha(if (swipeComplete) 0f else 1f)
                .swipeable(
                    state = swipeableState,
                    anchors = horizontalAnchors,
                    thresholds = { _, _ ->
                        FractionalThreshold(0.5F)
                    },
                    orientation = Orientation.Horizontal
                )
        )
        Text(
            text = text,
            color = Color(0xFF24303D),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(if (swipeComplete) 0f else alpha)
        )
        AnimatedVisibility(
            visible = swipeComplete,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Icon(
                completeIcon,
                contentDescription = "",
                modifier = Modifier.padding(2.dp),
                tint = Color(0xFF24303D)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    //enabled: Boolean,
//    result: String,
//    swipeableState: SwipeableState<Int>,
//    swipeComplete: Boolean,
//    setSwipeComplete: (Boolean) -> Unit,
    onSwipe: () -> Unit
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val (swipeComplete, setSwipeComplete) = remember {
        mutableStateOf(false)
    }
    val alpha: Float by animateFloatAsState(
        if(swipeableState.offset.value > 10f) (1 - swipeableState.progress.fraction) else 1f
    )

    LaunchedEffect(key1 = swipeableState.currentValue) {
        if(swipeableState.currentValue == 1) {
            setSwipeComplete(true)
            onSwipe()
        }
    }
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(if (swipeComplete) PaddingValues(SwipeButtonDefaults.verticalPadding) else SwipeButtonDefaults.paddingValues)
            .animateContentSize()
            .then(
                if (swipeComplete) {
                    Modifier.size(SwipeButtonDefaults.iconContainer)
                } else {
                    Modifier.fillMaxWidth()
                }
            )
    ) {
        val iconSize = SwipeButtonDefaults.iconContainer
        val boxWidth = this.maxWidth
        val paddingWidth = SwipeButtonDefaults.horizontalPadding
        val swipeWidth = with(LocalDensity.current) {
            boxWidth.toPx() - iconSize.toPx() - paddingWidth.toPx()
        }
        val horizontalAnchors = mapOf(0f to 0, swipeWidth to 1)

        SwipeIndicator(
            icon = icon,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    IntOffset(swipeableState.offset.value.roundToInt(), 0)
                }
                .alpha(if (swipeComplete) 0f else 1f)
                .swipeable(
                    state = swipeableState,
                    anchors = horizontalAnchors,
                    thresholds = { _, _ ->
                        FractionalThreshold(0.5F)
                    },
                    orientation = Orientation.Horizontal
                )
        )
        Text(
            text = text,
            color = Color(0xFF24303D),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(if (swipeComplete) 0f else alpha)
        )
        AnimatedVisibility(
            visible = swipeComplete,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val strokeWidth = 5.dp

            CircularProgressIndicator(
                modifier = Modifier.drawBehind {
                    drawCircle(
                        Color.Red,
                        radius = size.width / 2 - strokeWidth.toPx() / 2,
                        style = Stroke(strokeWidth.toPx())
                    )
                },
                color = Color.LightGray,
                strokeWidth = strokeWidth
            )
        }
    }
}

@Composable
fun SwipeIndicator(
    modifier: Modifier = Modifier,
    icon: ImageVector
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF24303D),
        modifier = modifier
            .size(36.dp)
    ) {
        Icon(
            icon,
            contentDescription = "",
            modifier = Modifier.padding(2.dp),
            tint = Color.White
        )
    }
}

object SwipeButtonDefaults {
    val horizontalPadding = 8.dp
    val verticalPadding = 8.dp
    val iconContainer = 30.dp

    val paddingValues = PaddingValues(
        horizontal = horizontalPadding,
        vertical = verticalPadding
    )
}