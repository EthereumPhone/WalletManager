package com.feature.send.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenGreen
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenOrche
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.dgenlibrary.ui.theme.label_fontSize
import com.example.dgenlibrary.ui.theme.mediumEnterDuration
import com.feature.send.R // Assuming R.drawable.globe_wireframe is in the feature.send module

enum class TransactionStatus {
    PENDING,
    SUCCESS,
    FAILURE
}

@Composable
fun TransactionStatusOverlay(
    status: TransactionStatus?,
    gifLoader: ImageLoader, // Actual Composable uses this
    onDismiss: () -> Unit
) {

    val infiniteTransition = rememberInfiniteTransition()
    val changeColor = infiniteTransition.animateColor(
        dgenRed, dgenGreen,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
    ).value
    AnimatedVisibility(
        visible = status != null,
        enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = mediumEnterDuration))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .size(280.dp)
                        .aspectRatio(1f),
                    imageLoader = gifLoader,
                    colorFilter = ColorFilter.tint(changeColor),
                    model = R.drawable.globe_wireframe,
                    contentDescription = "Status Animation"
                )

                val text = when (status) {
                    TransactionStatus.PENDING -> "Transaction Pending..."
                    TransactionStatus.SUCCESS -> "Transaction Confirmed!"
                    TransactionStatus.FAILURE -> "Transaction Failed"
                    null -> "" 
                }

                Text(
                    text = text,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenGunMetal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.offset(y=-48.dp).padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// Previews for different states
@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 420, heightDp = 420)
@Composable
fun TransactionStatusOverlayPreviewPending() {
    // For preview, we pass a dummy ImageLoader or use a placeholder directly
    // Here, we'll directly use an Image composable for simplicity in preview
    val context = LocalContext.current
    var status: TransactionStatus? = TransactionStatus.PENDING

    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    TransactionStatusOverlay(
        status = status,
        gifLoader = gifEnabledLoader,
        onDismiss = { status = null }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 720, heightDp = 720)
@Composable
fun TransactionStatusOverlayPreviewSuccess() {
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
                painter = painterResource(id = R.drawable.globe_wireframe),
                contentDescription = "Status Animation",
                modifier = Modifier
                    .size(480.dp)
                    .aspectRatio(1f)
            )
            Text(
                text = "Transaction Confirmed!",
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenGunMetal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.offset(y=-48.dp).padding(horizontal = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 420, heightDp = 420)
@Composable
fun TransactionStatusOverlayPreviewFailure() {
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
                painter = painterResource(id = R.drawable.globe_wireframe),
                contentDescription = "Status Animation",
                modifier = Modifier
                    .size(120.dp)
                    .aspectRatio(1f)
            )
            Text(
                text = "Transaction Failed".uppercase(),
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