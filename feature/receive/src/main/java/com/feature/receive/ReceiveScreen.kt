package com.feature.receive

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.Hashtable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.UserData
import com.core.ui.initializeFontMap
import com.core.ui.showCustomToast
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenOcean
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.feature.receive.ui.TruncatedAddress
import com.feature.receive.ui.rememberQrBitmapPainter

@Composable
internal fun ReceiveRoute(
    modifier: Modifier = Modifier,
    viewModel: ReceiveViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    //initializes fonts for toast
    initializeFontMap(SpaceMono, PitagonsSans)
    
    ReceiveScreen(
        userData = userData,
//        modifier = modifier,
        onBackClick = onBackClick,
        onCopyClick = {
            silentlyCopyToClipboard(context, userData.walletAddress)
            //clipboard.setText(AnnotatedString(userData.walletAddress))
        }
    )

}
fun silentlyCopyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("text", text)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // This flag helps suppress the clipboard overlay on Android 13+
        clipboardManager.setPrimaryClip(clipData)
    } else {
        clipboardManager.setPrimaryClip(clipData)
    }

    // Optional: Show a toast or some other feedback that doesn't use system UI
    // Toast.makeText(context, "Text copied", Toast.LENGTH_SHORT).show()
}
@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

@Composable
fun ReceiveScreen(
    userData: UserData,
    modifier: Modifier=Modifier,
    onBackClick: () -> Unit,
    onCopyClick: () -> Unit,

) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .background(dgenBlack)
            //.padding(horizontal = 32.dp, vertical = 32.dp)
        ){
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = 24.dp,
                        start = 24.dp, top = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                androidx.compose.material3.Text(
                    text = "RECEIVE ASSETS",
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.Medium,
                        fontSize = 24.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )

                androidx.compose.material3.Icon(
                    modifier = Modifier.size(32.dp).pointerInput(Unit) {
                        detectTapGestures {
                            onBackClick()
                        }
                    },
                    painter = painterResource(R.drawable.baseline_close_24),
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )

            }



            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ){
                Image(
                    //TODO: Change Address
                    painter = rememberQrBitmapPainter(content = "ethereum:${userData.walletAddress}"),
                    contentDescription = "wallet address QR",
                    contentScale = ContentScale.FillBounds,
                    //colorFilter = ColorFilter.tint(dgenRed),
                    modifier = Modifier.size(150.dp)
                        .aspectRatio(1f)
                )

                Spacer(modifier.height(24.dp))
                TruncatedAddress(userData.walletAddress)
                Spacer(modifier.height(8.dp))

                Text(
                    modifier = modifier.width(300.dp),
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenTurqoise.copy(0.35f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Center
                    ),
                    text = "This is your unique wallet address. You can use it to receive any token."
                )



            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(dgenBlack)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                IconButton(modifier = Modifier.clip(RoundedCornerShape(0.dp)).width(110.dp).height(50.dp).padding(bottom = 8.dp),
                    onClick = {
                        onCopyClick()

                        context.showCustomToast(
                            message = "Address copied!",
                            fontFamily = PitagonsSans,
                            fontWeight = FontWeight.SemiBold,
                            backgroundColor = dgenOcean,
                            textColor = dgenTurqoise
                        )
                    }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(R.drawable.cpyadd),
                            contentDescription = "Back",
                            tint = dgenTurqoise
                        )
                        Text(
                            text= "CPY ADD",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = dgenTurqoise,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 16.sp,
                                letterSpacing = 1.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                }

            }
        }
    }

}

@Preview
@Composable
fun TestQr() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(10.dp)

    ) {
        Image(
            painter = rememberQrBitmapPainter(content = "ethereum:0xBB6d8Def979571Da5e7231938248B18B19374c55"),
            contentDescription = "wallet address QR",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.size(135.dp),
        )
    }
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

@Composable
fun BitmapImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Wallet's Address QR",
        modifier = Modifier
            .clip(RoundedCornerShape(5))

    )
}




@Preview
@Composable
fun PreviewReceiveScreen() {
    val address = "0x71C7656EC7ab88b098defB751B7401B5f6d8976F"
    //ReceiveScreen()
}