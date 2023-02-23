package org.ethereumphone.walletmanager.feature_send.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import org.ethereumphone.walletmanager.core.designsystem.WmTheme
import org.ethereumphone.walletmanager.core.designsystem.onPrimary
import org.ethereumphone.walletmanager.core.model.SendData
import org.ethereumphone.walletmanager.core.ui.WmSwipeButton
import org.ethereumphone.walletmanager.core.ui.WmTextField
import org.ethereumphone.walletmanager.feature_home.model.WalletAmount
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import java.util.concurrent.CompletableFuture

private val NORMAL = Color(0XFF8F7CFF)
private val ERROR = Color(0xFFFF5F5F)
@Composable
fun SendDialog(
    modifier: Modifier = Modifier,
    walletAmount: WalletAmount = WalletAmount(),
    setShowDialog: () -> Unit,
    onConfirm: (SendData) -> Unit
) {
    var address by remember { mutableStateOf("") }
    var validSendAddress by remember { mutableStateOf(true) }
    var value by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(Icons.Default.QrCode) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }
    
    Dialog(
        onDismissRequest = {setShowDialog()},
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
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
                WmTextField(
                    text = address,
                    label = "Address or ENS",
                    trailingIcon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                icon = if (icon == Icons.Default.QrCode) {
                                    // Ask for camera permission
                                    if (!hasCameraPermission) {
                                        launcher.launch(Manifest.permission.CAMERA)
                                    }
                                    showDialog = true
                                    Icons.Default.Close
                                }else {
                                    showDialog = false
                                    Icons.Default.QrCode
                                }
                            }
                        )
                    },
                    onTextChanged = {
                        address = it
                        if (address.endsWith(".eth")) {
                            if (ENSName(address).isPotentialENSDomain()) {
                                // It is ENS
                                CompletableFuture.runAsync {
                                    val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                                    val ensAddr = ens.getAddress(ENSName(address))
                                    address = ensAddr?.hex.toString()
                                }
                            } else {
                                if (address.isNotEmpty()) validSendAddress = WalletUtils.isValidAddress(address)
                            }
                            if(address.isEmpty()) {
                                validSendAddress = true
                            }
                        }

                    }
                )
                Spacer(
                    Modifier.height(10.dp)
                )
                if (showDialog) {
                    if (hasCameraPermission) {
                        qrCodeDialog(
                            context = context,
                            setShowDialog = {
                                showDialog = false
                            },
                            onDecoded = {
                                address = it.replace("ethereum:", "")
                                showDialog = false
                            },
                        )
                    }
                } else {
                    Text(
                        text = if (value == "") {
                            "0.0 ETH"
                        } else {
                            "$value ETH"
                        },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (value == "") {
                            NORMAL
                        }else {
                            if(value.toFloat() <= walletAmount.ethAmount.toFloat()) NORMAL else ERROR
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.width(180.dp)
                    ) {
                        Divider(
                            color = Color(0xFF394450),
                            thickness = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        NumPad(
                            modifier = Modifier.fillMaxWidth(),
                            value = value,
                            onValueChange = {
                                value = it
                            }
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
                WmSwipeButton(
                    text = "Swipe to send",
                    icon = Icons.Rounded.ArrowForward,
                    completeIcon = Icons.Default.Check
                ) {

                    if (value == "") {
                        onConfirm(
                            SendData(
                                amount = "0.0".toFloat(),
                                address = address
                            )
                        )
                    } else {
                        onConfirm(
                            SendData(
                                amount = value.toFloat(),
                                address = address
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun qrCodeDialog(
    context: Context,
    setShowDialog: () -> Unit,
    onDecoded: (String) -> Unit
) {
    var scanFlag by remember { mutableStateOf(false) }
    val compoundBarcodeView = remember {
        CompoundBarcodeView(context).apply {
            val capture = CaptureManager(context as Activity, this)
            capture.initializeFromIntent(context.intent, null)
            this.setStatusText("")
            this.resume()
            capture.decode()
            this.decodeContinuous { result ->
                if(scanFlag){
                    return@decodeContinuous
                }
                scanFlag = true
                result.text?.let { barCodeOrQr->
                    //Do something and when you finish this something
                    //put scanFlag = false to scan another item
                    onDecoded(barCodeOrQr)
                    scanFlag = true
                }
                //If you don't put this scanFlag = false, it will never work again.
                //you can put a delay over 2 seconds and then scanFlag = false to prevent multiple scanning

            }
        }
    }
    AndroidView(
        modifier = Modifier,
        factory = { compoundBarcodeView },
    )
}

@Preview
@Composable
fun PreviewSendDialog() {
    WmTheme {
        SendDialog(
            walletAmount = WalletAmount(1.0, 2.0),
            modifier = Modifier
                .width(400.dp),
            setShowDialog = {}
        ) {}
    }
}