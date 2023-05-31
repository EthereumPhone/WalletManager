package org.ethereumphone.walletmanager.feature_home.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.walletmanager.core.designsystem.WmButtonColors
import org.ethereumphone.walletmanager.core.designsystem.primary
import org.ethereumphone.walletmanager.core.designsystem.primaryVariant
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.ui.components.Ethereum

@Composable
fun NetworkPill(
    currentNetwork: Network,
    address: String,
    colors: WmButtonColors = NetworkPillDefaults.wmDarkRoundedButton(),
    modifier: Modifier = Modifier,
    ) {
    val context = LocalContext.current
    Surface(
        shape = CircleShape,
        color = colors.container,
        modifier = modifier
            .clickable {
                val clipboard: ClipboardManager? =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Address", address)
                clipboard?.setPrimaryClip(clip)

                // Toast copy to clipboard
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(NetworkPillDefaults.paddingValues)

        ) {
            Box(
                Modifier
                    .clip(CircleShape)
                    .size(15.dp)
                    .background(currentNetwork.color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = currentNetwork.chainName.replace(Regex(" Mainnet| Testnet| Network"), ""),
                color = colors.content
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text= address.take(6),
                color = colors.content
            )
        }
    }
}

object NetworkPillDefaults {
    var textSize = 16.dp
    var textColor = Color.LightGray

    val paddingValues = PaddingValues(
        horizontal = 10.dp,
        vertical = 5.dp
    )

    fun wmLightRoundedButton() = WmButtonColors(
        container = primary,
        content = textColor
    )
    fun wmDarkRoundedButton() = WmButtonColors(
        container = primaryVariant,
        content = textColor
    )
}

@Preview
@Composable
fun NetworkPillPreview() {
    NetworkPill(
        currentNetwork = Ethereum,
        "0x123123123"
    )
}