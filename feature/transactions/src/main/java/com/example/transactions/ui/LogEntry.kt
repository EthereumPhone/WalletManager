package com.example.transactions.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.core.model.TransferItem
import com.core.ui.util.abbreviateNumber
import com.core.ui.util.formatAddress
import com.core.ui.util.formatWithSuffix
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import com.example.transactions.R
import com.example.transactions.TokenAssetUiState
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class TxType{
    SENT,
    RECEIVED,
    SWAPED,
    NFT
}

data class TxEntry (
    val fromAmount: Double = 0.0,
    val toAmount: Double = 0.0,
    val fromTokenName: String = "",
    val toTokenName: String = "",
    val fromAddress: String = "",
    val toAddress: String = "",
    val txType : TxType = TxType.SENT
)

/**
 * Gets the appropriate network logo resource for native tokens (ETH/MATIC)
 * based on the chain ID
 */
@Composable
private fun getNetworkLogoResource(chainId: Int, asset: String): Int? {
    val assetUppercase = asset.uppercase()
    
    // Check if it's a native token
    val isNativeToken = when (chainId) {
        137 -> assetUppercase == "MATIC" // Polygon
        else -> assetUppercase == "ETH"
    }
    
    if (!isNativeToken) return null
    
    // Return the appropriate network logo from feature.home drawable resources
    return when (chainId) {
        1, 5 -> R.drawable.ethereum_logo // Ethereum mainnet
        11155111 -> R.drawable.mainnet // Sepolia (using mainnet logo)
        137 -> R.drawable.polygon_logo // Polygon
        10 -> R.drawable.optimism_logo // Optimism
        42161 -> R.drawable.arbitrum_logo // Arbitrum
        8453 -> R.drawable.base_logo // Base
        else -> null
    }
}

@Composable
fun LogEntry(
    logoUrl: String = "",
    logEntry : TransferItem
) {
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    //formating
    val fromValue = if(logEntry.from.takeLast(4) == ".eth"){
        logEntry.from
    } else {
        formatAddress(logEntry.from)
    }

    val toValue = if(logEntry.to.takeLast(4) == ".eth"){
        logEntry.to
    } else {
        formatAddress(logEntry.to)
    }

    // Check if we should use a network logo
    val networkLogoResource = getNetworkLogoResource(logEntry.chainId, logEntry.asset)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        when {
            // First priority: Use network logo for native tokens
            networkLogoResource != null -> {
                Image(
                    modifier = Modifier.size(24.dp).clip(CircleShape),
                    painter = painterResource(networkLogoResource),
                    contentDescription = "${logEntry.asset} on chain ${logEntry.chainId}"
                )
            }
            // Second priority: Use provided logo URL
            logoUrl.isNotEmpty() -> {
                AsyncImage(
                    modifier = Modifier.padding(bottom = 2.dp).clip(CircleShape).size(24.dp),
                    model = logoUrl,
                    contentDescription = "Token logo"
                )
            }
            // Fallback: Use placeholder
            else -> {
                Image(
                    modifier = Modifier.size(24.dp).padding(top=4.dp),
                    painter = painterResource(com.core.ui.R.drawable.placeholer_icon_5),
                    contentDescription = "Placeholder"
                )
            }
        }

        //if the tx was sending something
        if (logEntry.userSent) {

            Text(
                buildAnnotatedString {
                    //append("Sent ")
                    append("${abbreviateNumber(logEntry.value.toDouble())} ${logEntry.asset}")

                    withStyle(
                        style = SpanStyle(
                            fontFamily = SpaceMono,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    ) {
                        append(" TO ")
                    }

                    append(toValue)
                },
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        }
        else{
            Text(

                buildAnnotatedString {
                    append("${logEntry.value.toDouble().formatWithSuffix()} ${logEntry.asset}")

                    withStyle(style = SpanStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                    ) {
                        append(" FROM ")
                    }

                    append(fromValue)
                },
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )

        }
    }


}