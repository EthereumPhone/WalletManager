package com.feature.send.ui

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAssetWithPrice
import com.core.ui.DgenBasicTextfield
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.pulseOpacity
import com.core.ui.util.smallDuration
import com.feature.send.AmountUiState
import com.feature.send.SelectedAssetUiState


@Composable
fun AmountTextField(
    selectedAssetUiState: SelectedAssetUiState,
    amountUiState: AmountUiState,
    onAmountChange: (String, Boolean) -> Unit,
    onMaxClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor
    var toggleFiat by remember { mutableStateOf(false) }

    val maxAlpha by animateFloatAsState(
        targetValue = if (amountUiState.useMaxAmount) 1f else pulseOpacity,
        animationSpec = tween(smallDuration,easing = FastOutLinearInEasing),
    )

    val amount = if (toggleFiat) {
        amountUiState.currentFiatAmount
    } else {
        amountUiState.currentAmount
    }

    // trick to keep cursor in correct position
    var textFieldValue by remember { mutableStateOf(TextFieldValue(amount)) }
    LaunchedEffect(amount) {
        if (textFieldValue.text != amount) {
            // Set cursor to end of text when amount changes (e.g., when MAX is clicked)
            textFieldValue = TextFieldValue(
                text = amount,
                selection = TextRange(amount.length)
            )
        }
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // vertical flair box
        Spacer(Modifier
            .offset(y = 5.dp)
            .height(77.dp)
            .width(8.dp)
            .background(primaryColor.copy(pulseOpacity))
            .padding(end = 8.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // currency toggle
                TextToggle(
                    Modifier.offset(x = 2.dp, y=4.dp),
                    when(selectedAssetUiState) {
                        is SelectedAssetUiState.Selected -> selectedAssetUiState.tokenAsset.symbol.uppercase()
                        SelectedAssetUiState.Unselected -> "ETH"
                    },
                    "$",
                    onToggle = {
                        toggleFiat = !toggleFiat

                        onAmountChange("", toggleFiat)
                               },
                    value = toggleFiat,
                    primaryColor = primaryColor
                )

                // max amount
                val maxAmount = if (toggleFiat) {
                    "$${amountUiState.formattedMaxFiatAmount}"
                } else {
                    amountUiState.formattedMaxAmount
                }
                Text(
                    text = "MAX  $maxAmount",
                    fontFamily = PitagonsSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    color = primaryColor.copy(maxAlpha),
                    modifier = Modifier
                        .offset(y = 2.dp)
                        .clickable {
                            onMaxClick()
                        }
                )
            }

            // amount TextField
            DgenBasicTextfield(
                value = textFieldValue,
                onValueChange={ new ->

                    if(new.text == textFieldValue.text) {
                        textFieldValue = new
                    } else if (new.text.matches("^\\d*\\.?\\d*$".toRegex())) {
                        textFieldValue = new
                        onAmountChange(new.text, toggleFiat)
                    }
                },
                maxLines = 1,
                maxLength = 15,
                cursorColor = primaryColor,
                placeholder = {
                    Text(
                        modifier = Modifier,
                        text = "0.0", // Static placeholder
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite.copy(alpha = pulseOpacity),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 42.sp,
                            textAlign = TextAlign.Start,
                        )
                    )
                },
                textStyle = TextStyle(
                    fontFamily = PitagonsSans,
                    color = if (isValidAmount(amountUiState, toggleFiat)) dgenWhite else dgenRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 42.sp,
                    textAlign = TextAlign.Start
                ),
                keyboardtype =  KeyboardType.Number,
                cursorWidth = 24.dp,
                cursorHeight= 24.dp,
                isAnyFieldFocused= remember { mutableStateOf(false) },
            )
        }
    }
}

private fun isValidAmount(amountUiState: AmountUiState, toggleFiat: Boolean): Boolean {

    if (amountUiState.useMaxAmount) return true

    return if (toggleFiat) {
        if(amountUiState.currentFiatAmount.isEmpty()) return true
        amountUiState.currentFiatAmount.toDouble() <= amountUiState.maxFiatAmount
    } else {
        if(amountUiState.currentAmount.isEmpty()) return true
        amountUiState.currentAmount.toDouble() <= amountUiState.maxAmount
    }
}



@Preview
@Composable
fun AmountTextFieldPreview() {
    val amount = AmountUiState(
        123.0,
        "123",
        "123",
        0.0,
        "",
        ""
    )

    val selectedAssetUiState = SelectedAssetUiState.Selected(
        TokenAssetWithPrice(
            address = "",
            chainId = 1,
            symbol = "USDC",
            name = "USDCoin",
            balance = 13.3,
            decimals = 16,
            logoUrl = "",
            swappable = false,
            fiatAmount = 13.3,
        )
    )

    AmountTextField(
        selectedAssetUiState,
        amount,
        { x, y ->

        },
        {},
    )
}

@Preview
@Composable
fun AmountTextFieldFiatPreview() {



    val amount = AmountUiState(
        72.0,
        "72",
        "123",
        0.0,
        "",
        "",
        false
    )

    val selectedAssetUiState = SelectedAssetUiState.Selected(
        TokenAssetWithPrice(
            address = "",
            chainId = 1,
            symbol = "USDC",
            name = "USDCoin",
            balance = 13.3,
            decimals = 16,
            logoUrl = "",
            swappable = false,
            fiatAmount = 13.3,
        )
    )

    AmountTextField(
        selectedAssetUiState,
        amount,
        { x, y ->

        },
        {},
    )
}