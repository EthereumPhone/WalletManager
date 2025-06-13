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
import com.core.ui.DgenButtonTextfield
import com.core.ui.HeaderBar
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGray
import com.core.ui.util.dgenGunMetal
import com.core.ui.util.dgenOcean
import com.core.ui.util.dgenTurqoise
import com.core.ui.util.dgenWhite
import com.core.ui.util.neonOpacity
import com.core.ui.util.pulseOpacity
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
internal fun PayMasterScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: PayMasterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()
    val topUpAmount by viewModel.topUpAmount.collectAsState()


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
        }
    }

    //closes terminal screen for paymaster button
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onTopUpClosed()
        }
    }

    PayMasterScreen(
        balance = balance,
        onBackClick = onBackClick,
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

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var selectedAmount by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }
//    var toUpAmount by remember { mutableStateOf(TextFieldValue("")) }
    val view = LocalView.current

    val formattedBalance = try {
        val bd = BigDecimal(balance)
        bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
    } catch (e: NumberFormatException) {
        balance
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            },
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        HeaderBar(text = "Gas", onClick = onBackClick, modifier = modifier.padding(horizontal = 12.dp), primaryColor = primaryColor)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier.padding(start = 12.dp, end = 12.dp)
        ){


            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.offset(y = 5.dp)
                        .height(77.dp)
                        .width(8.dp)
                        .background(primaryColor.copy(pulseOpacity))
                        .padding(end = 16.dp)
                    )
                    Column {
                        Text(
                            buildAnnotatedString {
                                append("TOTAL")
                            },
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            lineHeight = 18.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None,
                            modifier = Modifier.offset(y = 8.dp)
                        )
                        Text(
                            "$${formattedBalance}",
                            fontFamily = PitagonsSans,
                            color = dgenWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 48.sp,
                            lineHeight = 48.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    }
                }

                Text(
                    "Your wallet comes with a Paymaster account that covers gas on any chain. You can transact across chains without ETH or native tokens. \n\nPaymaster funds are not removeable.",
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

            /*DgenButtonTextfield(
            value = topUpAmount,
            onValueChange = { newValue ->
                val input = newValue.text
                
                // Remove any existing "$" to get the raw input
                val cleanInput = input.removePrefix("$")
                
                // Only allow digits and decimal point
                if (cleanInput.isEmpty()) {
                    onTopUpAmountChanged(TextFieldValue(""))
                } else if (cleanInput.matches(Regex("^\\d*\\.?\\d*$"))) {
                    // Add "$" prefix if there's any numeric input
                    val newText = "$$cleanInput"
                    // Set cursor position at the end (behind the number)
                    onTopUpAmountChanged(
                        TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                    )
                } else {
                    // Keep the previous value if input is invalid
                    onTopUpAmountChanged(topUpAmount)
                }
            },
            isAnyFieldFocused = remember { mutableStateOf(false) },
            onEditDone = { },
            view = view,
            labelContent = {
                Text(
                    text = "ADD AMOUNT",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                )
            },
            placeholder = {
                Text(
                    text = "0.00",
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite.copy(pulseOpacity),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = body2_fontSize
                    )
                )
            },
            button1Text = "10",
            button2Text = "20",
            button1Value = "\$10",
            button2Value = "\$20",
            onButton1Click = {
                onTopUpAmountChanged(TextFieldValue("\$10"))
            },
            onButton2Click = {
                onTopUpAmountChanged(TextFieldValue("\$20"))
            },
            modifier = Modifier.fillMaxWidth()
        )*/
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val amounts = listOf(10, 25,50,100)
            amounts.forEach { amount ->
                val amountText = "$amount"
                val isSelected = topUpAmount.text == amountText

                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .aspectRatio(16f/9f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) primaryColor else Color.Transparent)
                        .border(BorderStroke(1.dp, primaryColor), RoundedCornerShape(4.dp))
                        .clickable {
                            onTopUpAmountChanged(
                                TextFieldValue(
                                    text = amountText,
                                    selection = TextRange(amountText.length)
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = PitagonsSans,
                                    color = if (isSelected) secondaryColor else primaryColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    letterSpacing = 0.sp,
                                    textDecoration = TextDecoration.None
                                )
                            ) {
                                append("\$")
                            }
                            append(amountText)
                        },

                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = if (isSelected) secondaryColor else primaryColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
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