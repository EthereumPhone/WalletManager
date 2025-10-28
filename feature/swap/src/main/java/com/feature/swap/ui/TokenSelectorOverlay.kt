package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.core.database.provider.TokenMetadataProviderContract
import com.core.model.TokenAsset
import com.core.model.TokenGroupAssetOverview
import com.core.ui.HeaderBar
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.feature.swap.TokenSelectionMode

@Composable
fun TokenSelectorOverlay(
    isVisible: Boolean,
    mode: TokenSelectionMode,
    fromAssets: List<TokenGroupAssetOverview>,
    toTokens: List<TokenAsset>,
    selectFromGroup: (groupId: String) -> Unit,
    selectToToken: (TokenAsset) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    onDismiss: () -> Unit,
    currentChainId: Int?,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).background(dgenBlack)
        ) {
            HeaderBar(
                modifier = Modifier,
                text = "SELECT TOKEN",
                onClick = onDismiss,
                primaryColor = primaryColor
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (mode) {
                    TokenSelectionMode.From -> {
                        if (fromAssets.isNotEmpty()) {
                            TokenCardSwapCarousel(
                                assets = fromAssets,
                                navigateToSend = { groupId ->
                                    selectFromGroup(groupId)
                                    onDismiss()
                                },
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                modifier = Modifier.fillMaxSize().offset(y = -8.dp)
                            )
                        }
                    }
                    TokenSelectionMode.To -> {
                        val context = LocalContext.current
                        var priceMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }

                        LaunchedEffect(isVisible, currentChainId) {
                            val chainId = currentChainId
                            if (chainId != null) {
                                runCatching {
                                    TokenMetadataProviderContract.getTokensByChain(
                                        context.contentResolver,
                                        chainId
                                    )
                                }.onSuccess { list ->
                                    priceMap = list.associate { it.contractAddress.lowercase() to it.price }
                                }.onFailure {
                                    priceMap = emptyMap()
                                }
                            } else {
                                priceMap = emptyMap()
                            }
                        }

                        if (toTokens.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item{
                                    Spacer(modifier = Modifier.fillMaxWidth().height(24.dp))
                                }
                                items(toTokens, key = { it.address + "_" + it.chainId }) { token ->
                                    val unitPrice = priceMap[token.address.lowercase()] ?: 0.0
                                    ToTokenRow(
                                        token = token,
                                        unitPriceUsd = unitPrice,
                                        primaryColor = primaryColor,
                                        secondaryColor = secondaryColor,
                                        onClick = {
                                            selectToToken(token)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> { /* Do nothing */ }
                }

                // Top gradient fade
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 0.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(dgenBlack, dgenBlack, Color.Transparent)
                            )
                        ).zIndex(3f)
                )

                // Bottom gradient fade
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, dgenBlack)
                            )
                        )
                        .zIndex(3f)
                )
            }
        }
    }
}



fun formatAmount(value: Double): String {
    return if (value == 0.0) "0.0" else String.format("%.6f", value).trimEnd('0').trimEnd('.')
}

fun formatUsd(value: Double): String {
    return if (value == 0.0) "0.00" else String.format("%,.2f", value)
}


