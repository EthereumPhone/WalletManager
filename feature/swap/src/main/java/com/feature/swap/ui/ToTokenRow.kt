package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenRed
import com.core.ui.util.dgenWhite
import com.core.ui.util.label_fontSize
import com.core.ui.util.neonOpacity
import com.core.ui.util.formatWithSuffix
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun ToTokenRow(
    token: TokenAsset,
    unitPriceUsd: Double,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amount = token.balance
    val usdValue = amount * unitPriceUsd
    val isOwned = amount > 0.0
    val amountText = amount.formatWithSuffix()
    val fiatText = when {
        usdValue == 0.0 -> ""
        usdValue < 0.01 -> "‹ $0.01"
        else -> "$" + DecimalFormat("0.00", DecimalFormatSymbols(Locale.US)).format(usdValue)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TokenLogoWithChain(
            token = token,
            size = 32.dp,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showChainOverlay = true
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = token.name,
                    fontFamily = PitagonsSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dgenWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(200.dp)
                )
                // Show "OWNED" badge if user has this token
                if (isOwned) {
                    Text(
                        text = "OWNED",
                        fontFamily = SpaceMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .background(
                                primaryColor.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "$"+token.symbol,
                fontFamily = PitagonsSans,
                color = dgenWhite.copy(alpha = neonOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (isOwned) {
                Text(
                    text = amountText + " " + token.symbol,
                    fontFamily = PitagonsSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                if (fiatText.isNotEmpty()) {
                    Text(
                        text = fiatText,
                        fontFamily = PitagonsSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = primaryColor,
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = "$" + formatUsd(unitPriceUsd),
                    fontFamily = PitagonsSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = dgenWhite,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ToTokenRow(
    name: String,
    symbol: String,
    logoUrl: String?,
    unitPriceUsd: Double,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chainId: Int? = null,
    displayAmount: String? = null,
    displayUsd: String? = null,
    owned: Boolean = false
) {
    val isOwned = owned || (displayAmount?.isNotBlank() == true)

    // Create a lightweight token only for rendering the logo and optional chain overlay
    val logoToken = TokenAsset(
        address = if (symbol.equals("ETH", ignoreCase = true)) {
            "0x0000000000000000000000000000000000000000"
        } else {
            // Non-ETH placeholder address to avoid ETH detection in TokenLogoWithChain
            "0x1111111111111111111111111111111111111111"
        },
        chainId = chainId ?: 1,
        symbol = symbol,
        name = name,
        balance = 0.0,
        decimals = 0,
        logoUrl = logoUrl,
        swappable = true
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TokenLogoWithChain(
            token = logoToken,
            size = 32.dp,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showChainOverlay = chainId != null
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    fontFamily = PitagonsSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dgenWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(200.dp)
                )
                if (isOwned) {
                    Text(
                        text = "OWNED",
                        fontFamily = SpaceMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier
                            .background(
                                primaryColor.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = "$" + symbol,
                fontFamily = PitagonsSans,
                color = dgenWhite.copy(alpha = neonOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (isOwned && (displayAmount != null || displayUsd != null)) {
                if (displayAmount != null) {
                    Text(
                        text = displayAmount,
                        fontFamily = PitagonsSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                if (displayUsd != null) {
                    Text(
                        text = displayUsd,
                        fontFamily = PitagonsSans,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = primaryColor,
                        maxLines = 1
                    )
                }
            } else {
                Text(
                    text = "$" + formatUsd(unitPriceUsd),
                    fontFamily = PitagonsSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = dgenWhite,
                    letterSpacing = 1.sp,
                    maxLines = 1
                )
            }
        }
    }
}