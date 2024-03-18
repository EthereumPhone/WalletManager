package com.feature.home.ui

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun ethOSNetworkPill(
    modifier: Modifier = Modifier,
    address: String,
    network: String,
    chainColor: Color,
    onClick: () -> Unit
) {


    val context = LocalContext.current
    Surface (
        modifier = modifier.clip(CircleShape).clickable {
            onClick()
            //org.ethosmobile.components.library.core.copyTextToClipboard(context,address)
        },
        color = Colors.DARK_GRAY,
        contentColor = Colors.WHITE
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(4.dp,4.dp)//(4.dp,4.dp)
        ){
            //Add later
            Box(
                modifier = modifier.clip(CircleShape)
                    .size(24.dp)
                    .background(chainColor)
            )
            Text(
                text = network,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontFamily = Fonts.INTER,
            )
            Text(
                modifier = modifier.padding(end=12.dp),
                text = truncateText(address),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontFamily = Fonts.INTER,
            )
        }

    }
}


@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}


private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." //+ text.takeLast(3) + " "
    }
    return text
}


@Composable
@Preview
fun ethOSNetworkPillPreview() {
    ethOSNetworkPill(

        address = "0xfebo235b5kcten3452b45b4o",
        network = "Mainnet",
        chainColor = Colors.SUCCESS,
        onClick = {}
    )
}