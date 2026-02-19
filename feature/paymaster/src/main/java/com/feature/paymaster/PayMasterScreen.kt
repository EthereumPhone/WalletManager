package com.feature.paymaster

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SystemColorManager
import com.core.ui.util.neonOpacity
import com.core.ui.util.pulseOpacity
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.FocusManager
import com.example.dgenlibrary.AmountButton
import com.example.dgenlibrary.Quote
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.theme.DgenBackgroundHorizontalPadding

//import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground

@Composable
internal fun PayMasterScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: PayMasterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    val topUpAmount by viewModel.topUpAmount.collectAsState()
    // Track if user is navigating back to home
    // Set to false by default to ensure LED is always cleared
    var isNavigatingBack by remember { mutableStateOf(false) }


    //opens terminal screen for paymaster button
    LaunchedEffect(Unit) {
        viewModel.onTopUpOpened()
    }

    var hasHandledInitialResume by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Always update balance on resume
                    viewModel.onResume()
                    
                    if (!hasHandledInitialResume) {
                        hasHandledInitialResume = true
                    } else {
                        viewModel.onScreenOpenedAfterResume()
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onTopUpClosed()
        }
    }

    BackHandler {
        viewModel.onTopUpClosed()
        onBackClick()
    }

    PayMasterScreen(
        balance = balance,
        onBackClick = {
            viewModel.onTopUpClosed()
            onBackClick()
        },
        topUp = viewModel::topUp,
        forceRefresh = viewModel::forceUpdateBalance,
        topUpAmount = topUpAmount,
        onTopUpAmountChanged = viewModel::onTopUpAmountChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayMasterScreen(
    modifier: Modifier = Modifier,
    balance: String,
    onBackClick: () -> Unit,
    topUp: suspend (String) -> String?,
    topUpAmount: TextFieldValue,
    onTopUpAmountChanged: (TextFieldValue) -> Unit,
    forceRefresh: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val formattedBalance = try {
        val bd = BigDecimal(balance)
        bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
    } catch (e: NumberFormatException) {
        // Always show a valid numeric value, default to 0.00 if balance is invalid
        "0.00"
    }

    DgenHeaderBackground(
        onBackClick = onBackClick,
        title = "Gas",
        primaryColor = primaryColor
    ){

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.padding(start = DgenBackgroundHorizontalPadding, end = DgenBackgroundHorizontalPadding, bottom = 32.dp),

            ){
            Spacer(modifier = Modifier.fillMaxWidth().height(48.dp))

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            )
            {

                Quote(
                    primaryColor = primaryColor,
                    content = "$${formattedBalance}",
                    title = "TOTAL",
                )
                Text(
                    "This is your wallets paymaster account that handles all gas fees on any EVM chain. \n\nPaymaster transactions incur a 10% gas fee and funds cannot be withdrawn once filled.",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor.copy(neonOpacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Start
                    ),
                    modifier = Modifier.width(370.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                val amounts = listOf(10, 25, 50, 100)
                amounts.forEach { amount ->
                    val amountText = "$amount"
                    val isSelected = topUpAmount.text == amountText

                    AmountButton(
                        amount = amount,
                        isSelected = isSelected,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        onClick = {
                            onTopUpAmountChanged(
                                TextFieldValue(
                                    text = amountText,
                                    selection = TextRange(amountText.length)
                                )
                            )
                        }
                    )
                }
            }

        }

    }

}



@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun PayMasterScreenPreview() {
    PayMasterScreen(
        balance = "123.456789",
        onBackClick = {},
        topUp = { null },
        forceRefresh = {},
        topUpAmount = TextFieldValue("\$10"),
        onTopUpAmountChanged = {}
    )
}