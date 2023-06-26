package org.ethereumphone.walletmanager.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonDefaults.IconSpacing
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.core.designsystem.WmButtonColors
import org.ethereumphone.walletmanager.core.designsystem.onPrimary
import org.ethereumphone.walletmanager.core.designsystem.onPrimaryVariant
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.core.designsystem.primaryVariant
import org.ethereumphone.walletmanager.core.ui.WmButtonDefaults.paddingValues
import org.ethereumphone.walletmanager.core.ui.WmButtonDefaults.iconPadding
import org.ethereumphone.walletmanager.core.ui.WmButtonDefaults.wmDarkRoundedButton
import org.ethereumphone.walletmanager.core.ui.WmButtonDefaults.wmLightRoundedButton


@Composable
fun WmButton(
    value: String,
    icon: ImageVector,
    colors: WmButtonColors = WmButtonDefaults.wmLightRoundedButton(),
    textOffset: Dp = -WmButtonDefaults.iconContainer/2,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        contentPadding = paddingValues,
        colors = ButtonDefaults.buttonColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier
    ) {
        Surface(
            shape = IconButtonDefaults.filledShape,
            color = colors.content,
            modifier = Modifier
                .size(WmButtonDefaults.iconContainer)
        ) {
            Icon(
                icon,
                contentDescription = "",
                modifier = Modifier.padding(iconPadding),
                tint = colors.container
                )
        }
        Text(text = value,
            modifier = Modifier
                .weight(1f)
                .offset(-WmButtonDefaults.iconContainer / 2),
            textAlign = TextAlign.Center,
            fontSize = WmButtonDefaults.textFont,
            fontWeight = FontWeight.Bold
        )
    }
}



object WmButtonDefaults {
    val iconContainer = 30.dp
    val iconPadding = 2.dp
    val textFont = 15.sp

    val paddingValues = PaddingValues(
        horizontal = 8.dp,
        vertical = 8.dp
    )

    fun wmLightRoundedButton() = WmButtonColors(
        container = primary,
        content = onPrimary
    )
    fun wmDarkRoundedButton() = WmButtonColors(
        container = primaryVariant,
        content = onPrimaryVariant
    )
}

@Preview
@Composable
fun PreviewRoundedButton() {
    Column {
        WmButton(
            value = "test",
            icon = Icons.Rounded.ArrowUpward,
            modifier = Modifier.fillMaxWidth(),
            colors = wmDarkRoundedButton()
        ) {}
        WmButton(
            value = "test",
            icon = Icons.Rounded.ArrowUpward,
            modifier = Modifier.fillMaxWidth(),
            colors = wmLightRoundedButton()
        ) {}
        Text(text = "Test",
            fontWeight = FontWeight.W100,
            color = Color.White
        )
    }
}