package com.feature.swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    modifier: Modifier = Modifier,
    switchTokens: () -> Unit,
    onPickAssetClicked: (TextFieldSelected) -> Unit,
    onAmountChange: (TextFieldSelected, String) -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WmTextField(
            value = amountsUiState.fromAmount,
            onChange = { onAmountChange(TextFieldSelected.FROM, it) },
            modifier = Modifier.fillMaxWidth(),
            placeHolder = {
                Text(
                    text = "Amount",
                    color = Color(0xFF9FA2A5)
                )
            },
            trailingIcon = {
                TokenAssetIcon(assetsUiState.fromAsset) {
                    onPickAssetClicked(TextFieldSelected.FROM)
                }
            }
        )

        IconButton(
            onClick = { switchTokens() },
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
            modifier = Modifier.fillMaxWidth(),

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
    var clicked by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf(AmountsUiState("0.123", "0.234")) }

    Column {
        TokenSelector(
            amountsUiState = amount,
            assetsUiState = AssetsUiState(SelectedTokenUiState.Unselected, SelectedTokenUiState.Unselected),
            modifier = Modifier.fillMaxWidth(),
            {},
            {textFieldSelected: TextFieldSelected -> clicked += 1 },
            {textFieldSelected: TextFieldSelected, text: String ->
                when(textFieldSelected) {
                    TextFieldSelected.FROM -> {
                        amount = amount.copy(
                            fromAmount = text
                        )
                    }
                    TextFieldSelected.TO -> {
                        amount = amount.copy(
                            toAmount = text
                        )
                    }
                }

            }
        )
        Text(text = "Test TEXT $clicked")
    }


}