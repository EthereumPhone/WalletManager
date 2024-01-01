package com.feature.receive

import android.annotation.SuppressLint
import android.app.PendingIntent.getActivity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.Gravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.DismissValue
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode
import java.util.Hashtable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.UserData
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.feature.receive.ui.rememberQrBitmapPainter
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import net.glxn.qrgen.core.image.ImageType
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import java.io.ByteArrayOutputStream

@Composable
internal fun ReceiveRoute(
    modifier: Modifier = Modifier,
    viewModel: ReceiveViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    ReceiveScreen(
        userData = userData,
//        modifier = modifier,
        onBackClick = onBackClick,
        onCopyClick = {
            copyTextToClipboard(context, userData.walletAddress)
        }
    )

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

    Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = modifier
                .fillMaxSize()
                .background(Colors.BLACK)
                //.padding(horizontal = 32.dp, vertical = 32.dp)
        ){
        ethOSHeader(
            title="Receive",
            isBackButton = true,
            onBackClick = onBackClick,
        )



            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier

            ){
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Colors.WHITE)
                        .padding(10.dp)
                        .size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        //TODO: Change Address
                        painter = rememberQrBitmapPainter(content = "ethereum:${userData.walletAddress}"),
                        contentDescription = "wallet address QR",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .aspectRatio(1f)
                    )
                }


                Spacer(modifier = Modifier.height(48.dp))

                //Address
                //TODO: Address or ENS
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        modifier = modifier.width(175.dp),
                        fontSize = 18.sp,
                        fontFamily = Fonts.INTER,
                        color = Colors.GRAY,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Normal,
                        text = "Address"
                    )
                    //TODO: Make reveil compasable
                    Text(
                        modifier = modifier.width(250.dp),
                        fontSize = 16.sp,
                        color = Color.White,
                        fontFamily = Fonts.INTER,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        text = userData.walletAddress
                    )


                }

            }


        Row (
            modifier = Modifier.padding(start = 32.dp,end = 32.dp, bottom = 32.dp)
        ){
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    contentColor =  Color.White,
                ),
                onClick = onCopyClick
            ){
                Row {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Copy ethereum address",
                        tint = Colors.WHITE,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy address",
                        fontSize = 16.sp,
                        color = Colors.WHITE,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
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