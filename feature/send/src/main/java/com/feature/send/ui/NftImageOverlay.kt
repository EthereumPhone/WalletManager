package com.feature.send.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.mediumEnterDuration

/**
 * NFT Image Overlay - Full screen viewer with zoom and pan capabilities
 */
@Composable
fun NftImageOverlay(
    visible: Boolean,
    imageUrl: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = mediumEnterDuration)),
        exit = fadeOut(animationSpec = tween(durationMillis = mediumEnterDuration))
    ) {
        // Zoom and pan state
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        
        val minScale = 0.5f
        val maxScale = 5f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dgenBlack)
                .statusBarsPadding()
        ) {
            // Header - simplified version without right-side text
            NftOverlayHeader(
                onCloseClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp)
            )

            // Zoomable/Pannable Image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Update scale with limits
                            val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                            scale = newScale
                            
                            // Update offset for panning
                            offset = Offset(
                                x = offset.x + pan.x,
                                y = offset.y + pan.y
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Double tap to reset or zoom in
                                if (scale > 1f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.5f
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = gifEnabledLoader,
                    contentDescription = "NFT Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }

            // Zoom Controls
            ZoomControls(
                onZoomIn = {
                    scale = (scale * 1.5f).coerceAtMost(maxScale)
                },
                onZoomOut = {
                    scale = (scale / 1.5f).coerceAtLeast(minScale)
                },
                onReset = {
                    scale = 1f
                    offset = Offset.Zero
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }
    }
}

/**
 * Simplified header for the NFT overlay - just "VIEW" text and close button
 */
@Composable
fun NftOverlayHeader(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    var enabled by remember { mutableStateOf(true) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Text(
            text = "VIEW",
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )

        // Close icon
        IconButton(
            modifier = Modifier.size(56.dp),
            onClick = {
                if (!enabled) return@IconButton
                enabled = false
                onCloseClick()
            }
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.CenterEnd),
                    painter = painterResource(com.core.ui.R.drawable.baseline_close_24),
                    contentDescription = "Close",
                    tint = primaryColor
                )
            }
        }
    }
}

/**
 * Zoom control buttons
 */
@Composable
fun ZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = SystemColorManager.primaryColor

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Zoom Out
        IconButton(
            onClick = onZoomOut,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.15f))
        ) {
            Text(
                text = "−",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }

        // Reset
        IconButton(
            onClick = onReset,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.15f))
        ) {
            Text(
                text = "⊙",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }

        // Zoom In
        IconButton(
            onClick = onZoomIn,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.15f))
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewNftImageOverlay() {
    NftImageOverlay(
        visible = true,
        imageUrl = null,
        onDismiss = {}
    )
}
