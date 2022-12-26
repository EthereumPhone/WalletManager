package org.ethereumphone.walletmanager.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.*
import org.ethereumphone.walletmanager.ui.components.InputField
import org.ethereumphone.walletmanager.utils.WalletSDK
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture

@Composable
fun SendRoute(
    selectedNetwork: State<Network>,
    onBackClick: () -> Unit
) {
    SendScreen(
        selectedNetworkState = selectedNetwork,
        onBackClick = onBackClick
    )
}

private val networkStyles: List<NetworkStyle> = NetworkStyle.values().asList()

@Composable
fun SendScreen(
    selectedNetworkState: State<Network>,
    onBackClick: () -> Unit
) {
    var address by remember { mutableStateOf("") }
    var ensToAddress by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val selectedNetwork = selectedNetworkState.value


    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if(showDialog) {
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

    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
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
                                networkStyles.first { it.networkName == selectedNetworkState.value.chainName }.color
                            )
                    )
                    Text(
                        text = "  "+selectedNetworkState.value.chainName,
                        fontSize = 12.sp,
                    )
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            Row {
                InputField(
                    label = "To Address/ENS",
                    value = address,
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            if (ENSName(address).isPotentialENSDomain()) {
                                // It is ENS
                                CompletableFuture.runAsync {
                                    val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                                    val ensAddr = ens.getAddress(ENSName(address))

                                    ensToAddress = ensAddr?.hex
                                        .toString()
                                        .replace("null", "")
                                }
                            } else {
                                ensToAddress = ""
                            }
                        }
                ) {
                    address = it
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Address by QR",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = ensToAddress,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        InputField(
            label = "Amount",
            modifier = Modifier.fillMaxWidth(),
            value = amount,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        ) { amount = it}

        Spacer(Modifier.weight(1f))
        // Send
        Button(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            enabled = amount.isNotEmpty() && address.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(disabledBackgroundColor = Color.Gray),
            onClick = {
                val walletSDK = WalletSDK(context, web3RPC = selectedNetwork.chainRPC)
                val ensName = ENSName(address)
                if (ensName.isPotentialENSDomain()) {
                    val completableFuture = CompletableFuture<String>()
                    CompletableFuture.runAsync {
                        val ens = ENS(HttpEthereumRPC(selectedNetwork.chainRPC))
                        completableFuture.complete(ens.getAddress(ensName)?.hex.toString())
                    }
                    ensToAddress = completableFuture.get()
                }
                walletSDK.sendTransaction(
                    to = ensToAddress,
                    value = BigDecimal(amount.filterNot { it.isWhitespace() }).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(),
                    data = "",
                    chainId = selectedNetwork.chainId
                ).whenComplete { txHash, throwable ->
                    println("Send transaction result: $txHash")
                    // Open tx in etherscan
                    if (txHash != "decline") {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(selectedNetwork.chainExplorer + "/tx/" + txHash)
                        context.startActivity(intent)
                    }
                }
            }
        ) {
            Text(
                text = "SEND",
                fontSize = 20.sp,
                color = Color.White
            )
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
    Dialog(onDismissRequest = { setShowDialog() }) {
        AndroidView(
            modifier = Modifier,
            factory = { compoundBarcodeView },
        )
    }
}
/**
@ExperimentalComposeUiApi
@Composable
@Preview
fun PreviewMintingScreen() {
    WalletManagerTheme {
        val mainnetNetwork = Network(
            chainName = "Mainnet",
            chainId = 1,
            chainRPC = "https://cloudflare-eth.com",
            chainExplorer = "https://etherscan.io",
            chainCurrency = "ETH",
        )
        // Create State<Network> object with mainnetNetwork
        val selectedNetwork = object : State<Network> {
            override val value: Network
                get() = mainnetNetwork
        }
        SendScreen(selectedNetworkState = selectedNetwork)
    }
}
        */