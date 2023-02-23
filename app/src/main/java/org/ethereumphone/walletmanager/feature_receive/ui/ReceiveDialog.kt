package org.ethereumphone.walletmanager.feature_receive.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.view.Gravity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.DismissValue
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.core.designsystem.onPrimary
import org.ethereumphone.walletmanager.core.ui.WmSwipeButton
import java.io.ByteArrayOutputStream
import java.util.Hashtable


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReceiveDialog(
    address: String,
    modifier: Modifier = Modifier,
    setShowDialog: () -> Unit
) {
    Dialog(
        onDismissRequest = {setShowDialog()},
        properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            color = onPrimary,
            shape = RoundedCornerShape(3),
            modifier = modifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(
                        horizontal = 40.dp,
                        vertical = 40.dp,
                    )
            ) {
                BoxWithConstraints {
                    val boxWithConstraintsScope = this
                    val width = with(LocalDensity.current) {boxWithConstraintsScope.maxWidth.toPx()*0.9}.toInt()
                    BitmapImage(
                        bitmap = generateQRCode(
                            input = "ethereum:$address",
                            size = width,
                        ),
                    )
                }
                Spacer(Modifier.weight(1f))
                WmSwipeButton(
                    text = "Dismiss",
                    icon = Icons.Rounded.ArrowForward,
                    completeIcon = Icons.Rounded.Close
                ) {
                    setShowDialog()
                }
            }
        }
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
fun PreviewSendDialog() {
    WmTheme {
        ReceiveDialog(
            address = "0x123123123",
            modifier = Modifier.height(400.dp)
        ) {}
    }
}