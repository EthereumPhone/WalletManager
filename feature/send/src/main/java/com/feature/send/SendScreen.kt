package com.feature.send

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Network
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.AddressBar
import com.feature.send.ui.SwipeButton
import com.feature.send.ui.TextToggleButton
import com.feature.send.ui.ethOSTextField
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import java.util.concurrent.CompletableFuture
import java.util.jar.Manifest


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    //selectedNetwork: State<Network>,
    onBackClick: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {

    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    SendScreen(
        modifier = Modifier,
        //userAddress = userAddress,
//        onAddressClick = {
//            copyTextToClipboard(localContext, userAddress)
//        }
    )

    //SendScreen()


}
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    //userAddress: String,
    //walletAmount: WalletAmount = WalletAmount(),
    //onAddressClick: () -> Unit,
) {

    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    //var showDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var validSendAddress by remember { mutableStateOf(true) }


    val context = LocalContext.current
    //val selectedNetwork = selectedNetworkState.value


//    var hasCameraPermission by remember {
//        mutableStateOf(
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED
//        )
//    }
//    val launcher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { granted ->
//            hasCameraPermission = granted
//        }
//    )

//    LaunchedEffect(key1 = true) {
//        launcher.launch(Manifest.permission.CAMERA)
//    }

    val showDialog =  remember { mutableStateOf(false) }

    if(showDialog.value)
        qrCodeDialog(
            context = context,
            setShowDialog = {
            showDialog.value = false
        }) {
            Log.i("HomePage","HomePage : $it")
        }

//    if(showDialog) {
//        qrCodeDialog(
//            context = context,
//            setShowDialog = {
//                showDialog = false
//            },
//            onDecoded = {
//                address = it.replace("ethereum:", "")
//                showDialog = false
//            },
//        )
//    }


    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
//            .fillMaxSize()
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF1E2730))
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ){

        Column (
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
           //AddressBar(userAddress,onclick={}, network = "Mainnet")//Addressclick
            Spacer(modifier = Modifier.height(4.dp))
            ethOSTextField(
                text = address,
                label = "Address or ENS",
                onTextChanged = {
//                    address = it.lowercase()
//                    if (address.endsWith(".eth")) {
//                        if (ENSName(address).isPotentialENSDomain()) {
//                            // It is ENS
//                            CompletableFuture.runAsync {
//                                val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
//                                val ensAddr = ens.getAddress(ENSName(address))
//                                address = ensAddr?.hex.toString()
//                            }
//                        } else {
//                            if (address.isNotEmpty()) validSendAddress = WalletUtils.isValidAddress(address)
//                        }
//                        if(address.isEmpty()) {
//                            validSendAddress = true
//                        }
//                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = { showDialog.value = true },
                        modifier = Modifier.padding(end=16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Address by QR",
                            tint = if(validSendAddress) Color.White else Color.Red,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                },

                modifier = Modifier.fillMaxWidth().onFocusChanged {

//                    if (ENSName(address).isPotentialENSDomain()) {
//                        // It is ENS
//                        CompletableFuture.runAsync {
//                            val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
//                            val ensAddr = ens.getAddress(ENSName(address))
//                            address = ensAddr?.hex.toString()
//                        }
//                    } else {
//                        if (address.isNotEmpty()) validSendAddress = isValidAddress(address) }
//                    if(address.isEmpty()) validSendAddress = true

                }
            )

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Text(
                    text = "0.00 ETH",//"${walletAmount.ethAmount} ETH",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 42.sp,
                    color = Color.White
                )
                val maxed = remember { mutableStateOf(false) }
                TextToggleButton(text = "MAX", selected = maxed, onClickChange = {
                    maxed.value = !maxed.value
                })
            }
            NumPad(
                value = "",
                modifier = Modifier
            ) {}
            SwipeButton(
                text = "Swipe to send",
                icon = Icons.Rounded.ArrowForward,
                completeIcon = Icons.Rounded.Check,

            ) {

            }
        }



    }

}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

@Composable
fun qrCodeDialog(
    context: Context,
    setShowDialog: () -> Unit,
    onDecoded: (String) -> Unit
) {
//    var scanFlag by remember { mutableStateOf(false) }
//    val compoundBarcodeView = remember {
//        CompoundBarcodeView(context).apply {
//            val capture = CaptureManager(context as Activity, this)
//            capture.initializeFromIntent(context.intent, null)
//            this.setStatusText("")
//            this.resume()
//            capture.decode()
//            this.decodeContinuous { result ->
//                if(scanFlag){
//                    return@decodeContinuous
//                }
//                scanFlag = true
//                result.text?.let { barCodeOrQr->
//                    //Do something and when you finish this something
//                    //put scanFlag = false to scan another item
//                    onDecoded(barCodeOrQr)
//                    scanFlag = true
//                }
//                //If you don't put this scanFlag = false, it will never work again.
//                //you can put a delay over 2 seconds and then scanFlag = false to prevent multiple scanning
//
//            }
//        }
//    }

    //Dialog(onDismissRequest = { setShowDialog() }) {
//        AndroidView(
//            modifier = Modifier,
//            factory = { compoundBarcodeView },
//        )
    //}

    Dialog(onDismissRequest = { setShowDialog() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF24303D),
            contentColor = Color.White,
            tonalElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scan QR Code",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = Color.White,
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clickable { setShowDialog() }
                        )
                    }

                    //TODO: QRCode
//                    AndroidView(
//                        modifier = Modifier,
//                        factory = { compoundBarcodeView },
//                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun PreviewSendScreen() {
    SendScreen()
}