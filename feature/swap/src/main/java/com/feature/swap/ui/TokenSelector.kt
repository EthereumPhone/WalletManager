package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.SwapToken
import com.core.ui.util.SpaceMono

@Composable
fun TokenSelector(
    token: SwapToken?,
    primaryColor: Color,
    secondaryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectable: Boolean = true
) {
    Row(
        modifier = modifier
            .then(
                if (isSelectable) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
            .widthIn(max = 132.dp)
            .background(
                Color.Transparent,
                RoundedCornerShape(3.dp)
            )
            .padding(vertical=12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (token != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token image with chain overlay - always show chain overlay
                TokenLogoWithChain(
                    token = token.token,
                    size = 32.dp, // Increased from 24dp to 32dp
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    showChainOverlay = true // Always show chain to indicate which chain the token is on
                )
                // Token symbol
                val tokensymbol = if(token.token.symbol == "ETH") "ETH" else "\$"+token.token.symbol
                Text(
                    text = tokensymbol,
                    fontFamily = SpaceMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = if(isSelectable) 56.dp else 80.dp)
                )
            }
        } else {
            Text(
                text = "Select token",
                fontSize = 16.sp,
                fontFamily = SpaceMono,
                color = primaryColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 80.dp)
            )
        }

        // Show chevron only if selectable
        if (isSelectable) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Select token",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}