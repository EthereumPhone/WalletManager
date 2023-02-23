package org.ethereumphone.walletmanager.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandCircleDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.WmListColors
import org.ethereumphone.walletmanager.core.designsystem.background
import org.ethereumphone.walletmanager.core.designsystem.onPrimary
import org.ethereumphone.walletmanager.core.designsystem.onPrimaryVariant
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.core.designsystem.primaryVariant


@Composable
fun WmListItem(
    primaryText: String = "",
    secondaryText: String = "",
    icon: ImageVector,
    colors: WmListColors = WmListDefaults.wmDarkRoundedButton(),
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(15),
            color = colors.container,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WmListDefaults.wmDarkRoundedButton().content,
                modifier = Modifier
                    .size(WmListDefaults.iconSize)
                    .padding(10.dp)

            )
        }
        Spacer(modifier = Modifier.size(15.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = primaryText,
                color = colors.content,
                fontWeight = FontWeight.Bold,
                fontSize = WmListDefaults.primaryFontSize,
            )
            Text(
                text = secondaryText,
                color = colors.neutral,
                fontSize = WmListDefaults.secondaryFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(WmListDefaults.iconSize)
        ) {
            Icon(
                imageVector = Icons.Outlined.ExpandCircleDown,
                contentDescription = "expand Item",
                modifier = Modifier
                    .size(WmListDefaults.expandIconSize)
                    .clickable { onClick() },
                tint = WmListDefaults.wmDarkRoundedButton().container
            )
        }
    }
}

object WmListDefaults {
    val expandIconSize = 25.dp
    val iconSize = 50.dp
    val primaryFontSize = 18.sp
    val secondaryFontSize = 12.sp

    fun wmLightRoundedButton() = WmListColors(
        container = primary,
        content = onPrimary,
        neutral = Color.LightGray
    )
    fun wmDarkRoundedButton() = WmListColors(
        container = primaryVariant,
        content = onPrimaryVariant,
        neutral = Color.LightGray
    )
}

@Preview
@Composable
fun PreviewWmListItem() {
    Surface(color = background) {
        WmListItem(
            primaryText = "0.001 ETH",
            secondaryText = "Recieved 12 Seconds ago",
            icon = Icons.Rounded.Add,
        ) {}
    }
}



