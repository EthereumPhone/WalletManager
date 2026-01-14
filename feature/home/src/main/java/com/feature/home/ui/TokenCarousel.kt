package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.zIndex
import com.core.ui.Card
import com.core.ui.views.IdleView
import com.core.ui.util.largeEnterDuration
import com.core.ui.util.smallDuration
import kotlin.math.abs
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.core.model.TokenGroupAssetOverview
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGreen
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@SuppressLint("RestrictedApi")
@Composable
fun TokenCardCarousel(
    assets: List<TokenGroupAssetOverview>,
    navigateToSend: (groupId: String) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier,
    hasNfts: Boolean = true,
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
            Log.d("ScrollSave", "User scroll FINISHED. Saved: index=${savedScrollIndex}, offset=${savedScrollOffset}")
            isUserScrolling = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var scrollJob by remember { mutableStateOf<Job?>(null) }


    LaunchedEffect(assets) {
        Log.d("TokenCardCarousel", "Effect: Assets/SelectedToken changed. Assets: ${assets.size}, autoScrollDone: $autoScrollDone")
        if (assets.isNotEmpty()) {
            if (!autoScrollDone) {
                autoScrollDone = true
                Log.d("TokenCardCarousel", "Attempting auto-scroll, autoScrollDone set to true immediately.")
                scrollJob?.cancel()
                isUserScrolling = false
                val targetToken = assets.lastOrNull()

                targetToken?.let { token ->
                    val targetIndex = assets.indexOf(token)
                    if (targetIndex != -1) {
                        Log.d("TokenCardCarousel", "Auto-scrolling to ${token.symbol} at index $targetIndex")
                        scrollJob = coroutineScope.launch {
                            listState.scrollToItem(targetIndex)
                            savedScrollIndex = targetIndex
                            savedScrollOffset = 0
                            Log.d("ScrollSave", "Auto-scroll COMPLETED & SAVED. New saved: index=$savedScrollIndex, offset=$savedScrollOffset.")
                        }
                    } else {
                        Log.d("TokenCardCarousel", "Auto-scroll target token not found in assets.")
                    }
                } ?: run {
                    Log.d("TokenCardCarousel", "No target token for auto-scroll.")
                }
            }
        }
    }

    val cardHeight  = 400.dp
    val visibleCount= 5
    val overlap     = (-cardHeight / visibleCount) *4     // -50.dp
    val clampRange  = (visibleCount - 1).toFloat()     // 3f

    val sensitivity = 0.2f

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag) {
                    if (!isUserScrolling) {
                        Log.d("ScrollState", "User scroll STARTED via Drag")
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

    val topPadding = if (hasNfts) 32.dp else 64.dp
    val baseBottomPadding = 130.dp
    val itemSpacing = overlap - 32.dp
    // Extra space so the last card can fully settle without lifting the stack too high.
    val extraScrollMargin = cardHeight * 0.5f

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val containerHeight = this.maxHeight
        
        // Responsive frontCardTranslation based on screen height
        // Small screens (~600dp): 900f, Medium (~800dp): 1200f, Large (~900dp+): 1500f
        val maxTranslation = (containerHeight.value * 1.5f).coerceIn(900f, 1600f)
        
        val baseContentHeight = (cardHeight * assets.size) +
                (itemSpacing * (assets.size - 1).coerceAtLeast(0)) +
                topPadding

        // Guarantee enough scrollable area so back cards are reachable on tall screens.
        val bottomPadding = maxOf(
            baseBottomPadding,
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
            itemsIndexed(assets, key = { _, asset -> asset.groupId}) { index, item ->
                val rotX: Float by animateFloatAsState ( -25f , label = "rotX")

                val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
                val isFirstCard = index == firstVisibleIndex

                LaunchedEffect(isFirstCard, item.groupId, listState.isScrollInProgress) {
                    if (isFirstCard && !listState.isScrollInProgress && !isUserScrolling) {
                        Log.d("FirstCard", "Card $index (${item.symbol}) is first & settled. setSelectedToken.")
                    }
                }

                val firstVisibleOffset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
                val scrollOffset = firstVisibleIndex + firstVisibleOffset / 1000f
                val relIdx = (index - scrollOffset).coerceIn(-clampRange, clampRange)

                val scale by animateFloatAsState(
                    targetValue = when {
                        abs(relIdx) <= 0.5f       -> 0.8f
                        abs(relIdx) <= clampRange -> lerp(0.8f, 0.55f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                        else                      -> 0.55f
                    },
                    animationSpec = tween(smallDuration, easing = FastOutSlowInEasing), label = "scaleAnimation"
                )

                val alphafactor by animateFloatAsState(
                    targetValue = when {
                        abs(relIdx) <= 0.5f       -> 1f
                        abs(relIdx) <= clampRange -> lerp(1f, 0f, (abs(relIdx)-0.5f)/(clampRange-0.5f))
                        else                      -> 0f
                    },
                    animationSpec = tween(smallDuration, easing = FastOutSlowInEasing), label = "alphaAnimation"
                )

                val frontCardTranslation by animateFloatAsState(
                    targetValue = lerp(0f, maxTranslation, (relIdx / 2).coerceIn(0f, 1f)),
                    animationSpec = tween(durationMillis = largeEnterDuration, easing = FastOutSlowInEasing), label = "translationAnimation"
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
                            IdleView(
                                amount = item.totalBalance,
                                tokenName = item.symbol,
                                fiatAmount = item.totalFiatBalance ?: 0.0,
                                icon = if(item.logoUrl != null && item.logoUrl != "") item.logoUrl else "",
                                navigateToSend = { navigateToSend(item.groupId) },
                                enableSend = item.totalBalance > 0,
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
