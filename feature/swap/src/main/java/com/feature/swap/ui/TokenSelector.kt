package com.feature.swap.ui

import android.graphics.Paint.Align
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.ArrowDropDown
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

    var fromBalance by remember { mutableStateOf("") }
        when(assetsUiState.fromAsset) {
        is SelectedTokenUiState.Unselected -> { "" }
        is SelectedTokenUiState.Selected -> {
            formatDouble(assetsUiState.fromAsset.tokenAsset.balance)
        }

        else -> {""}
    }
//

    var toBalance by remember { mutableStateOf("") }
        when(assetsUiState.toAsset) {
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
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        //From
        Column(
//            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val fromAmountTooHigh = amountsUiState.fromAmount.isNotBlank() &&
                    (assetsUiState.fromAsset is SelectedTokenUiState.Selected) &&
                    (assetsUiState.fromAsset.tokenAsset.balance < amountsUiState.fromAmount.toDouble())


            Row (
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "From",
                    fontWeight= FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color= Color(0xFF9FA2A5),
                )

                if (fromBalance != "") {
                    Text(
                        modifier = Modifier.clickable {
                            onAmountChange(TextFieldSelected.FROM, fromBalance)
                        },
                        text = "Balance: $fromBalance",
                        fontSize = 16.sp,
                        color= if(fromAmountTooHigh) Color(0xFFF1847E) else  Color(0xFF9FA2A5)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                Row (
                    modifier = Modifier.weight(0.7f),
                    verticalAlignment = Alignment.CenterVertically
                ){
                        ethOSTextField(
                            text = fromBalance,
                            label = "0",
                            singleLine = true,
                            onTextChanged = { text -> fromBalance = text },
                            size = 64,
                            maxChar = 9,
                            sizeCut = 6,
                            numberInput = true




                            )
                    }

                Button(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    onClick = {
                        onPickAssetClicked(TextFieldSelected.FROM)
                    },
                    modifier = Modifier.weight(0.3f)
                ) {

                    val fromtext = when(assetsUiState.fromAsset) {
                        is SelectedTokenUiState.Unselected -> { "Select Token" }
                        is SelectedTokenUiState.Selected -> { assetsUiState.fromAsset.tokenAsset.symbol  }
                        else -> {""}
                    }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = fromtext, fontSize = 26.sp)
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                    }
            }

            Row(
                modifier = modifier.fillMaxWidth() ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "\$000.00-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color= Color(0xFF9FA2A5)
                )
                Button(
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    onClick = {
                        onPickAssetClicked(TextFieldSelected.FROM)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "MAX", fontSize = 14.sp)

                    }

                }
            }
        }

        //Token Switch
        IconButton(
            colors = IconButtonDefaults.iconButtonColors(
                containerColor= Color.White,
                contentColor = Color(0xFF24303D),
            ),
            onClick = {
                switchTokens()
                      },
            content = {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    tint = Color.Black,
                    contentDescription = "Swap Tokens"
                )
            }
        )


        //To

        Column {
            val toAmountTooHigh = false
            //Title
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "To",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color(0xFF9FA2A5),
                )

                if (toBalance != "") {
                    Text(
                        text = "Balance: ${toBalance}",
                        fontSize = 16.sp,
                        color = if (toAmountTooHigh) Color(0xFFF1847E) else Color(0xFF9FA2A5)
                    )
                }
            }

            //Amount
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(0.7f),
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    ethOSTextField(
                        text = toBalance,
                        label = "0",
                        singleLine = true,
                        onTextChanged = { text -> toBalance = text },
                        size = 64,
                        maxChar = 9,
                        sizeCut = 6,
                        numberInput = true
                    )
                }

                Button(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    onClick = {
                        onPickAssetClicked(TextFieldSelected.TO)
                    },
                    modifier = Modifier.weight(0.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                ) {

                    val totext = when (assetsUiState.toAsset) {
                        is SelectedTokenUiState.Unselected -> {
                            "Select Token"
                        }

                        is SelectedTokenUiState.Selected -> {
                            assetsUiState.toAsset.tokenAsset.symbol
                        }

                        else -> {
                            ""
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = totext, fontSize = 14.sp)

                    }
                }
            }

            //Dollar Amount
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$000.00-",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9FA2A5)
                )
                Button(
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "MAX", fontSize = 14.sp)
                    }
                }
            }
        }
    }
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

    Column {
//        TokenSelector(
////            amountsUiState = amount,
////            assetsUiState = AssetsUiState(SelectedTokenUiState.Unselected, SelectedTokenUiState.Unselected),
////            isSyncing = false,
//            modifier = Modifier.fillMaxWidth(),
//            {},
//            {textFieldSelected: TextFieldSelected -> clicked += 1 },
//            {textFieldSelected: TextFieldSelected, text: String ->
//                amount = when(textFieldSelected) {
//                    TextFieldSelected.FROM -> {
//                        amount.copy(
//                            fromAmount = text
//                        )
//                    }
//
//                    TextFieldSelected.TO -> {
//                        amount.copy(
//                            toAmount = text
//                        )
//                    }
//                }
//
//            }
//        )
        Spacer(modifier = Modifier.height(24.dp))
        //Text(text = "Test TEXT $clicked")
        Column (
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(36.dp)

        ){
            ExchangeRateRow(
                assetsUiState = assetState,
                exchangeUiState = 0.1,
                isSyncing = true
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){


                Text(
                    text = "Swap fee",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color= Color.White
                )
                Text(
                    text = "0.5% per transaction",
                    fontSize = 18.sp,
                    color= Color(0xFF9FA2A5)
                )
            }
        }
    }


}