package com.feature.swap.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.feature.swap.AmountsUiState
import com.feature.swap.AssetsUiState
import com.feature.swap.SelectedTokenUiState

@Composable
fun ExchangeRateRow(
    assetsUiState: AssetsUiState,
    exchangeUiState: Double,
    isSyncing: Boolean
) {
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
                  text = " Fetching exchange rate",
                  color = Color(0xFF9FA2A5),
                  fontSize = 16.sp
              )
          }
      } else {
          Text(
              text = "1 ${assetsUiState.fromAsset.tokenAsset.symbol} = $exchangeUiState ${assetsUiState.toAsset.tokenAsset.symbol}",
              color = Color.White,
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