package com.feature.paymaster

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.core.ui.HeaderBar
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenGray
import com.example.dgenlibrary.ui.theme.dgenGunMetal
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenRed
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
internal fun PayMasterScreenRoute(
    modifier: Modifier = Modifier,
    viewModel: PayMasterViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val balance by viewModel.balance.collectAsState()

    PayMasterScreen(
        balance = balance,
        onBackClick = onBackClick,
        topUp = viewModel::topUp,
        forceRefresh = viewModel::forceUpdateBalance
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayMasterScreen(
    modifier: Modifier = Modifier,
    balance: String,
    onBackClick: () -> Unit,
    topUp: suspend (String) -> String?,
    forceRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showAmountDialog by remember { mutableStateOf(false) }
    var selectedAmount by remember { mutableStateOf("") }
    var customAmount by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }

    val formattedBalance = try {
        val bd = BigDecimal(balance)
        bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
    } catch (e: NumberFormatException) {
        balance
    }

    if (showAmountDialog) {
        Dialog(onDismissRequest = { showAmountDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = dgenGray.copy(0.95f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "SELECT AMOUNT",
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )

                    Text(
                        "Choose how much to add to your gas balance",
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Preset amounts
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("10", "25", "50").forEach { amount ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedAmount = amount
                                        isCustomSelected = false
                                        customAmount = ""
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedAmount == amount && !isCustomSelected) dgenTurqoise else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 2.dp,
                                    color = if (selectedAmount == amount && !isCustomSelected) dgenTurqoise else dgenGunMetal
                                )
                            ) {
                                Text(
                                    text = "$$amount",
                                    fontFamily = SpaceMono,
                                    color = if (selectedAmount == amount && !isCustomSelected) dgenBlack else dgenWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Custom amount input
                    OutlinedTextField(
                        value = customAmount,
                        onValueChange = { value ->
                            // Only allow digits and decimal point
                            if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
                                customAmount = value
                                isCustomSelected = value.isNotEmpty()
                                if (value.isNotEmpty()) {
                                    selectedAmount = value
                                }
                            }
                        },
                        label = { 
                            Text(
                                "Custom amount (USD)",
                                fontFamily = PitagonsSans,
                                color = dgenGunMetal
                            )
                        },
                        leadingIcon = {
                            Text(
                                "$",
                                fontFamily = SpaceMono,
                                color = dgenWhite,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = dgenTurqoise,
                            unfocusedBorderColor = dgenGunMetal,
                            focusedTextColor = dgenWhite,
                            unfocusedTextColor = dgenWhite,
                            cursorColor = dgenTurqoise
                        ),
                        textStyle = TextStyle(
                            fontFamily = SpaceMono,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (isCustomSelected && customAmount.isNotEmpty()) {
                        val customAmountValue = customAmount.toDoubleOrNull() ?: 0.0
                        if (customAmountValue < 10.0) {
                            Text(
                                "Minimum amount is $10",
                                fontFamily = PitagonsSans,
                                color = dgenRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showAmountDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = dgenWhite
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 2.dp,
                                color = dgenGunMetal
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "CANCEL",
                                fontFamily = SpaceMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val amount = selectedAmount.toDoubleOrNull() ?: 0.0
                                if (amount >= 10.0) {
                                    scope.launch {
                                        showAmountDialog = false
                                        val daimoUrl = topUp(selectedAmount)
                                        if (daimoUrl != null) {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(daimoUrl))
                                            context.startActivity(intent)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = selectedAmount.isNotEmpty() && (selectedAmount.toDoubleOrNull() ?: 0.0) >= 10.0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = dgenTurqoise,
                                contentColor = dgenBlack,
                                disabledContainerColor = dgenGunMetal,
                                disabledContentColor = dgenGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "TOP UP",
                                fontFamily = SpaceMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .background(dgenBlack)
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ){
        HeaderBar(text = "Gas", onClick = onBackClick)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(Modifier.offset(y = 5.dp)
                    .height(77.dp)
                    .width(8.dp)
                    .background(dgenGray.copy(0.5f))
                    .padding(end = 16.dp)
                )
                Column {
                    Text(
                        buildAnnotatedString {
                            append("TOTAL")
                        },
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
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
                    color = dgenGunMetal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.width(370.dp)
            )
        }

        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                contentColor = dgenTurqoise,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures {
                        showAmountDialog = true
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.topup_icon),
                        contentDescription = "Top up",
                        tint = dgenTurqoise
                    )
                    Text(
                        "Top up".uppercase(),
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
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
        forceRefresh = {}
    )
}