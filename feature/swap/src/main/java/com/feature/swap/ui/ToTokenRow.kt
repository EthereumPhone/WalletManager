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
                    text = formatAmount(amount) + " " + token.symbol,
                    fontFamily = PitagonsSans,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = "$" + formatUsd(usdValue),
                    fontFamily = PitagonsSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = primaryColor,
                    maxLines = 1
                )
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