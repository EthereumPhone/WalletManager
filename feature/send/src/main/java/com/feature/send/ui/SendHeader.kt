package com.feature.send.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.core.ui.HeaderBar
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.TokenLogoFallback
import com.feature.send.AssetsUiState
import com.feature.send.R

@Composable
fun SendHeader(
    modifier: Modifier = Modifier,
    assetsUiState: AssetsUiState,
    onBackClick: () -> Unit
) {
    val primaryColor = SystemColorManager.primaryColor

    val (iconUrl, symbol) = when(assetsUiState) {
        is AssetsUiState.Success -> {
            val asset = assetsUiState.assets.first()
            asset.logoUrl to asset.symbol
        }
        else -> "" to "ETH"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = "SEND",
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        )

        val fallback = TokenLogoFallback.getFallbackLogo(symbol)

        val icon = if (iconUrl.isNullOrEmpty()) {
            if (fallback is TokenLogoFallback.LogoSource.Url) fallback.url else ""
        } else iconUrl

        val placeHolder = if (fallback is TokenLogoFallback.LogoSource.LocalResource) {
            when(symbol.uppercase()) {
                "ETH" -> R.drawable.mainnet
                "MATIC" -> R.drawable.polygon
                else -> R.drawable.placeholer_icon_5
            }
        } else R.drawable.placeholer_icon_5

        AsyncImage(
            model = icon,
            contentDescription = symbol,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape),
            placeholder = painterResource(placeHolder),
            error = painterResource(placeHolder)
        )

        Text(
            text = symbol,
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )

        // back icon
        IconButton(
            modifier = Modifier.size(56.dp),
            onClick = { onBackClick() }
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                Icon(
                    modifier = Modifier.size(32.dp).align(Alignment.CenterEnd),
                    painter = painterResource(com.core.ui.R.drawable.baseline_close_24),
                    contentDescription = "Back",
                    tint = primaryColor
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewSendHeader() {

}