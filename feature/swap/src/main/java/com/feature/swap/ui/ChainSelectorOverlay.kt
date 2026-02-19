package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.core.ui.ChainIcon
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenBlack
import com.example.dgenlibrary.ui.backgrounds.DgenHeaderBackground
import com.example.dgenlibrary.ui.backgrounds.FadeDirection
import com.example.dgenlibrary.ui.backgrounds.FadeEdge

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

    // All available chains with BASE at the top
    val chains = listOf(
        NetworkChain.BASE to "BASE",
        NetworkChain.MAINNET to "ETHEREUM",
        NetworkChain.OPTIMISM to "OPTIMISM",
        NetworkChain.POLYGON to "POLYGON",
        NetworkChain.ARBITRUM to "ARBITRUM"
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
            DgenHeaderBackground(
                title = "SELECT CHAIN",
                onBackClick = onDismiss,
                primaryColor = primaryColor
            ) {

                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chains) { (networkChain, chainName) ->
                            ChainSelectionRow(
                                chainId = networkChain.chainId,
                                chainName = chainName,
                                isSelected = selectedChainId == networkChain.chainId,
                                primaryColor = primaryColor,
                                onClick = {
                                    onChainSelected(networkChain.chainId)
                                    onDismiss()
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }



                    FadeEdge(FadeDirection.Top,  modifier = Modifier.align(Alignment.TopCenter),)
                    FadeEdge(FadeDirection.Bottom, modifier = Modifier.align(Alignment.BottomCenter))

                    // Top gradient fade

                }
            }
        }

    }
}


@Composable
private fun ChainSelectionRow(
    chainId: Int?,
    chainName: String,
    isSelected: Boolean,
    primaryColor: Color,
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


