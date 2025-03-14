package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.fontscaling.MathUtils.lerp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.core.model.TokenAsset
import com.core.ui.Card
import com.core.ui.views.IdleView
import com.feature.send.SelectedTokenUiState
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@SuppressLint("RestrictedApi")
@Composable
fun CardCarousel(
    assets: List<TokenAsset>,
    selectedTokenUiState: SelectedTokenUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    setSelectedToken: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = assets.lastIndex)




    // Ensure scrolling starts at the last item
    LaunchedEffect(Unit) {

        if (assets.isNotEmpty()){
            val token = when(selectedTokenUiState){
                is SelectedTokenUiState.Unselected -> {
                    assets.last()
                }

                is SelectedTokenUiState.Selected -> {
                    selectedTokenUiState.tokenAsset
                }
            }
            val index = assets.indexOf(token)
            listState.scrollToItem(index)
            //setSelectedToken(token)
        }
    }



    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .zIndex(3f),
        verticalArrangement = Arrangement.spacedBy((-225).dp), // Overlapping effect
        contentPadding = PaddingValues(top = 100.dp, bottom = 100.dp) // Ensures enough space for scrolling
    ) {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        
        itemsIndexed(assets) { index, item ->
            Log.d("SetToken", "${ item.address } - ${ item.symbol } - ${ item.name }")

            Log.d("SendID", "Token: $index - ${ item.name } - ${ item.address }")

            var enabled by remember { mutableStateOf(false) }

            val rotX: Float by animateFloatAsState(if (enabled) -0.5f else -7f, label = "alpha")



            val scrollOffset = listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 1000f
            val relativeIndex = (index - scrollOffset).coerceIn(-2f, 2f) // Keep relative index in a reasonable range


            // Animated values

            //val scaleFactor = lerp(0.9f, 0.5f, (abs(relativeIndex) / 5).coerceIn(0f, 1f))
            val scaleFactor by animateFloatAsState(
                targetValue = lerp(0.8f, 0.55f, (abs(relativeIndex) / 4).coerceIn(0f, 1f)),
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )

            val alphaFactor by animateFloatAsState(
                targetValue = lerp(1f, 0f, (abs(relativeIndex) / 4).coerceIn(0f, 1f)),
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )

            val frontCardTranslation by animateFloatAsState(
                targetValue = lerp(0f, 800f, (relativeIndex / 2).coerceIn(0f, 1f)), // Reduced translation range
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )


            // Detect the front card
            val isFirstCard = index == firstVisibleIndex

            LaunchedEffect(isFirstCard) {
                if (isFirstCard) {
                    Log.d("FirstCard", "Karte mit Index $index ist jetzt die erste sichtbare")
                    // Weitere Logik, z.B. setSelectedToken(item.address) usw.
                    enabled = true
                    setSelectedToken(item.address)
                } else {
                    enabled = false
                }
            }


            Log.d("CardAnimation", " Token Home:  ${ item.address }")

            Log.d("CardItem", "${ item.address }")

            with(sharedTransitionScope) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scaleFactor
                            scaleY = scaleFactor
                            alpha = alphaFactor
                            rotationX = rotX
                            translationY = frontCardTranslation
                        }
//                        .sharedBounds(
//                            rememberSharedContentState(key = "token-${item.address}"),
//                            animatedVisibilityScope = animatedContentScope,
//                            enter = expandIn(),
//                            exit  = shrinkOut(),
//                        )
                    ,
                    frontSide = {

                        val tokenName = if (item.name == item.symbol)  "ETH-${item.symbol}" else item.symbol
                        IdleView(
                            amount = item.balance,
                            tokenName = tokenName,
                            fiatAmount = item.balance,
                            icon = item.logoUrl
                        )
                    },
                )
            }



        }
    }

}