package com.feature.swap

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.SwapUIState
import com.core.model.SwapToken
import com.core.ui.HeaderBar
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.core.ui.util.SystemColorManager
import com.core.ui.util.chainIdToName
import com.core.ui.util.dgenBlack
import com.feature.swap.ui.SwapInterface

@Composable
internal fun SwapRoute(
    modifier: Modifier,
    viewModel: SwapViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {

    SwapScreen(
        modifier = modifier,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwapScreen(
    modifier: Modifier=Modifier,
    onBackClick: () -> Unit,
) {

    val context = LocalContext.current
    LaunchedEffect(Unit) { SystemColorManager.refresh(context) }

    val primaryColor = SystemColorManager.primaryColor

    // Create sample UI state for testing
    val sampleUIState = remember {
        SwapUIState(
            fromToken = SwapToken(
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
            ),
            fromCurrentAmount = "",
            fromCurrentFiatAmount = "",
            fromUseMaxAmount = false,
            fromTitle = "FROM",
            fromReadOnly = false,
            toToken = null,
            toCurrentAmount = "",
            toCurrentFiatAmount = "",
            toUseMaxAmount = false,
            toTitle = "TO",
            toReadOnly = true,
            fromOnAmountChange = { _, _ -> },
            fromOnMaxClick = { },
            fromOnTokenClick = { },
            toOnAmountChange = { _, _ -> },
            toOnMaxClick = { },
            toOnTokenClick = { }
        )
    }

    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
        //.padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        HeaderBar(text = "SWAP ASSETS", onClick = onBackClick, primaryColor = primaryColor)
        
        SwapInterface(
            primaryColor = primaryColor,
            uiState = sampleUIState,
            modifier = Modifier.fillMaxWidth()
        )
    }


}

fun isEthereumTransactionHash(input: String): Boolean {
    val transactionHashPattern = "^0x([A-Fa-f0-9]{64})$"
    return Regex(transactionHashPattern).matches(input)
}

@Preview
@Composable
fun PreviewSwapScreen() {
    SwapScreen(
        onBackClick= {},
    )
}


