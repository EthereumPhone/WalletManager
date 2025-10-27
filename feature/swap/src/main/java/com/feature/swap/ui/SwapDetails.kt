package com.feature.swap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

//@Composable
//fun SwapDetails(
//    swapUiState: SwapUiState,
//    primaryColor: Color,
//    modifier: Modifier = Modifier
//) {
////    if (swapUiState.exchangeRate.isNotEmpty() || swapUiState.currentQuote != null || swapUiState.gasEstimation != null) {
//    Column(
//        modifier = Modifier.padding(vertical = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    )
//    {
//        // Exchange rate
//        if (swapUiState.exchangeRate.isNotEmpty()) {
//            swapUiState.fromToken?.token?.symbol?.let { fromSymbol ->
//                swapUiState.toToken?.token?.symbol?.let { toSymbol ->
//                    ExchangeRate(
//                        fromSymbol,
//                        toToken = toSymbol,
//                        toValue = swapUiState.exchangeRate,
//                        fiatAmount = swapUiState.ethPriceUsd,
//                    )
//                }
//            }
//        }
//        // Gas estimation removed - not needed for user experience
//    }
////    }
//}