package com.feature.swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.core.ui.WmTextField
import com.feature.swap.AmountsUiState
import com.feature.swap.AssetsUiState
import com.feature.swap.SelectedTokenUiState
import com.feature.swap.SwapTokenUiState
import com.feature.swap.TextFieldSelected

@Composable
fun TokenSelector(
    amountsUiState: AmountsUiState,
    assetsUiState: AssetsUiState,
    switchTokens: () -> Unit,
    onPickAssetClicked: (TextFieldSelected) -> Unit,
    onAmountChange: (TextFieldSelected, String) -> Unit
) {
    Column {
        WmTextField(
            value = amountsUiState.fromAmount,
            onChange = { onAmountChange(TextFieldSelected.FROM, it) },
            trailingIcon = {
                TokenAssetIcon(assetsUiState.fromAsset) {
                    onPickAssetClicked(TextFieldSelected.FROM)
                }
            }
        )

        IconButton(
            onClick = {
                switchTokens()
            },
            content = {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    tint = Color.White,
                    contentDescription = "Swap Tokens"
                )
            }
        )

        WmTextField(
            value = amountsUiState.toAmount,
            onChange = { onAmountChange(TextFieldSelected.TO, it) },
            trailingIcon = {
                TokenAssetIcon(assetsUiState.toAsset) {
                    onPickAssetClicked(TextFieldSelected.TO)
                }
            }
        )
    }
}

@Composable
private fun TokenAssetIcon(
    tokenAsset: SelectedTokenUiState,
    onClick: () -> Unit

) {
    val text = when(tokenAsset) {
        is SelectedTokenUiState.Unselected -> { "Select Token" }
        is SelectedTokenUiState.Selected -> { tokenAsset.tokenAsset.symbol }
    }
    Button(
        onClick = onClick,
        content = {
            Text(text)
        }
    )
}










@Preview
@Composable
fun PreviewTokenSelector() {

}