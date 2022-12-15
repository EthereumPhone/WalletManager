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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.glxn.qrgen.android.QRCode
import org.ethereumphone.walletmanager.theme.*
import org.ethereumphone.walletmanager.utils.WalletSDK
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


/**
 * Function to generate bitmap that is a qr code from a string
 */
fun generateQRCode(input: String, width: Int, height: Int): Bitmap {
    val stream: ByteArrayOutputStream = QRCode
        .from(input)
        .withSize(width, height)
        .stream()
    val bytearay = stream.toByteArray()
    return BitmapFactory.decodeByteArray(bytearay, 0, bytearay.size)
}


@Composable
fun ReceiveRoute(
    modifier: Modifier = Modifier,
    address: String
) {
    ReceiveScreen(
        modifier = modifier,
        address = address
    )
}

@Composable
fun ReceiveScreen(
    modifier: Modifier = Modifier,
    address: String
) {
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    WalletManagerTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = modifier
                .height(LocalConfiguration.current.screenHeightDp.dp)
                .padding(40.dp)

        ) {
            Text(
                text = "Receive ether",
                fontWeight = FontWeight.Bold,
                fontSize = 45.sp,
                modifier = Modifier.fillMaxWidth(),
                // Set color to be white
                color = md_theme_dark_onPrimary
            )
            BitmapImage(
                bitmap = generateQRCode(
                    input = "ethereum:$address",
                    width = configuration.screenWidthDp.dpToPx(),
                    height = configuration.screenWidthDp.dpToPx()
                ),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                shape = RoundedCornerShape(50.dp),
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
                    text = "Copy address",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    // Set color to be white
                    color = md_theme_dark_onPrimary
                )
            }
        }
    }
}

/**
 * Function to turn dp into pixels
 */
fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "some useful description",
    )
}



@Preview
@Composable
fun ReceiveScreenPreview() {
    ReceiveScreen(address = "0x3a4e6ed8b0f02bfbfaa3c6506af2db939ea5798c")
}