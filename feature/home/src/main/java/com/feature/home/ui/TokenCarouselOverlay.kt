package com.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.core.model.TokenGroupAssetOverview
import com.core.ui.HeaderBar
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGreen
import com.core.ui.util.dgenRed

/**
 * Full-screen overlay that displays the TokenCarousel with all user's wallet tokens.
 * 
 * @param isVisible Controls the visibility of the overlay
 * @param assets List of token assets to display in the carousel
 * @param navigateToSend Callback for navigation to send screen
 * @param primaryColor Primary theme color
 * @param secondaryColor Secondary theme color
 * @param onDismiss Callback when overlay should be dismissed
 */
@Composable
fun TokenCarouselOverlay(
    isVisible: Boolean,
    assets: List<TokenGroupAssetOverview>,
    navigateToSend: (groupId: String) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).background(dgenBlack)
                ) {
                    // Header with close button
                    HeaderBar(
                        modifier = Modifier,
                        text = "SELECT TOKEN",
                        onClick = onDismiss,
                        primaryColor = primaryColor
                    )
                    // Token Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        if (assets.isNotEmpty()) {
                            TokenCardCarousel(
                                assets = assets,
                                navigateToSend = { groupId ->
                                    navigateToSend(groupId)
                                    onDismiss() // Close overlay after navigation
                                },
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                modifier = Modifier.fillMaxSize().offset(y=-8.dp)
                            )
                        }
                        else {
                            // Empty state
                            EmptyTokensState(
                                primaryColor = primaryColor,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Top gradient fade
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .align(Alignment.TopCenter)
                                .offset(y=0.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(dgenBlack,dgenBlack, Color.Transparent)
                                    )
                                ).zIndex(3f)
                        )
                        
                        // Bottom gradient fade
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, dgenBlack)
                                    )
                                )
                                .zIndex(3f)
                        )
                    }
                }
        }
    }
}

/**
 * Empty state component when no tokens are available.
 */
@Composable
private fun EmptyTokensState(
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state icon placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💰",
                fontSize = 32.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Tokens Found",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your wallet doesn't contain any tokens yet.\nAdd some tokens to get started!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
