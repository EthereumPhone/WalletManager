package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.model.TokenAssetWithPrice
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
fun TokenRow(
    name: String,
    symbol: String,
    logoUrl: String?,
    unitPriceUsd: Double,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    chainId: Int? = null,
    amount: Double? = null,
    usdValue: Double? = null,
    owned: Boolean = false
) {
    val isOwned = owned || (amount != null && amount > 0.0)

    // Check if this is native ETH by checking for zero address or EeeeeE address
    val isNativeEth = logoUrl?.let { url ->
        url.contains("0x0000000000000000000000000000000000000000", ignoreCase = true) ||
        url.contains("0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE", ignoreCase = true)
    } ?: symbol.equals("ETH", ignoreCase = true)

    // Create a lightweight token only for rendering the logo and optional chain overlay
    val logoToken = TokenAsset(
        address = if (isNativeEth) {
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
            size = 40.dp,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            showChainOverlay = chainId != null
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
                Text(
                    text = name,
                    fontFamily = PitagonsSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = dgenWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max=200.dp)
                )

            Text(
                text = "$$symbol",
                fontFamily = PitagonsSans,
                color = dgenWhite.copy(alpha = neonOpacity),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (isOwned && (amount != null || usdValue != null)) {
                if (amount != null && amount > 0.0) {
                    val formattedAmount = amount.formatWithSuffix()
                    Text(
                        text = "$formattedAmount $symbol",
                        fontFamily = PitagonsSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1
                    )
                }
                if (usdValue != null && usdValue > 0.0) {
                    val formattedUsd = usdValue.formatWithSuffix()
                    Text(
                        text = "$$formattedUsd",
                        fontFamily = PitagonsSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Convenience overload that accepts a TokenAsset directly
@Composable
fun TokenRow(
    token: TokenAssetWithPrice,
    unitPriceUsd: Double,
    fiatAmount: Double = 0.0,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val balance = token.balance
    //val usdValue = if (balance > 0.0 && unitPriceUsd > 0.0) balance * unitPriceUsd else null
    
    // Debug logging for ETH tokens
    if (token.symbol.equals("ETH", ignoreCase = true)) {
        //android.util.Log.d("TokenRow", "ETH Token - Symbol: ${token.symbol}, Balance: $balance, UnitPrice: $unitPriceUsd, USD Value: $usdValue")
    }
    
    // Check if this token is native ETH by its address or if address equals chainId (network token)
    val isNativeEth = token.address.equals("0x0000000000000000000000000000000000000000", ignoreCase = true) ||
                      token.address.equals("0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE", ignoreCase = true) ||
                      token.address == token.chainId.toString() || // Network token (address is chain ID)
                      token.symbol.equals("ETH", ignoreCase = true)
    
    // Normalize ETH tokens to always show as "Ethereum" and "ETH" regardless of chain
    val displayName = if (isNativeEth) "Ethereum" else token.name
    val displaySymbol = if (isNativeEth) "ETH" else token.symbol
    
    // Use the token's logoUrl, but for native ETH we can construct it from the address
    val effectiveLogoUrl = if (isNativeEth && token.logoUrl.isNullOrEmpty()) {
        "0x0000000000000000000000000000000000000000"
    } else {
        token.logoUrl
    }
    
    TokenRow(
        name = displayName,
        symbol = displaySymbol,
        logoUrl = effectiveLogoUrl,
        unitPriceUsd = unitPriceUsd,
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        onClick = onClick,
        modifier = modifier,
        chainId = token.chainId,
        amount = if (balance > 0.0) balance else null,
        usdValue = fiatAmount,
        owned = balance > 0.0
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TokenRowPreview_Owned() {
    val sampleToken = TokenAssetWithPrice(
        address = "0x1234567890123456789012345678901234567890",
        chainId = 1,
        symbol = "USDC",
        name = "USD Coin",
        balance = 1250.75,
        decimals = 6,
        logoUrl = null,
        swappable = true
    )
    
    TokenRow(
        token = sampleToken,
        unitPriceUsd = 0.999,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF8800FF),
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TokenRowPreview_NotOwned() {
    val sampleToken = TokenAssetWithPrice(
        address = "0x1234567890123456789012345678901234567890",
        chainId = 1,
        symbol = "LINK",
        name = "Chainlink",
        balance = 0.0,
        decimals = 18,
        logoUrl = null,
        swappable = true
    )
    
    TokenRow(
        token = sampleToken,
        unitPriceUsd = 14.52,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF8800FF),
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TokenRowPreview_SimpleVersion_Owned() {
    TokenRow(
        name = "Wrapped Ethereum",
        symbol = "WETH",
        logoUrl = null,
        unitPriceUsd = 3421.89,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF8800FF),
        onClick = {},
        chainId = 1,
        amount = 2.5,
        usdValue = 8554.72,
        owned = true
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TokenRowPreview_SimpleVersion_NotOwned() {
    TokenRow(
        name = "Uniswap",
        symbol = "UNI",
        logoUrl = null,
        unitPriceUsd = 8.75,
        primaryColor = Color(0xFF00FF88),
        secondaryColor = Color(0xFF8800FF),
        onClick = {},
        chainId = 1,
        owned = false
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
private fun TokenRowPreview_LongName() {
    TokenRow(
        name = "Very Long Token Name That Should Be Truncated",
        symbol = "VLTNTST",
        logoUrl = null,
        unitPriceUsd = 0.000123,
        primaryColor = Color(0xFFFF0088),
        secondaryColor = Color(0xFF8800FF),
        onClick = {},
        chainId = 137,
        owned = false
    )
}