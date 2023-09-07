package com.feature.swap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.core.ui.TextToggleButton
import com.core.ui.WmTextField
import com.core.ui.ethOSButton
import com.feature.swap.AmountsUiState
import com.feature.swap.AssetsUiState
import com.feature.swap.SelectedTokenUiState
import com.feature.swap.SwapTokenUiState
import com.feature.swap.TextFieldSelected
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun TokenSelector(
    amountsUiState: AmountsUiState,
    assetsUiState: AssetsUiState,
    modifier: Modifier = Modifier,
    switchTokens: () -> Unit,
    onPickAssetClicked: (TextFieldSelected) -> Unit,
    onAmountChange: (TextFieldSelected, String) -> Unit
) {

    val fromBalance = when(assetsUiState.fromAsset) {
        is SelectedTokenUiState.Unselected -> { "" }
        is SelectedTokenUiState.Selected -> {
            formatDouble(assetsUiState.fromAsset.tokenAsset.balance)
        }
    }
//    val fromSymbol = when(assetsUiState.fromAsset) {
//        is SelectedTokenUiState.Unselected -> { "" }
//        is SelectedTokenUiState.Selected -> { assetsUiState.fromAsset.tokenAsset.symbol  }
//    }

    val toBalance = when(assetsUiState.toAsset) {
        is SelectedTokenUiState.Unselected -> { "" }
        is SelectedTokenUiState.Selected -> {
            formatDouble(assetsUiState.toAsset.tokenAsset.balance)
        }
    }
//    val toSymbol = when(assetsUiState.toAsset) {
//        is SelectedTokenUiState.Unselected -> { "" }
//        is SelectedTokenUiState.Selected -> { assetsUiState.toAsset.tokenAsset.symbol  }
//    }
    //var fromValue by remember { mutableStateOf(amountsUiState.fromAmount) }
    val maxed2 = remember { mutableStateOf(false) }
    //var fromPrevAmount by remember { mutableStateOf(fromValue) }

    // Creating a values and variables to remember
    // focus requester, manager and state
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current


    var fromEnabled = when(assetsUiState.fromAsset) {
        is SelectedTokenUiState.Unselected -> { false }
        is SelectedTokenUiState.Selected -> { true }
    }

    var toEnabled = when(assetsUiState.toAsset) {
        is SelectedTokenUiState.Unselected -> { false }
        is SelectedTokenUiState.Selected -> { true }
    }




    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row (
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "From",
                    fontWeight= FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color= Color.White
                )
                if (fromBalance != "") {
                    Text(
                        modifier = Modifier.clickable {
                            onAmountChange(TextFieldSelected.FROM, fromBalance)
                        },
                        text = "Balance: $fromBalance",
                        fontSize = 16.sp,
                        color= Color(0xFF9FA2A5)
                    )
                }
            }
            SwapTextField(
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    TokenAssetIcon(assetsUiState.fromAsset) {
                        onPickAssetClicked(TextFieldSelected.FROM)
                        //fromEnabled = true
                    }
                },
                value = amountsUiState.fromAmount,
                onChange = { onAmountChange(TextFieldSelected.FROM, it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Amount",
                focusRequester = focusRequester,
                enabled = fromEnabled


                )
        }


        //Token Switch
        IconButton(
            colors = IconButtonDefaults.iconButtonColors(
                containerColor= Color.White,
                contentColor = Color(0xFF24303D),
            ),
            onClick = {
                switchTokens()
                focusManager.clearFocus()
                      },
            content = {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    tint = Color(0xFF24303D),
                    contentDescription = "Swap Tokens"
                )
            }
        )

        Column (
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){
            Row (
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "To",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color= Color.White
                )

                if (toBalance != "") {
                    Text(
                        text = "Balance: ${toBalance}",
                        fontSize = 16.sp,
                        color= Color(0xFF9FA2A5)
                    )
                }
            }
            SwapTextField(
                value = if(maxed2.value) "$toBalance" else amountsUiState.toAmount,
                onChange = {
                    onAmountChange(TextFieldSelected.TO, it)
                    focusManager.clearFocus()
                           },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    TokenAssetIcon(assetsUiState.toAsset) {
                        onPickAssetClicked(TextFieldSelected.TO)
                        //toEnabled = true
                    }
                },
                placeholder = "Amount",
                focusRequester = focusRequester,
                enabled = toEnabled
            )
        }

    }
}

@Composable
private fun TokenAssetIcon(
    tokenAsset: SelectedTokenUiState,
    onClick: () -> Unit

) {
    val text = when(tokenAsset) {
        is SelectedTokenUiState.Unselected -> { "Select Token" }
        is SelectedTokenUiState.Selected -> { tokenAsset.tokenAsset.symbol  }
    }
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1E2730),
            contentColor = Color.White
        ),
        shape = CircleShape,
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
                amount = when(textFieldSelected) {
                    TextFieldSelected.FROM -> {
                        amount.copy(
                            fromAmount = text
                        )
                    }

                    TextFieldSelected.TO -> {
                        amount.copy(
                            toAmount = text
                        )
                    }
                }

            }
        )
        Text(text = "Test TEXT $clicked")
    }


}