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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.core.model.TokenGroupAssetOverview
import com.core.ui.Card
import com.core.ui.util.dgenBlack
import com.core.ui.views.IdleView
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

@OptIn(ExperimentalFoundationApi::class)
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
    val state = rememberStackCarouselState(initialPosition = 0f)
    state.itemCount = assets.size

    val coroutineScope = rememberCoroutineScope()

    if (assets.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerHeightPx = with(LocalDensity.current) { maxHeight.toPx() }
        // Drag sensitivity: how many pixels to drag for one full card swipe
        // Higher value = less sensitive (need more finger travel per card)
        val pxPerCard = containerHeightPx * 0.7f

        val draggableState = rememberDraggableState { delta ->
            if (!isSelectionMode) {
                val newPos = (state.scrollPosition + delta / pxPerCard)
                    .coerceIn(0f, assets.lastIndex.toFloat())
                coroutineScope.launch { state.snapTo(newPos) }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        // Convert px/s velocity to scroll-position/s velocity
                        val scrollVelocity = velocity / pxPerCard
                        coroutineScope.launch {
                            state.fling(scrollVelocity)
                        }
                    }
                )
        ) {
            CardStackLayout(
                assets = assets,
                scrollPosition = state.scrollPosition,
                containerHeightPx = containerHeightPx,
                navigateToSend = navigateToSend,
                onLongPressToken = onLongPressToken,
                isSelectionMode = isSelectionMode,
                onClearSelection = onClearSelection,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
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

        // Scrollbar overlaid on the right
        CarouselScrollbar(
            itemCount = assets.size,
            currentPosition = state.scrollPosition,
            onScrollTo = { position ->
                coroutineScope.launch {
                    state.snapTo(position)
                }
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
@SuppressLint("RestrictedApi")
@Composable
private fun CardStackLayout(
    assets: List<TokenGroupAssetOverview>,
    scrollPosition: Float,
    containerHeightPx: Float,
    navigateToSend: (groupId: String) -> Unit,
    onLongPressToken: (TokenGroupAssetOverview) -> Unit,
    isSelectionMode: Boolean,
    onClearSelection: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
) {
    val currentIdx = scrollPosition.toInt()
    val fractional = scrollPosition - currentIdx  // 0..1 progress swiping current card away

    // Show current card + up to MAX_STACK_BEHIND cards after it in the list
    val firstCard = currentIdx
    val lastCard = (currentIdx + MAX_STACK_BEHIND).coerceAtMost(assets.lastIndex)

    // Render back-to-front so the front card draws on top
    val cardsToRender = (lastCard downTo firstCard).toList()

    Layout(
        content = {
            for (index in cardsToRender) {
                val item = assets[index]
                val depth = index - currentIdx  // 0 = front, 1+ = behind

                // Effective depth shifts as user scrolls between cards
                val effectiveDepth = (depth - fractional).coerceAtLeast(0f)

                // ── Scale ──
                val scale = (FRONT_SCALE - SCALE_PER_CARD * effectiveDepth).coerceAtLeast(0.4f)

                // ── Alpha ──
                val alpha = if (depth == 0) {
                    // Front card fades slightly as it swipes away
                    (1f - fractional * 0.3f).coerceIn(0f, 1f)
                } else {
                    exp(-FADE_RATE * effectiveDepth).coerceIn(0f, 1f)
                }

                // ── Translation Y ──
                val translationY = if (depth == 0) {
                    // Front card: swipe down as user scrolls (finger up → card down)
                    fractional * containerHeightPx
                } else {
                    // Stacked cards: peek upward behind the front card
                    -EDGE_OFFSET_PX * effectiveDepth
                }

                // ── Rotation X ──
                val rotationX = -(BASE_TILT + TILT_PER_CARD * effectiveDepth)

                // ── Z-index ──
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
                                if (!isSelectionMode && isFrontCard) onLongPressToken(item)
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
                        frontSide = {
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
                        },
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
            placeables.forEach { placeable ->
                placeable.place(0, centerY)
            }
        }
    }
}
