package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import com.core.model.NFT
import com.core.ui.Card
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGreen
import com.core.ui.views.NftCardView
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.smallDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * NFT Card Carousel - displays NFTs in a stacked card carousel
 * Similar to TokenCardCarousel but for NFTs
 */
@SuppressLint("RestrictedApi")
@Composable
fun NftCardCarousel(
    nfts: List<NFT>,
    navigateToSendNft: (contractAddress: String, tokenId: String, chainId: Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    var savedScrollIndex by rememberSaveable { mutableStateOf(0) }
    var savedScrollOffset by rememberSaveable { mutableStateOf(0) }
    var autoScrollDone by rememberSaveable { mutableStateOf(false) }
    var isUserScrolling by remember { mutableStateOf(false) }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedScrollIndex,
        initialFirstVisibleItemScrollOffset = savedScrollOffset
    )

    LaunchedEffect(listState.isScrollInProgress, isUserScrolling) {
        if (!listState.isScrollInProgress && isUserScrolling) {
            savedScrollIndex = listState.firstVisibleItemIndex
            savedScrollOffset = listState.firstVisibleItemScrollOffset
            Log.d("NftScrollSave", "User scroll FINISHED. Saved: index=$savedScrollIndex, offset=$savedScrollOffset")
            isUserScrolling = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var scrollJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(nfts) {
        Log.d("NftCardCarousel", "Effect: NFTs changed. Count: ${nfts.size}, autoScrollDone: $autoScrollDone")
        if (nfts.isNotEmpty()) {
            if (!autoScrollDone) {
                autoScrollDone = true
                scrollJob?.cancel()
                isUserScrolling = false
                val targetIndex = nfts.lastIndex
                if (targetIndex >= 0) {
                    Log.d("NftCardCarousel", "Auto-scrolling to index $targetIndex")
                    scrollJob = coroutineScope.launch {
                        listState.scrollToItem(targetIndex)
                        savedScrollIndex = targetIndex
                        savedScrollOffset = 0
                    }
                }
            }
        }
    }

    val cardHeight = 400.dp
    val visibleCount = 5
    val overlap = (-cardHeight / visibleCount) * 4
    val clampRange = (visibleCount - 1).toFloat()
    val sensitivity = 0.2f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag) {
                    if (!isUserScrolling) {
                        Log.d("NftScrollState", "User scroll STARTED via Drag")
                        isUserScrolling = true
                        scrollJob?.cancel()
                    }
                }
                val scaledY = available.y * sensitivity
                if (isUserScrolling) {
                    coroutineScope.launch {
                        listState.scrollBy(scaledY)
                    }
                    return Offset(x = 0f, y = scaledY)
                }
                return Offset.Zero
            }
        }
    }

    val itemSpacing = overlap - 32.dp
    val extraScrollMargin = cardHeight * 0.5f

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val containerHeight = this.maxHeight
        val containerWidth = this.maxWidth
        
        // Determine if device is square-ish (aspect ratio close to 1:1)
        val aspectRatio = containerWidth / containerHeight
        val isSquareDevice = aspectRatio in 0.8f..1.25f
        
        // Use smaller dimension for calculations on square devices
        val referenceDimension = if (isSquareDevice) {
            minOf(containerHeight.value, containerWidth.value)
        } else {
            containerHeight.value
        }
        
        // Responsive padding based on display size
        // Smaller screens get less padding, larger screens get more
        val paddingScale = (referenceDimension / 480f).coerceIn(0.6f, 1.5f)
        val baseTopPadding = (32 * paddingScale).dp
        val baseBottomPadding = (120 * paddingScale).dp

        // Keep the "front" card more fixed around the vertical center by anchoring the list's
        // first visible item at roughly mid-screen.
        val centeredPadding = ((containerHeight - cardHeight) / 2).coerceAtLeast(0.dp)
        val topPadding = maxOf(baseTopPadding, centeredPadding)
        // Ensure the last card can also settle into the same centered position.
        val minBottomPadding = maxOf(baseBottomPadding, topPadding)
        
        // Responsive frontCardTranslation based on screen size and aspect ratio
        // Square devices need less translation to prevent cards from going off-screen
        // Larger screens need proportionally larger translation values
        val translationMultiplier = if (isSquareDevice) 1.2f else 1.5f
        val maxTranslation = (referenceDimension * translationMultiplier).coerceIn(600f, 1600f)

        val baseContentHeight = (cardHeight * nfts.size) +
                (itemSpacing * (nfts.size - 1).coerceAtLeast(0)) +
                topPadding

        val bottomPadding = maxOf(
            minBottomPadding,
            (containerHeight + extraScrollMargin - baseContentHeight)
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .zIndex(3f),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            contentPadding = PaddingValues(top = topPadding, bottom = bottomPadding)
        )
        {
            itemsIndexed(nfts, key = { _, nft -> "${nft.contractAddress}_${nft.tokenId}_${nft.chainId}" }) { index, nft ->
                val rotX: Float by animateFloatAsState(-25f, label = "rotX")

                val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                val isFirstCard = index == firstVisibleIndex

                val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
                val scrollOffset = firstVisibleIndex + firstVisibleOffset / 1000f
                val relIdx = (index - scrollOffset).coerceIn(-clampRange, clampRange)

                val scale by animateFloatAsState(
                    targetValue = when {
                        abs(relIdx) <= 0.5f -> 0.8f
                        abs(relIdx) <= clampRange -> lerp(0.8f, 0.55f, (abs(relIdx) - 0.5f) / (clampRange - 0.5f))
                        else -> 0.55f
                    },
                    animationSpec = tween(smallDuration, easing = FastOutSlowInEasing),
                    label = "scaleAnimation"
                )

                val alphafactor by animateFloatAsState(
                    targetValue = when {
                        abs(relIdx) <= 0.5f -> 1f
                        abs(relIdx) <= clampRange -> lerp(1f, 0f, (abs(relIdx) - 0.5f) / (clampRange - 0.5f))
                        else -> 0f
                    },
                    animationSpec = tween(smallDuration, easing = FastOutSlowInEasing),
                    label = "alphaAnimation"
                )

                val frontCardTranslation by animateFloatAsState(
                    targetValue = lerp(0f, maxTranslation, (relIdx / 2).coerceIn(0f, 1f)),
                    animationSpec = tween(durationMillis = largeEnterDuration, easing = FastOutSlowInEasing),
                    label = "translationAnimation"
                )

                Card(
                    isFirst = isFirstCard,
                    modifier = Modifier
                        .height(cardHeight)
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = alphafactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                            cameraDistance = 32f * density
                        },
                    frontSide = {
                        NftCardView(
                            nftName = nft.name,
                            collectionName = nft.collectionName,
                            imageUrl = nft.imageUrl ?: nft.thumbnailUrl,
                            floorPriceEth = nft.floorPriceEth,
                            floorPriceUsd = nft.floorPriceUsd,
                            navigateToSendNft = { 
                                navigateToSendNft(nft.contractAddress, nft.tokenId, nft.chainId) 
                            },
                            primaryColor = primaryColor,
                        )
                    },
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
            }
        }

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
