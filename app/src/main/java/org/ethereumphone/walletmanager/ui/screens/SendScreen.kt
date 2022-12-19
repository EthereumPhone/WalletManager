package org.ethereumphone.walletmanager.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Icon
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset.Companion.Unspecified
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.ethereumphone.walletmanager.activities.MainActivity
import org.ethereumphone.walletmanager.models.Network
import org.ethereumphone.walletmanager.theme.*
import org.ethereumphone.walletmanager.ui.components.ENSInputField
import org.ethereumphone.walletmanager.ui.components.InputField
import org.ethereumphone.walletmanager.utils.WalletSDK
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.EthereumRPC
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.utils.QRCodeAnalyzer

@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    selectedNetwork: State<Network>
) {
    SendScreen(
        modifier = modifier,
        selectedNetworkState = selectedNetwork
    )
}


@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    selectedNetworkState: State<Network>,
) {
    val amount = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val scanning = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val selectedNetwork = selectedNetworkState.value
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

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
    WalletManagerTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = modifier
                .height(LocalConfiguration.current.screenHeightDp.dp)
                .padding(40.dp)

        ) {
            Text(
                text = "Send ether",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth(),
                // Set color to be white
                color = md_theme_dark_onPrimary
            )
            if (scanning.value) {

                AndroidView(
                    factory = { context ->
                        val previewView = PreviewView(context)
                        val preview = Preview.Builder().build()
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                        imageAnalysis.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            QRCodeAnalyzer { result ->
                                result?.let { address.value = it }
                                scanning.value = false
                            }
                        )

                        try {
                            cameraProviderFuture.get().bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        return@AndroidView previewView
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(63.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ENSInputField(
                        value = address.value,
                        label = "To address/ENS",
                        readOnly = false,
                        singeLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight()
                    ) {
                        address.value = it
                    }
                    Button(
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = md_theme_light_primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        enabled = true,
                        onClick = {
                            scanning.value = true
                        }
                    ) {
                        // QR code button
                        Image(painterResource(R.drawable.qricon),"Qr code")

                    }
                }
                InputField(
                    value = "",
                    label = "Amount",
                    readOnly = false,
                    singeLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onlyNumbers = true
                ) {
                    amount.value = it
                }
                Button(
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = md_theme_light_primary),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = amount.value.isNotEmpty() && address.value.isNotEmpty(),
                    onClick = {
                        val walletSDK = WalletSDK(context, web3RPC = selectedNetwork.chainRPC)
                        val ensName = ENSName(address.value)
                        if (ensName.isPotentialENSDomain()) {
                            val completableFuture = CompletableFuture<String>()
                            CompletableFuture.runAsync {
                                val ens = ENS(HttpEthereumRPC(selectedNetwork.chainRPC))
                                completableFuture.complete(ens.getAddress(ensName)?.hex.toString())
                            }
                            address.value = completableFuture.get()
                        }
                        walletSDK.sendTransaction(
                            to = address.value,
                            value = BigDecimal(amount.value).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(),
                            data = "",
                            chainId = selectedNetwork.chainId
                        ).whenComplete { txHash, throwable ->
                            println("Send transaction result: $txHash")
                            // Open tx in etherscan
                            if (txHash != "decline") {
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = android.net.Uri.parse(selectedNetwork.chainExplorer + "tx/" + txHash)
                                context.startActivity(intent)
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Send",
                        color = if (amount.value.isNotEmpty() && address.value.isNotEmpty()) md_theme_light_onPrimary else md_theme_light_onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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