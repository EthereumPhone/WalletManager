package org.ethereumphone.walletmanager.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import org.ethereumphone.walletmanager.core.domain.model.Network
import org.ethereumphone.walletmanager.core.model.NetworkStyle
import org.ethereumphone.walletmanager.theme.*
import java.util.Hashtable


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
    Box (modifier = Modifier.background(black)
        .fillMaxSize()) {
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
                            .size(32.dp),
                        tint = Color.White,

                        )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Address",
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier
                            .padding(top = 20.dp),
                        fontStyle = FontStyle.Normal,
                        color = Color.White
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
                            color = Color.White
                        )
                    }
                }
            }

            BitmapImage(
                bitmap = generateQRCode(
                    input = "ethereum:$address",
                    size = configuration.screenWidthDp.dpToPx(),
                ),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = dark_primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                //onClick = {}
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
                    text = "Copy Address",
                    color = Color.White,
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }

}

@Preview
@Composable
fun previewQR() {
    BitmapImage(
        bitmap = generateQRCode(
            input = "ethereum:0x3a4e6eD8B0F02BFBfaA3C6506Af2DB939eA5798c",
            size = LocalConfiguration.current.screenWidthDp.dpToPx(),
        ),
    )
}
fun generateQRCode(input: String, size: Int): Bitmap {
    val hints = Hashtable<EncodeHintType, Any>()
    hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
    val qrWriter = QRCodeWriter()
    val bitMatrix = qrWriter.encode(input, BarcodeFormat.QR_CODE, size, size, hints)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val pixels = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            if (bitMatrix[x, y]) {
                pixels[y * width + x] = android.graphics.Color.BLACK
            } else {
                pixels[y * width + x] = android.graphics.Color.WHITE
            }
        }
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    return addWhiteBorder(cropBitmap(bitmap))
}

fun addWhiteBorder(bitmap: Bitmap): Bitmap {
    val borderSize = 25
    val widthWithBorder = bitmap.width + 2 * borderSize
    val heightWithBorder = bitmap.height + 2 * borderSize

    val output = Bitmap.createBitmap(widthWithBorder, heightWithBorder, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    canvas.drawColor(android.graphics.Color.WHITE)
    canvas.drawBitmap(bitmap, borderSize.toFloat(), borderSize.toFloat(), null)

    return output
}

fun cropBitmap(bitmap: Bitmap): Bitmap {
    var top = bitmap.height
    var left = bitmap.width
    var bottom = 0
    var right = 0

    for (y in 0 until bitmap.height) {
        for (x in 0 until bitmap.width) {
            val pixel = bitmap.getPixel(x, y)
            if (pixel != android.graphics.Color.WHITE) {
                if (x < left) left = x
                if (y < top) top = y
                if (x > right) right = x
                if (y > bottom) bottom = y
            }
        }
    }

    return Bitmap.createBitmap(
        bitmap,
        left,
        top,
        right - left + 1,
        bottom - top + 1
    )
}




/**
 * Function to turn dp into pixels
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}


// Get trimmed size of QRCode bitmap without fat white borders

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



/*@Preview
@Composable
fun ReceiveScreenPreview() {
    ethOSTheme{
        ReceiveScreen(address = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c")
    }

}*/