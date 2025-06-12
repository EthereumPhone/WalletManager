package com.feature.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
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
import com.core.ui.HeaderBar
import com.core.ui.initializeFontMap
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.SystemColorManager
import com.core.ui.util.dgenBlack
import com.core.ui.util.dgenGunMetal
import com.core.ui.util.dgenWhite
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

    //opens terminal screen for receive button
    LaunchedEffect(Unit) {
        viewModel.onCopyOpened()
    }

    //closes terminal screen for receive button
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCopyClosed()
        }
    }
    
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

@Composable
fun ReceiveScreen(
    userData: UserData,
    modifier: Modifier=Modifier,
    onBackClick: () -> Unit,
    onCopyClick: () -> Unit,

) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        SystemColorManager.refresh(context)
    }

    val primaryColor = SystemColorManager.primaryColor
    val secondaryColor = SystemColorManager.secondaryColor

    val scope = rememberCoroutineScope()


        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxSize()
                .background(dgenBlack)
                .padding(horizontal = 24.dp)
            //.padding(horizontal = 32.dp, vertical = 32.dp)
        ){
            HeaderBar(text = "RECEIVE ASSETS", onClick = onBackClick, primaryColor = primaryColor)

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ){
                Image(
                    //TODO: Change Address
                    painter = rememberQrBitmapPainter(content = "ethereum:${userData.walletAddress}", primaryColor = primaryColor),
                    contentDescription = "wallet address QR",
                    contentScale = ContentScale.FillBounds,
                    //colorFilter = ColorFilter.tint(dgenRed),
                    modifier = Modifier.size(150.dp)
                        .aspectRatio(1f)//.border(2.dp, dgenTurqoise)
                )

                Spacer(modifier.height(32.dp))

                Text(
                    modifier = modifier.width(350.dp),
                    text = userData.walletAddress,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        lineHeight = 24.sp,
                        letterSpacing = 1.sp,
                        textDecoration = TextDecoration.None
                    )
                )
                Spacer(modifier.height(8.dp))
                Text(
                    modifier = modifier.width(300.dp),
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor.copy(0.5f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None,
                        textAlign = TextAlign.Center
                    ),
                    text = "This is your unique wallet address. You can use it to receive any token."
                )
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
            painter = rememberQrBitmapPainter(content = "ethereum:0xBB6d8Def979571Da5e7231938248B18B19374c55", primaryColor = Color.Red),
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