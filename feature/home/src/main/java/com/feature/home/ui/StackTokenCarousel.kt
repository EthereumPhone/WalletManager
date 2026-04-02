package com.feature.home.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.core.model.NFT
import com.core.model.TokenGroupAssetOverview
import com.core.ui.Card
import com.core.ui.util.dgenBlack
import com.core.ui.views.IdleView
import com.core.ui.views.NftCardView
import kotlinx.coroutines.launch
import kotlin.math.exp

// ── Tuning constants ──────────────────────────────────────────────────
private const val FRONT_SCALE = 0.82f
private const val SCALE_PER_CARD = 0.03f
private const val EDGE_OFFSET_PX = 36f
private const val FADE_RATE = 0.5f
private const val BASE_TILT = 5f
private const val TILT_PER_CARD = 1.5f
private const val MAX_STACK_BEHIND = 4
private const val CAMERA_DIST_FACTOR = 32f

// ─────────────────────────────────────────────────────────────────────
// Generic stack carousel
// ─────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> StackCardCarousel(
    items: List<T>,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    onLongPress: (T) -> Unit = {},
    onClearSelection: () -> Unit = {},
    cardContent: @Composable (item: T, isFrontCard: Boolean) -> Unit,
) {
    val state = rememberStackCarouselState(initialPosition = 0f)
    state.itemCount = items.size

    val coroutineScope = rememberCoroutineScope()

    if (items.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        val pxPerCard = containerHeightPx * 0.7f

        val draggableState = rememberDraggableState { delta ->
            if (!isSelectionMode) {
                val newPos = (state.scrollPosition + delta / pxPerCard)
                    .coerceIn(0f, items.lastIndex.toFloat())
                coroutineScope.launch { state.snapTo(newPos) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    enabled = !isSelectionMode,
                    onDragStopped = { velocity ->
                        val scrollVelocity = velocity / pxPerCard
                        coroutineScope.launch {
                            state.fling(scrollVelocity)
                        }
                    }
                )
        ) {
            StackLayout(
                items = items,
                scrollPosition = state.scrollPosition,
                containerHeightPx = containerHeightPx,
                isSelectionMode = isSelectionMode,
                onLongPress = onLongPress,
                onClearSelection = onClearSelection,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                cardContent = cardContent,
            )
        }

        // Top gradient fade
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .align(Alignment.TopCenter)
                .zIndex(10f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(dgenBlack, Color.Transparent)
                    )
                )
        )

        // Scrollbar
        CarouselScrollbar(
            itemCount = items.size,
            currentPosition = state.scrollPosition,
            onScrollTo = { position ->
                coroutineScope.launch { state.snapTo(position) }
            },
            primaryColor = primaryColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.6f)
                .zIndex(11f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> StackLayout(
    items: List<T>,
    scrollPosition: Float,
    containerHeightPx: Float,
    isSelectionMode: Boolean,
    onLongPress: (T) -> Unit,
    onClearSelection: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    cardContent: @Composable (item: T, isFrontCard: Boolean) -> Unit,
) {
    val currentIdx = scrollPosition.toInt()
    val fractional = scrollPosition - currentIdx

    val firstCard = currentIdx
    val lastCard = (currentIdx + MAX_STACK_BEHIND).coerceAtMost(items.lastIndex)
    val cardsToRender = (lastCard downTo firstCard).toList()

    Layout(
        content = {
            for (index in cardsToRender) {
                val item = items[index]
                val depth = index - currentIdx
                val effectiveDepth = (depth - fractional).coerceAtLeast(0f)

                val scale = (FRONT_SCALE - SCALE_PER_CARD * effectiveDepth).coerceAtLeast(0.4f)
                val alpha = if (depth == 0) {
                    (1f - fractional * 0.3f).coerceIn(0f, 1f)
                } else {
                    exp(-FADE_RATE * effectiveDepth).coerceIn(0f, 1f)
                }
                val translationY = if (depth == 0) {
                    fractional * containerHeightPx
                } else {
                    -EDGE_OFFSET_PX * effectiveDepth
                }
                val rotationX = -(BASE_TILT + TILT_PER_CARD * effectiveDepth)
                val zIdx = (MAX_STACK_BEHIND + 1f) - effectiveDepth
                val isFrontCard = depth == 0 && fractional < 0.3f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(zIdx)
                        .combinedClickable(
                            onClick = {
                                if (isSelectionMode && isFrontCard) onClearSelection()
                            },
                            onLongClick = {
                                if (!isSelectionMode && isFrontCard) onLongPress(item)
                            }
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                            this.rotationX = rotationX
                            this.translationY = translationY
                            cameraDistance = CAMERA_DIST_FACTOR * this.density
                        }
                ) {
                    Card(
                        isFirst = isFrontCard,
                        modifier = Modifier.fillMaxWidth(),
                        frontSide = { cardContent(item, isFrontCard) },
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { measurables, constraints ->
        val cardWidth = constraints.maxWidth
        val cardHeight = (cardWidth * 9f / 16f).toInt()
        val cardConstraints = Constraints.fixed(cardWidth, cardHeight)
        val placeables = measurables.map { it.measure(cardConstraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            val centerY = (constraints.maxHeight - cardHeight) / 2
            placeables.forEach { it.place(0, centerY) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────
// Token carousel
// ─────────────────────────────────────────────────────────────────────

@SuppressLint("RestrictedApi")
@Composable
fun StackTokenCarousel(
    assets: List<TokenGroupAssetOverview>,
    navigateToSend: (groupId: String) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    hasNfts: Boolean = true,
    onLongPressToken: (TokenGroupAssetOverview) -> Unit = {},
    isSelectionMode: Boolean = false,
    onClearSelection: () -> Unit = {},
) {
    StackCardCarousel(
        items = assets,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        modifier = modifier,
        isSelectionMode = isSelectionMode,
        onLongPress = onLongPressToken,
        onClearSelection = onClearSelection,
    ) { item, isFrontCard ->
        IdleView(
            amount = item.totalBalance,
            tokenName = item.symbol,
            fiatAmount = item.totalFiatBalance ?: 0.0,
            icon = if (!item.logoUrl.isNullOrEmpty()) item.logoUrl else "",
            navigateToSend = {
                if (isFrontCard) navigateToSend(item.groupId)
            },
            enableSend = item.totalBalance > 0 && isFrontCard,
            primaryColor = primaryColor,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────
// NFT carousel
// ─────────────────────────────────────────────────────────────────────

@Composable
fun StackNftCarousel(
    nfts: List<NFT>,
    navigateToSendNft: (contractAddress: String, tokenId: String, chainId: Int) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
) {
    StackCardCarousel(
        items = nfts,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        modifier = modifier,
    ) { nft, isFrontCard ->
        NftCardView(
            nftName = nft.name,
            collectionName = nft.collectionName,
            imageUrl = nft.imageUrl ?: nft.thumbnailUrl,
            floorPriceEth = nft.floorPriceEth,
            floorPriceUsd = nft.floorPriceUsd,
            navigateToSendNft = {
                if (isFrontCard) navigateToSendNft(nft.contractAddress, nft.tokenId, nft.chainId)
            },
            primaryColor = primaryColor,
        )
    }
}
