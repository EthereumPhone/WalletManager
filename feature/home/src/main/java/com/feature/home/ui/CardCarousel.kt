package com.feature.home.ui

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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

@SuppressLint("RestrictedApi")
@Composable
fun CardCarousel(
    assets: List<TokenAsset>,
    selectedTokenUiState: SelectedTokenUiState,
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
        verticalArrangement = Arrangement.spacedBy((-240).dp), // Overlapping effect
        contentPadding = PaddingValues(top = 60.dp, bottom =40.dp) // Ensures enough space for scrolling
    ) {
        itemsIndexed(assets) { index, item ->

            Log.d("TokenID", "$index - ${ item.name } - ${ item.address }")
            val scrollOffset = listState.firstVisibleItemIndex + listState.firstVisibleItemScrollOffset / 1000f
            val relativeIndex = index - scrollOffset

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
                targetValue = lerp(0f, 1000f, (relativeIndex).coerceIn(0f, 1f)),
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )


            // Detect the front card
            LaunchedEffect(scaleFactor) {
                if (scaleFactor >= 0.79f) { // Check if the card is closest to the target scale
                    setSelectedToken(item.address)
                    Log.d("SetToken", item.address)
                }
            }


            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                        alpha = alphaFactor
                        rotationX = -10f
                        translationY = frontCardTranslation
                    }
                ,
                frontSide = {
                    IdleView(
                        amount = item.balance,
                        tokenName = item.name,
                        fiatAmount = item.balance,
                        icon = item.logoUrl
                    )
                },
            )


        }
    }

}