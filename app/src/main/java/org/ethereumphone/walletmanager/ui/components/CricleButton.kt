package org.ethereumphone.walletmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethereumphone.walletmanager.theme.WalletManagerTheme
import org.ethereumphone.walletmanager.theme.md_theme_dark_onSurface
import org.ethereumphone.walletmanager.theme.md_theme_dark_primary

@Composable
fun CircleButton(
    image: ImageVector,
    buttonText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = MaterialTheme.colors.primary

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(50.dp)
                .clip(shape = CircleShape)
                .background(color = color)
        ) {
            Icon(
                image,
                contentDescription = "Arrow",
                tint = Color.White,
                modifier = Modifier.fillMaxSize(.70f)
            )
        }
        Text(
            text = buttonText,
            fontSize = 20.sp,
            color = Color.White
        )
    }

}




@Preview
@Composable
fun CircleButtonPreview() {
    WalletManagerTheme {
        CircleButton(
            Icons.Filled.NorthEast,
            "Send",
            modifier = Modifier
        ) {}
    }

}