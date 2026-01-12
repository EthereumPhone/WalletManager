package com.feature.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.core.model.NFT
import com.core.model.TokenGroupAssetOverview
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import kotlinx.coroutines.launch

/**
 * Horizontal pager that switches between Token and NFT carousels
 */
@Composable
fun AssetPager(
    tokens: List<TokenGroupAssetOverview>,
    nfts: List<NFT>,
    navigateToSend: (groupId: String) -> Unit,
    navigateToSendNft: (contractAddress: String, tokenId: String, chainId: Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (nfts.isNotEmpty()) 2 else 1 }
    )
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        // Page indicator tabs (only show if NFTs exist)
        if (nfts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .background(dgenBlack)
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                PageTab(
                    text = "TOKENS",
                    isSelected = pagerState.currentPage == 0,
                    primaryColor = primaryColor,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.3f))
                )
                
                PageTab(
                    text = "NFTs",
                    isSelected = pagerState.currentPage == 1,
                    primaryColor = primaryColor,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> {
                    // Token Carousel
                    Box(
                        modifier = Modifier
                        .fillMaxSize()
                    ) {
                        TokenCardCarousel(
                            assets = tokens,
                            navigateToSend = navigateToSend,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()

                                .height(48.dp) // Adjust thickness of fading border
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(dgenBlack, Color.Transparent)
                                    )
                                )


                        )
                    }
                }
                1 -> {
                    // NFT Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        NftCardCarousel(
                            nfts = nfts,
                            navigateToSendNft = navigateToSendNft,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp) // Adjust thickness of fading border
                                .align(Alignment.TopCenter)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(dgenBlack, Color.Transparent)
                                    )
                                )

                        )
                    }
                }
            }
        }

        // Page dots indicator
        if (nfts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    val color by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.3f),
                        animationSpec = tween(200),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun PageTab(
    text: String,
    isSelected: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) primaryColor else primaryColor.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "tabTextColor"
    )
    
    Text(
        text = text,
        style = TextStyle(
            fontFamily = SpaceMono,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
