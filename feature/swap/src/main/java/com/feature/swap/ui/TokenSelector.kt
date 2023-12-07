package com.feature.swap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.draw.rotate
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
import com.core.model.TokenAsset
import com.core.ui.TextToggleButton
import com.core.ui.WmTextField
import com.core.ui.ethOSButton
import com.core.ui.ethOSTextField
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
    isSyncing: Boolean,
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

        else -> {""}
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

        else -> {""}
    }

    val maxed2 = remember { mutableStateOf(false) }
    //var fromPrevAmount by remember { mutableStateOf(fromValue) }

    // Creating a values and variables to remember
    // focus requester, manager and state





    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(42.dp)
    ) {
        Column(
            horizontalAlignment= Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val fromAmountTooHigh = amountsUiState.fromAmount.isNotBlank() &&
                    (assetsUiState.fromAsset is SelectedTokenUiState.Selected) &&
                    (assetsUiState.fromAsset.tokenAsset.balance < amountsUiState.fromAmount.toDouble())

            ethOSTextField(
                text = amountsUiState.fromAmount,
                label="0",
                size = 56,
                maxChar = 12,
                onTextChanged = {
                    val amountText = if (it == ".") "0$it" else it
                    onAmountChange(TextFieldSelected.FROM, amountText)
                },
                color = if(fromAmountTooHigh) Color(0xFFF1847E) else Color.White

            )
            when(assetsUiState.fromAsset) {
            is SelectedTokenUiState.Unselected -> { }
            is SelectedTokenUiState.Selected -> {
                formatDouble(assetsUiState.fromAsset.tokenAsset.balance)
                Text(text = "${formatDouble(assetsUiState.fromAsset.tokenAsset.balance)} available", fontSize = 16.sp,
                    color= Color(0xFF9FA2A5))
            }
        }



            //tokenasseticon
            TokenAssetIcon(assetsUiState.fromAsset) {
                onPickAssetClicked(TextFieldSelected.FROM)
                //fromEnabled = true
                if(TextFieldSelected.FROM.name == TextFieldSelected.TO.name){
                    switchTokens()
                }
            }
            //switchTokens()


        }



        Icon(imageVector = Icons.Rounded.ArrowDownward, contentDescription = "Swap Icon", tint=Color.White, modifier = Modifier.size(64.dp))

        val toAmountTooHigh = amountsUiState.toAmount.isNotBlank() &&
                (assetsUiState.toAsset is SelectedTokenUiState.Selected) &&
                (assetsUiState.toAsset.tokenAsset.balance < amountsUiState.toAmount.toDouble())

        Column (
            horizontalAlignment= Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ){

            ethOSTextField(
                text = if(maxed2.value) "$toBalance" else amountsUiState.toAmount,
                label="0",
                size = 56,
                maxChar = 12,
                onTextChanged = {
                    val amountText = if (it == ".") "0$it" else it
                    onAmountChange(TextFieldSelected.TO, amountText)
                },
                color = if(toAmountTooHigh) Color(0xFFF1847E) else Color.White
            )

            TokenAssetIcon(assetsUiState.toAsset) {
                onPickAssetClicked(TextFieldSelected.TO)

                if(TextFieldSelected.TO.name == TextFieldSelected.FROM.name){
                    switchTokens()
                }
                //toEnabled = true
            }

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
        else -> {""}
    }
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(start = 12.dp,end = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF262626),
            contentColor = Color.White
        ),
        shape = CircleShape,
        content = {
            Row (
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text,fontWeight = FontWeight.Medium, fontSize = 16.sp)
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp).rotate(90f)
                )
            }
        }
    )
}




@Preview
@Composable
fun PreviewTokenSelector() {
    var clicked by remember { mutableStateOf(0) }
    var amount by remember { mutableStateOf(AmountsUiState("0.123", "0.234")) }

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
        SelectedTokenUiState.Unselected
    )

    Column {
        TokenSelector(
            amountsUiState = amount,
            assetsUiState = assetState,
            isSyncing = false,
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
        Spacer(modifier = Modifier.height(8.dp))
        //Text(text = "Test TEXT $clicked")


    }


}