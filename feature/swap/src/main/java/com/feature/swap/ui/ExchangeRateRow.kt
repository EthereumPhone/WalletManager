package com.feature.swap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.feature.swap.AmountsUiState
import com.feature.swap.AssetsUiState
import com.feature.swap.SelectedTokenUiState
import androidx.compose.foundation.background
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onGloballyPositioned


@Composable
fun ExchangeRateRow(
    assetsUiState: AssetsUiState,
    exchangeUiState: Double,
    isSyncing: Boolean
) {

    //Opacity Animation
    val transition = rememberInfiniteTransition()
    val fadingAnimation by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                1.0f at  0 with LinearEasing
                0f at  1000 with LinearEasing
                1.0f at  2000 with LinearEasing
            }
        )
    )

   if(assetsUiState.fromAsset is SelectedTokenUiState.Selected && assetsUiState.toAsset is SelectedTokenUiState.Selected) {
      if (isSyncing) {
          Row(
              verticalAlignment = Alignment.CenterVertically
          ) {
              CircularProgressIndicator(
                  color = Color.White,
                  modifier = Modifier.size(20.dp)
              )
              Text(
                  modifier = Modifier.alpha(fadingAnimation),
                  text = " Fetching exchange rate...",
                  color = Color(0xFF9FA2A5),
                  fontSize = 16.sp
              )
          }
      } else {
          Text(
              text = "1 ${assetsUiState.fromAsset.tokenAsset.symbol} = $exchangeUiState ${assetsUiState.toAsset.tokenAsset.symbol}",
              color = Color(0xFF9FA2A5),
              fontSize = 16.sp
          )
      }
   }
}


@Preview
@Composable
fun previewExchangeRateRow() {
    val assetState = AssetsUiState(
        SelectedTokenUiState.Selected(
            TokenAsset(
                "123",
                1,
                "ABC",
                "ABC",
                0.1,
            )
        ),
        SelectedTokenUiState.Selected(
            TokenAsset(
                "123",
                1,
                "ABC",
                "ABC",
                0.1,
            )
        )
    )

    ExchangeRateRow(
        assetState,
        0.0,
        true
    )
}