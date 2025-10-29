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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.core.model.NetworkChain
import com.core.ui.HeaderBar
import com.core.ui.ChainIcon
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenBurgendy
import com.core.ui.util.dgenWhite

@Composable
fun ChainSelectorOverlay(
    isVisible: Boolean,
    selectedChainId: Int?,
    onChainSelected: (Int?) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    // All available chains including "All Chains" option
    val chains = listOf(
        null to "ALL CHAINS",
        NetworkChain.MAINNET.chainId to "ETHEREUM",
        NetworkChain.OPTIMISM.chainId to "OPTIMISM",
        NetworkChain.POLYGON.chainId to "POLYGON",
        NetworkChain.BASE.chainId to "BASE",
        NetworkChain.ARBITRUM.chainId to "ARBITRUM",
        NetworkChain.ZORA.chainId to "ZORA"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize().background(dgenBlack)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            )
            {
                HeaderBar(
                    modifier = Modifier,
                    text = "SELECT CHAIN",
                    onClick = onDismiss,
                    primaryColor = primaryColor
                )

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chains) { (chainId, chainName) ->
                            ChainRow(
                                chainId = chainId,
                                chainName = chainName,
                                isSelected = selectedChainId == chainId,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                onClick = {
                                    onChainSelected(chainId)
                                    onDismiss()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Top gradient fade
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(dgenBlack, dgenBlack, Color.Transparent)
                                )
                            )
                            .zIndex(3f)
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
}

@Composable
private fun ChainRow(
    chainId: Int?,
    chainName: String,
    isSelected: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 16.dp,bottom = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ChainIcon(
            chainId = chainId,
            size = 32.dp
        )
        
        Text(
            text = chainName,
            style = TextStyle(
                fontFamily = SpaceMono,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            ),
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(primaryColor, shape = RoundedCornerShape(50))
            )
        }
    }
}


