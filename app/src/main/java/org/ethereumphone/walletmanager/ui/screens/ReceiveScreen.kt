package org.ethereumphone.walletmanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.glxn.qrgen.android.QRCode
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.*
import org.ethereumphone.walletmanager.utils.WalletSDK
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Function to generate bitmap that is a qr code from a string
 */


@Composable
fun ReceiveRoute(
    address: String,
    selectedNetworkState: State<Network>,
    onBackClick: () -> Unit
) {
    ReceiveScreen(
        address = address,
        selectedNetwork = selectedNetworkState,
        onBackClick = onBackClick
    )
}

private val networkStyles: List<NetworkStyle> = NetworkStyle.values().asList()

@Composable
fun ReceiveScreen(
    address: String,
    selectedNetwork: State<Network>,
    onBackClick: () -> Unit

) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
        ) {
            IconButton(
                onClick = { onBackClick() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "return to home screen",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(32.dp)
                )
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Send To",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .padding(top = 20.dp),
                    fontStyle = FontStyle.Normal
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(shape = CircleShape)
                            .background(
                                networkStyles.first { it.networkName == selectedNetwork.value.chainName }.color
                            )
                    )
                    Text(
                        text = "  "+selectedNetwork.value.chainName,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        BitmapImage(
            bitmap = generateQRCode(
                input = "ethereum:$address",
                width = configuration.screenWidthDp.dpToPx(),
                height = configuration.screenWidthDp.dpToPx()
            ),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = md_theme_light_primary),
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                // Copy address to clipboard
                val clipboard: ClipboardManager? =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                val clip = ClipData.newPlainText("Address", address)
                clipboard?.setPrimaryClip(clip)

                // Toast copy to clipboard
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(
                text = "Copy Address"
            )
        }
    }
}

/**
 * Function to turn dp into pixels
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun generateQRCode(input: String, width: Int, height: Int): Bitmap {
    val stream: ByteArrayOutputStream = QRCode
        .from(input)
        .withSize(width, height)
        .stream()
    val bytearay = stream.toByteArray()
    return BitmapFactory.decodeByteArray(bytearay, 0, bytearay.size)
}

@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Wallet's Address QR",
        modifier = Modifier
            .padding(top = 20.dp)
            .clip(RoundedCornerShape(20.dp))
    )
}



@Preview
@Composable
fun ReceiveScreenPreview() {
    //ReceiveScreen(address = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c")
}