package com.feature.swap.ui

import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenGunMetal
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.extraLargeEnterDuration
import com.core.ui.util.extraLargeExitDuration
import com.core.ui.util.mediumEnterDuration
import com.core.ui.util.pulseOpacity
import com.feature.swap.R
import kotlinx.coroutines.delay

sealed class SwapTransactionStatus {
    object PENDING : SwapTransactionStatus()
    object SUCCESS : SwapTransactionStatus()
    data class FAILURE(val errorMessage: String? = null) : SwapTransactionStatus()
}

@Composable
fun SwapTransactionStatusOverlay(
    status: SwapTransactionStatus?,
    gifLoader: ImageLoader,
    primaryColor: Color,
    secondaryColor: Color,
    onDismiss: () -> Unit,
    dismissDelay: Long = 5000L
) {
    var displayStatus by remember { mutableStateOf<SwapTransactionStatus?>(null) }

    // Sync external status to our internal display status, but only when it's not null.
    // This makes our internal state "sticky" for the success/failure message.
    LaunchedEffect(status) {
        if (status != null) {
            displayStatus = status
        }
    }

    // This effect handles the dismissal logic based on our *internal* state.
    // It won't be cancelled prematurely by the external status becoming null.
    LaunchedEffect(displayStatus) {
        if (displayStatus is SwapTransactionStatus.SUCCESS || displayStatus is SwapTransactionStatus.FAILURE) {
            delay(dismissDelay)
            onDismiss()
            displayStatus = null // Hide the overlay after the delay.
        }
    }

    AnimatedVisibility(
        visible = displayStatus != null,
        enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = mediumEnterDuration))
    ) {
        // The rest of the UI is driven by our non-null internal state.
        val currentStatus = displayStatus ?: return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        Log.d("SwapTransactionOverlay", "Overlay tapped - dismissing")
                        //onDismiss()
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val pulsatingAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = pulseOpacity,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1500),
                        repeatMode = RepeatMode.Reverse
                    ), label = "pulsatingAlpha"
                )

                val blinkingAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.3f, // Not fully off, less aggressive
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ), label = "blinkingAlpha"
                )

                val targetColor = when (currentStatus) {
                    is SwapTransactionStatus.PENDING -> dgenTurqoise
                    is SwapTransactionStatus.SUCCESS -> dgenGreen
                    is SwapTransactionStatus.FAILURE -> dgenRed
                }

                val animatedBaseColor by animateColorAsState(
                    targetValue = targetColor,
                    animationSpec = tween(durationMillis = 2000), // Long fade
                    label = "baseColorAnimation"
                )

                AsyncImage(
                    modifier = Modifier
                        .size(350.dp)
                        .aspectRatio(1f),
                    imageLoader = gifLoader,
                    colorFilter = ColorFilter.tint(animatedBaseColor.copy(alpha = pulsatingAlpha)),
                    model = R.drawable.globe_transfer_thick,
                    contentDescription = "Status Animation"
                )

                AnimatedContent(
                    targetState = currentStatus,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = tween(extraLargeEnterDuration)
                        ) togetherWith fadeOut(animationSpec = tween(extraLargeExitDuration))
                    },
                    label = "textAnimation"
                ) { targetStatus ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 0.dp)
                    ) {
                        val mainText = when (targetStatus) {
                            is SwapTransactionStatus.PENDING -> "Swap Pending..."
                            is SwapTransactionStatus.SUCCESS -> "Swap Confirmed!"
                            is SwapTransactionStatus.FAILURE -> {
                                // Check if we have a custom error message
                                if (!targetStatus.errorMessage.isNullOrEmpty()) {
                                    targetStatus.errorMessage
                                } else {
                                    "Swap Failed"
                                }
                            }
                        }
                        Text(
                            text = mainText.uppercase(),
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor.copy(alpha = blinkingAlpha),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                        )
                        
                        // Add additional help text based on error type
                        if (targetStatus is SwapTransactionStatus.FAILURE) {
                            val helpText = when {
                                targetStatus.errorMessage?.contains("gas", ignoreCase = true) == true -> 
                                    "Add ETH to your wallet to pay for gas fees"
                                targetStatus.errorMessage?.contains("nonce", ignoreCase = true) == true -> 
                                    "Wait for pending transactions to complete"
                                targetStatus.errorMessage?.contains("signature", ignoreCase = true) == true -> 
                                    "Please try signing the transaction again"
                                targetStatus.errorMessage?.contains("expired", ignoreCase = true) == true -> 
                                    "Transaction took too long. Please try again"
                                targetStatus.errorMessage?.contains("insufficient funds", ignoreCase = true) == true -> 
                                    "Add more funds to your wallet"
                                targetStatus.errorMessage?.contains("paymaster", ignoreCase = true) == true -> 
                                    "Sponsorship service unavailable. Try again later"
                                targetStatus.errorMessage?.contains("account not deployed", ignoreCase = true) == true -> 
                                    "Your account needs to be activated first"
                                targetStatus.errorMessage?.contains("throttled", ignoreCase = true) == true -> 
                                    "Too many requests. Please wait and try again"
                                targetStatus.errorMessage?.contains("slippage", ignoreCase = true) == true -> 
                                    "Price moved too much. Try increasing slippage tolerance"
                                targetStatus.errorMessage?.contains("liquidity", ignoreCase = true) == true -> 
                                    "Not enough liquidity for this swap"
                                targetStatus.errorMessage?.contains("cross-chain", ignoreCase = true) == true -> 
                                    "Cross-chain swaps require bridge integration"
                                else -> null
                            }
                            
                            helpText?.let {
                                Text(
                                    text = it,
                                    style = TextStyle(
                                        fontFamily = PitagonsSans,
                                        color = primaryColor.copy(alpha = blinkingAlpha * 0.7f),
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        letterSpacing = 0.sp,
                                        textDecoration = TextDecoration.None,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Previews for different states
@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 420, heightDp = 420)
@Composable
fun SwapTransactionStatusOverlayPreviewPending() {
    val context = LocalContext.current
    var status: SwapTransactionStatus? = SwapTransactionStatus.PENDING

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    SwapTransactionStatusOverlay(
        status = status,
        gifLoader = gifEnabledLoader,
        primaryColor = dgenTurqoise,
        secondaryColor = dgenBlack,
        onDismiss = { status = null }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 720, heightDp = 720)
@Composable
fun SwapTransactionStatusOverlayPreviewSuccess() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.globe_transfer_thick),
                contentDescription = "Status Animation",
                modifier = Modifier
                    .size(480.dp)
                    .aspectRatio(1f),
                colorFilter = ColorFilter.tint(dgenGreen)
            )
            Text(
                text = "Swap Confirmed!",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenGunMetal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 420, heightDp = 420)
@Composable
fun SwapTransactionStatusOverlayPreviewFailure() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.globe_transfer_thick),
                contentDescription = "Status Animation",
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f),
                colorFilter = ColorFilter.tint(dgenRed)
            )
            Text(
                text = "Swap Failed".uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

