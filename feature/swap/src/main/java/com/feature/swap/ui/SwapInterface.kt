package com.feature.swap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.model.TokenAsset
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.formatWithSuffix

@Composable
fun SwapInterface(
    primaryColor: Color = dgenTurqoise,
    secondaryColor: Color = dgenOcean,
    uiState: SwapUIState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top=40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // From token section using Dgen AmountTextFieldBasic
        AmountTextFieldBasic(
            currentAmount = uiState.fromCurrentAmount,
            currentFiatAmount = uiState.fromCurrentFiatAmount,
            formattedMaxAmount = uiState.fromToken?.token?.balance?.formatWithSuffix() ?: "0.00",
            formattedMaxFiatAmount = uiState.fromToken?.formattedMaxFiatAmount ?: "0.00",
            useMaxAmount = uiState.fromUseMaxAmount,
            title = uiState.fromTitle,
            secondaryContent = { isSelectable ->
                TokenSelector(
                    token = uiState.fromToken,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onClick = uiState.fromOnTokenClick,
                    isSelectable = isSelectable
                )
            },
            onAmountChange = uiState.fromOnAmountChange,
            onMaxClick = uiState.fromOnMaxClick,
            readOnly = uiState.fromReadOnly,
            maxClickable = !uiState.fromReadOnly,
            secondarySelectable = true,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Arrow Down",
                modifier = Modifier.size(32.dp),
                tint = primaryColor
            )
        }

        // To token section using Dgen AmountTextFieldBasic (display-focused)
        AmountTextFieldBasic(
            currentAmount = uiState.toCurrentAmount,
            currentFiatAmount = uiState.toCurrentFiatAmount,
            formattedMaxAmount = uiState.toToken?.token?.balance?.formatWithSuffix() ?: "0.00",
            formattedMaxFiatAmount = uiState.toToken?.formattedMaxFiatAmount ?: "0.00",
            useMaxAmount = uiState.toUseMaxAmount,
            title = uiState.toTitle,
            showMaxAmount = false,
            secondaryContent = { isSelectable ->
                TokenSelector(
                    token = uiState.toToken,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    onClick = uiState.toOnTokenClick,
                    isSelectable = isSelectable
                )
            },
            onAmountChange = uiState.toOnAmountChange,
            onMaxClick = uiState.toOnMaxClick,
            readOnly = uiState.toReadOnly,
            maxClickable = false,
            secondarySelectable = true,
        )
    }
}

@Preview
@Composable
private fun PreviewSwapInterface() {
    // Create test tokens
    val ethToken = SwapToken(
        token = TokenAsset(
            address = "0x0000000000000000000000000000000000000000",
            chainId = 1,
            symbol = "ETH",
            name = "Ethereum",
            balance = 2.5,
            decimals = 18,
            logoUrl = "",
            swappable = true
        ),
        balance = "2.5000",
        fiatBalance = "8350.00",
        formattedMaxAmount = "2.5000",
        formattedMaxFiatAmount = "8350.00"
    )
    
    val usdcToken = SwapToken(
        token = TokenAsset(
            address = "0xa0b86a33e6c3b4c0b3b4b3b4b3b4b3b4b3b4b3b4",
            chainId = 1,
            symbol = "USDC",
            name = "USD Coin",
            balance = 10000.0,
            decimals = 6,
            logoUrl = "",
            swappable = true
        ),
        balance = "10,000.00",
        fiatBalance = "10,000.00",
        formattedMaxAmount = "10,000.00",
        formattedMaxFiatAmount = "10,000.00"
    )
    
    SwapInterface(
        uiState = SwapUIState(
            fromToken = ethToken,
            fromCurrentAmount = "1.2345",
            fromCurrentFiatAmount = "4123.45",
            fromUseMaxAmount = true,
            fromTitle = "FROM",
            fromReadOnly = false,
            toToken = usdcToken,
            toCurrentAmount = "2450.00",
            toCurrentFiatAmount = "2450.00",
            toUseMaxAmount = false,
            toTitle = "TO",
            toReadOnly = true
        ),
        primaryColor = dgenTurqoise,
        modifier = Modifier.fillMaxWidth()
    )
}
