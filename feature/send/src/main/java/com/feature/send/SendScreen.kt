package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Network
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.AddressBar
import com.feature.send.ui.BottomSheet
import com.feature.send.ui.MockNetworkData
import com.feature.send.ui.NetworkPickerSheet
import com.feature.send.ui.SelectedNetworkButton
import com.feature.send.ui.SwipeButton
import com.feature.send.ui.TextToggleButton
import com.feature.send.ui.ethOSTextField
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import java.util.concurrent.CompletableFuture

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import org.web3j.crypto.WalletUtils.isValidAddress


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    //selectedNetwork: State<Network>,
    onBackClick: () -> Unit,
    viewModel: SendViewModel = hiltViewModel()
) {

    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val maxAmount by viewModel.maxAmount.collectAsStateWithLifecycle(initialValue = "")
    val balances by viewModel.networkBalanceState.collectAsStateWithLifecycle()

//    val neworkBalanceRepository by viewModel.networkBalanceInfo.getNetworksBalance()
//    val composableScope = rememberCoroutineScope()
//    composableScope.launch {
//        neworkBalanceRepository. { value ->
//            // Do something with the collected value
//            println("Collected value: $value")
//        }
//    }

    SendScreen(
        modifier = Modifier,
        onBackClick = onBackClick,
        balances = balances,
        //userAddress = userAddress,
//        onAddressClick = {
//            copyTextToClipboard(localContext, userAddress)
//        }
    )

    //SendScreen()


}
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    balances:  AssetUiState
    //userAddress: String,

    //onAddressClick: () -> Unit,
) {

    var address by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    //var showDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var validSendAddress by remember { mutableStateOf(true) }



    val context = LocalContext.current
    //val selectedNetwork = selectedNetworkState.value


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

    var amountfontSize by remember { mutableStateOf(68.sp) }

    val maxFontSize = 68.sp
    val minFontSize = 42.sp
    val characterCount = value.length
    amountfontSize =
        when {
        characterCount <= 2 -> 68.sp
        characterCount > 2 && characterCount <= 4 -> 60.sp
        characterCount > 4 && characterCount <= 6 -> 52.sp
            characterCount > 4 && characterCount <= 6 -> 48.sp
            characterCount > 6 && characterCount <= 9 -> 44.sp
        //characterCount > 5 && characterCount <= 10 -> 18.sp
        else -> minFontSize
    }






    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    val showDialog =  remember { mutableStateOf(false) }

    if(showDialog.value)
        qrCodeDialog(
            context = context,
            setShowDialog = {
            showDialog.value = false
        }) {
            Log.i("HomePage","HomePage : $it")
        }

    val showInfoDialog =  remember { mutableStateOf(false) }
    if(showInfoDialog.value){
        InfoDialog(
            setShowDialog = {
                showInfoDialog.value = false
            },
            title = "Info",
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
        )
    }

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Create a BottomSheetScaffoldState
    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)




    Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E2730))
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ){
            //Breadcrumb w/ backbutton
            TopHeader(
                onBackClick = onBackClick,
                title = "Send",
                icon = {
                    IconButton(
                        onClick = {
                            //opens InfoDialog
                            showInfoDialog.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Information",
                            tint = Color(0xFF9FA2A5),
                            modifier = modifier
                                .clip(CircleShape)

                        )
                    }
                }
            )
            Spacer(modifier = modifier.height(12.dp))

            Column (
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                //AddressBar("0x123123123123123123",onclick={}, network = "Mainnet")//userAddress,onclick={}, network = "Mainnet")//Addressclick



                    //Textfield
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ){

                        ethOSTextField(
                            text = address,
                            label = "Address or ENS",
                            onTextChanged = {

                                address = it.lowercase()
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
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDialog.value = true },

                                    ) {

                                    Icon(
                                        imageVector = Icons.Default.QrCode,
                                        contentDescription = "Address by QR",
                                        tint = if(validSendAddress) Color.White else Color.Red,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {

                                    if (ENSName(address).isPotentialENSDomain()) {
                                        // It is ENS
                                        CompletableFuture.runAsync {
                                            val ens =
                                                ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                                            val ensAddr = ens.getAddress(ENSName(address))
                                            address = ensAddr?.hex.toString()
                                        }
                                    } else {
                                        if (address.isNotEmpty()) validSendAddress =
                                            isValidAddress(address)
                                    }
                                    if (address.isEmpty()) validSendAddress = true

                                }
                        )
                    }
                    Spacer(modifier = Modifier
                        .height(16.dp)
                    )

                        //Amount Section
                        Column (
                            modifier = modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            //Max Amount Section
                            Column (
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val maxed = remember { mutableStateOf(false) }
                                TextToggleButton(text = "MAX", selected = maxed, onClickChange = {
                                    maxed.value = !maxed.value
                                })
                                Text(
                                    text = "Available: 10.13235 ETH",//"${walletAmount.ethAmount} ETH",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = Color(0xFF9FA2A5)
                                )
                            }

                            //Amount
                            Column (horizontalAlignment = Alignment.CenterHorizontally){
                                Text(
                                    text = //"0.0 ETH",
                                    if (value == "") {
                                        "0.0 ETH"
                                    } else {
                                        // If value has more than 7 decimals, show the string with less decimals
                                        if (value.contains(".")) {
                                            val decimals = value.split(".")[1]
                                            if (decimals.length > 5) {
                                                value.split(".")[0] + "." + decimals.substring(0, 5)
                                            } else {
                                                value
                                            }
                                        } else {
                                            value
                                        } + " ETH"
                                    },

                                    color = if (value == "") {
                                        Color.White
                                    } else {
                                        Color.White
                                        //if (value.toFloat() <= walletAmount.ethAmount.toFloat()) Color.White else Color.Red
                                    },//"${walletAmount.ethAmount} ETH",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = amountfontSize,
                                    textAlign = TextAlign.Center
                                )
                                //max amount
                                Text(
                                    text = "$ 18700.27",//"${walletAmount.ethAmount} ETH",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = Color(0xFF9FA2A5)
                                )

                            }

                            //select network
                            var id by remember {mutableStateOf(1) }
                            //different network -> different color
                            SelectedNetworkButton(
                                chainId = id,
                                onClickChange = {
                                    showSheet = true

                                }
                            )



                        }


                Spacer(modifier = Modifier
                    .height(16.dp)
                )
                //Input Section
                Column (horizontalAlignment = Alignment.CenterHorizontally){
                        NumPad(
                            value = value,
                            modifier = Modifier,
                            onValueChange = {
                                value = it
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SwipeButton(
                            text = "Swipe to send",
                            icon = Icons.Rounded.ArrowForward,
                            completeIcon = Icons.Rounded.Check,
                        ){}
                    }

            }

            if(showSheet) {
                ModalBottomSheet(
                    containerColor= Color(0xFF24303D),
                    contentColor= Color.White,

                    onDismissRequest = {
                        coroutineScope.launch {
                            modalSheetState.hide()
                        }.invokeOnCompletion {
                            if(!modalSheetState.isVisible) showSheet = false
                        }
                    },
                    sheetState = modalSheetState
                ) {

                    NetworkPickerSheet(
                        balancesState = balances,
                        onSelectAsset = {
                            //onSelectAsset(it)

                            //hides ModelBottomSheet
                            coroutineScope.launch {
                                modalSheetState.hide()
                            }.invokeOnCompletion {
                                if(!modalSheetState.isVisible) showSheet = false
                            }
                        }
                    )
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
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF24303D),
            contentColor = Color.White,
            tonalElevation = 4.dp
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),

                    horizontalAlignment = Alignment.CenterHorizontally

                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "",
                            tint = Color.Transparent,
                            modifier = Modifier
//                                .width(30.dp)
//                                .height(30.dp)

                        )
                        Text(
                            text = "Scan QR Code",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp),
                            onClick = {
                                setShowDialog()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF9FA2A5),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(36.dp))

                    //TODO: QRCode
                    Box (
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .height(400.dp)
                            .width(300.dp),
                        contentAlignment = Alignment.Center
                    ){
                        AndroidView(
                            modifier = Modifier,
                            factory = { compoundBarcodeView },
                        )
                    }

                }
            }
        }
    }
}



@Preview
@Composable
fun PreviewSendScreen() {
    //endScreen()
}