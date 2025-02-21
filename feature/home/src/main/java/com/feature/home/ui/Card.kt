package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun Card(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    rotated: Boolean = false,
    rotation: Float = 0f,
    frontSide: @Composable () -> Unit = {},
    backSide: @Composable () -> Unit = {}
){
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    val frontVisible = rotation < 90f
    val backVisible = rotation > 90f

    Surface(
        color = backgroundColor,
        modifier = modifier
            .aspectRatio(16f / 9f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
    ) {


        if (frontVisible) {
            frontSide()
        }
        if (backVisible) {
            backSide()
        }

    }
}

fun formatSmart(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString() // No decimal part, show as integer
    } else {
        String.format(Locale.US, "%.2f", value) // Show with two decimals
    }
}


@Preview
@Composable
fun PreviewCard(){
    Card(
        backgroundColor = Color(0xFF1E5A9C),
        frontSide = {
//            IdleView(
//                amount = 120.00,
//                tokenName = "USDC",
//                fiatAmount = 120.00,
//                chainList = listOf(1,10,8453,42161),
//                icon = R.drawable.usdc,
//            )
        }

    )
}


data class Asset(
    val amount: Double,
    val tokenName: String,
    val fiatAmount: Double,
    val chainList: List<Int>,
    val backgroundColor: Color,
    val icon: Int,
)
enum class TOKENACTION{
    IDLE,
    SEND,
    SWAP,
    GET
}

@Composable
fun IsolatedCardView(){

    var rotated by remember { mutableStateOf(false) }

    var startSwapAnimation by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
    )

    var isAnimating by remember { mutableStateOf(false) }
    var hasClicked by remember { mutableStateOf(false) } // Ensures animation does not run on first launch


    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.7f else 0.85f,
        animationSpec = tween(600, 200),
        label = "ScaleAnimation"
    )
    var translateY by remember { mutableStateOf(0f) }

    LaunchedEffect(isAnimating) {
        if (isAnimating && hasClicked) {
            Log.d("SWAP", "true")
            // Expand
            animate(
                initialValue = 0f,
                targetValue = 150f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) { value, _ -> translateY = value }

            delay(150)
            // Contract
            animate(
                initialValue = 150f,
                targetValue = 70f,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) { value, _ -> translateY = value }
            hasClicked = false
        } else {
            if(hasClicked){
                Log.d("SWAP", "Else")
                animate(
                    initialValue = 125f,
                    targetValue = 150f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) { value, _ -> translateY = value }

                delay(100)
                // Contract
                animate(
                    initialValue = 150f,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) { value, _ -> translateY = value }

                hasClicked = false
            }
        }
    }

    var zIndex1 by remember { mutableStateOf(1f) } // Default zIndex

    var zIndex2 by remember { mutableStateOf(2f) } // Default zIndex

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(350) // Wait for animation duration
            zIndex1 = 2f // Change zIndex after animation finishes
            zIndex2 = 1f // Reset when animation resets
        } else {
            zIndex1 = 1f // Change zIndex after animation finishes
            zIndex2 = 2f // Reset when animation resets
        }
    }


    var cardState by remember { mutableStateOf(TOKENACTION.IDLE) }




    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {

            Box(
                contentAlignment = Alignment.Center,
            ){

                Crossfade(
                    animationSpec = tween(300, if(isAnimating) 100 else 450),
                    modifier = Modifier.zIndex(zIndex1)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationY = translateY

                            cameraDistance = 12f * density
                        },
                    targetState = isAnimating, label = "cross fade") { showCard ->
                    if(showCard){
                        Card(
                            modifier = Modifier,
                            frontSide = {
                                Crossfade(
                                    modifier = Modifier.fillMaxSize(),
                                    targetState = isAnimating, label = "cross fade") { showSwap ->

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ){
                                        when (showSwap) {
                                            true -> {
                                                SecondSwapCardView(
                                                    amount = 120.00,
                                                    tokenName = "USDC",
                                                    icon = R.drawable.usdc,
                                                )
                                            }
                                            false-> {
                                                IdleView(
                                                    amount = 120.00,
                                                    tokenName = "USDC",
                                                    fiatAmount = 120.00,
                                                    chainList = listOf(1,10,8453,42161),
                                                    icon = R.drawable.usdc,
                                                )
                                            }
                                        }
                                    }
                                }

                            },
                            backgroundColor = Color(0xFF9C27B0),
                            backSide = {}
                        )
                    }

                }


                Card(
                    modifier = Modifier
                        .zIndex(zIndex2)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            rotationY = rotation
                            translationY = -translateY

                            cameraDistance = 12f * density
                        },
                    frontSide = {


                        Crossfade(
                            modifier = Modifier.fillMaxSize(),
                            targetState = isAnimating, label = "cross fade") { showSwap ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ){
                                when (showSwap) {
                                    true -> {
                                        SwapCardView(
                                            amount = 120.00,
                                            tokenName = "USDC",
                                            icon = R.drawable.usdc,
                                        )
                                    }
                                    false-> {
                                        IdleView(
                                            amount = 120.00,
                                            tokenName = "USDC",
                                            fiatAmount = 120.00,
                                            chainList = listOf(1,10,8453,42161),
                                            icon = R.drawable.usdc,
                                        )
                                    }
                                }
                            }
                        }
                    },
                    backgroundColor = Color(0xFF1E5A9C),
                    rotated = rotated,
                    rotation = rotation,
                    backSide = {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationY = -180f
                                },
                            contentAlignment = Alignment.Center
                        ) {

                            if(cardState == TOKENACTION.SEND){
                                SendCardView(
                                    amount = 120.00,
                                    tokenName = "USDC",
                                    chainList = listOf(1,10,8453,42161),

                                    )
                            }

                            if(cardState == TOKENACTION.SWAP){
                                SwapCardView(
                                    amount = 120.00,
                                    tokenName = "USDC",
                                    icon = R.drawable.usdc
                                )
                            }


                        }

//                        }

                    }
                )


                Crossfade(modifier = Modifier.graphicsLayer{ translationY = -60f}.zIndex(5f), targetState = isAnimating, animationSpec = if(isAnimating) tween(600, 800) else tween(200, 0)) { visible ->
                    if (visible){
                        CircleButton(
                            icon = {
                                Icon(
                                    modifier = Modifier.graphicsLayer(rotationZ = 90f).size(24.dp),
                                    painter = painterResource(R.drawable.baseline_swap_horiz_24),
                                    contentDescription = "Swap Icon"
                                )
                            },
                            containerColor = dgenOcean,
                            contentColor = dgenTurqoise,
                            buttonSize = 40.dp,
                            onClick = {
                                //rotated = !rotated
                                //isAnimating = !isAnimating
                                //startSwapAnimation = !startSwapAnimation
                                //hasClicked = true

                            }
                        )
                    }
                }
            }





            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                AnimatedVisibility(
                    (cardState != TOKENACTION.IDLE),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    CircleButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.baseline_close_24),
                                contentDescription = "Send Icon"
                            )
                        },
                        containerColor = dgenBurgendy,
                        contentColor = dgenRed ,
                        buttonSize = 48.dp,
                        onClick = {
                            rotated = false

                            isAnimating = false
                            hasClicked = true

                            cardState = TOKENACTION.IDLE
                        }
                    )
                }


                AnimatedVisibility(
                    visible = (cardState == TOKENACTION.IDLE) || (cardState == TOKENACTION.SEND),
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    CircleButton(
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.baseline_arrow_outward_24),
                                contentDescription = "Send Icon"
                            )
                        },
                        containerColor = if(cardState == TOKENACTION.SEND) dgenTurqoise else dgenOcean,
                        contentColor = if(cardState == TOKENACTION.SEND)  dgenOcean else dgenTurqoise ,
                        buttonSize = 48.dp,
                        onClick = {
                            rotated = !rotated
                            cardState = TOKENACTION.SEND
                        }
                    )
                }
            }
        }
    }

}

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Preview(
    showBackground = true,
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun IsolatedCardViewPreview(){
    IsolatedCardView()
}