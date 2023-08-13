package com.feature.swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.core.ui.WmTextField
import com.feature.swap.SelectedTokenUiState
import com.feature.swap.SwapTokenUiState

@Composable
fun TokenSelector(
    fromAmount: Double,
    fromTokenAsset: SelectedTokenUiState,
    toAmount: Double,
    toTokenAsset: SelectedTokenUiState,
    switchTokens: () -> Unit,
    onValueChange: (TextFieldSelector) -> Unit
) {
    Column {
        WmTextField(
            value = fromAmount.toString(),
            onChange = { onValueChange(TextFieldSelector.FIRST) },
            leadingIcon = {
                TokenAssetIcon(fromTokenAsset)
            }

        )


        WmTextField(
            value = toAmount.toString(),
            onChange = { onValueChange(TextFieldSelector.SECOND) },
            leadingIcon = {
                TokenAssetIcon(toTokenAsset)
            }
        )
    }
}

@Composable
private fun TokenAssetIcon(tokenAsset: SelectedTokenUiState) {
    val text = when(tokenAsset) {
        is SelectedTokenUiState.Unselected -> { "Select Token" }
        is SelectedTokenUiState.Selected -> { tokenAsset.tokenAsset.symbol }
    }

    Card {
        Text(text)
    }

}


enum class TextFieldSelector {
    FIRST, SECOND
}







@Preview
@Composable
fun PreviewTokenSelector() {

}