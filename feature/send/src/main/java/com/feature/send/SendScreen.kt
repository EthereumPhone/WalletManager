package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.core.model.SendData
import com.core.model.TokenAsset
import com.core.ui.InfoDialog
import com.core.ui.SwipeButton
import com.core.ui.TopHeader
import com.core.ui.ethOSButton
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.NetworkPickerSheet
import com.feature.send.ui.SelectedNetworkButton
import com.feature.send.ui.TextToggleButton
import com.feature.send.ui.ethOSTextField
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.CompletableFuture

import kotlinx.coroutines.launch
import org.ethereumphone.walletsdk.WalletSDK
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import org.web3j.crypto.WalletUtils.isValidAddress
import java.math.BigDecimal


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

    //val toAddress by viewModel.toAddress.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val walletSDK = WalletSDK(context)
    val coroutineScope = rememberCoroutineScope()

//    val neworkBalanceRepository by viewModel.networkBalanceInfo.getNetworksBalance()
//    val composableScope = rememberCoroutineScope()
//    composableScope.launch {
//        neworkBalanceRepository. { value ->
//            // Do something with the collected value
//            println("Collected value: $value")
//        }
//    }

    //val tmp =  userAddress
    SendScreen(
        modifier = Modifier,
        onBackClick = onBackClick,
        balances = balances,
        onChangeAssetClicked = viewModel::changeSelectedAsset,
        onToAddressChanged= viewModel::changeToAddress,
        sendTransaction = { sendData ->

            val amount = sendData.amount.toString()
            var address = sendData.address


            // Check if phone is connected to internet using NetworkManager
            val connectivityManager = ContextCompat.getSystemService(context, android.net.ConnectivityManager::class.java)
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val hasInternet = capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if(hasInternet) {
                CoroutineScope(Dispatchers.IO).launch {
//
                    val wallet = WalletSDK(
                    context = context,
                    )

                    val res = wallet.sendTransaction(
                        to = address,
                        value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(), // 1 eth in wei
                        data = ""
                    )
                    //Toast.makeText(context, "swipebutton", Toast.LENGTH_LONG).show()
                    //println(res)
                    Log.e("Test",res)
                }


//                val walletSDK = WalletSDK(context)
//                val ensName = ENSName(address)
//                if (ensName.isPotentialENSDomain()) {
//                    val completableFuture = CompletableFuture<String>()
//                    CompletableFuture.runAsync {
//                        val ens = ENS(HttpEthereumRPC(walletManagerState.network.value.chainRPC))
//                        completableFuture.complete(ens.getAddress(ensName)?.hex.toString())
//                    }
//                    address = completableFuture.get()
//                }
//                walletSDK.sendTransaction(
//                    to = address,
//                    value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(),
//                    data = "",
//                    gasPrice = null,
//                ).whenComplete { txHash, throwable ->
//                    println("Send transaction result: $txHash")
//                    // Open tx in etherscan
//                    if (txHash != "decline") {
//                        val intent = Intent(Intent.ACTION_VIEW)
//                        intent.data = android.net.Uri.parse(walletManagerState.network.value.chainExplorer + "/tx/" + txHash)
//                        context.startActivity(intent)
//                    }
//                }
            } else {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
                }
            }
        },
        //userAddress = userAddress,
        //toAddress = toAddress,
        selectedToken = selectedToken
//        onAddressClick = {
//            copyTextToClipboard(localContext, userAddress)
//        }
    )

    //SendScreen()


}
@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    balances:  AssetUiState,
    onChangeAssetClicked: (TokenAsset) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (SendData) -> Unit,
    selectedToken: SelectedTokenUiState,
    //toAddress: String,
    //onAddressClick: () -> Unit,
) {

    var address by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    //var showDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var validSendAddress by remember { mutableStateOf(true) }

    var tokeninfo by remember { mutableStateOf(TokenAsset("",0,"","",7.0)) }

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Create a BottomSheetScaffoldState
    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)

    //initalize values







    var test by remember { mutableStateOf(tokeninfo) }


    var maxAmount by remember { mutableStateOf(test.balance) }
    var prevAmount by remember { mutableStateOf(value) }
    var enableButton by remember { mutableStateOf(true) }
    //var amountColor by remember { mutableStateOf(Color.White) }
    var network by remember { mutableStateOf(test.chainId) }










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



    //Values




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

                                onToAddressChanged(address)

                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showDialog.value = true },

                                    ) {

                                    Icon(
                                        imageVector = Icons.Rounded.QrCode,
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

                                when (selectedToken) {
                                    is SelectedTokenUiState.Unselected -> {
                                        val maxed = remember { mutableStateOf(false) }
                                        TextToggleButton(text = "MAX", selected = maxed, onClickChange = {
                                        }, enabled = false)

                                        Text(
                                            text = "-",//"${walletAmount.ethAmount} ETH",
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            color = Color(0xFF9FA2A5)
                                        )
                                    }
                                    is SelectedTokenUiState.Selected -> {

                                        val maxed = remember { mutableStateOf(false) }
                                        TextToggleButton(text = "MAX", selected = maxed, onClickChange = {
                                            maxed.value = !maxed.value

                                            //If max
                                            if(maxed.value){
                                                prevAmount = value // save previous amount
                                                value = selectedToken.tokenAsset.balance.toString() // replace maxAmount with current value
                                                enableButton = false
                                            }else{//if switches bck
                                                value = prevAmount // replace value w/ maxAmount w/ prevAmount
                                                enableButton = true
                                            }
                                        }, enabled = true)

                                        Text(
                                            text = "Available: ${selectedToken.tokenAsset.balance} "+ if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH",//"${walletAmount.ethAmount} ETH",
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 16.sp,
                                            color = Color(0xFF9FA2A5)
                                        )
                                    }
                                }


                            }

                            //Amount
                            Column (horizontalAlignment = Alignment.CenterHorizontally){

                                when (selectedToken) {
                                    is SelectedTokenUiState.Unselected -> {
                                        Text(
                                            text = "0.0 ETH"
                                             ,
                                            //check if value over maxamount

                                            color = Color(0xFF9FA2A5),//"${walletAmount.ethAmount} ETH",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = amountfontSize,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    is SelectedTokenUiState.Selected -> {
                                        Text(
                                            text = //"0.0 ETH",
                                            if (value == "") {
                                                "0.0 "+ if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH"
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
                                                } + " " + if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH"
                                            },
                                            //check if value over maxamount

                                            color = if (value == "") {
                                                Color.White
                                            } else {
                                                if(value.toDouble() > selectedToken.tokenAsset.balance){
                                                    Color.Red
                                                }else{
                                                    Color.White
                                                }
                                                //if (value.toFloat() <= walletAmount.ethAmount.toFloat()) Color.White else Color.Red
                                            },//"${walletAmount.ethAmount} ETH",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = amountfontSize,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    else -> {}
                                }
                                //max amount
                                Text(
                                    text = "$ 18700.27",//"${walletAmount.ethAmount} ETH",
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = Color(0xFF9FA2A5)
                                )

                            }

                            //select network
                            //var id by remember {mutableStateOf(1) }
                            //different network -> different color
                            when (selectedToken) {
                                is SelectedTokenUiState.Unselected -> {
                                    SelectedNetworkButton(
                                        chainId = 0,
                                        onClickChange = {
                                            showSheet = true

                                        },

                                        )
                                }
                                is SelectedTokenUiState.Selected -> {
                                    SelectedNetworkButton(
                                        chainId = selectedToken.tokenAsset.chainId,
                                        onClickChange = {
                                            showSheet = true

                                        },

                                        )
                                }

                                else -> {}
                            }




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

                            },
                            enabled = enableButton
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                    var swipeableState = rememberSwipeableState(initialValue = 0)
                    val (swipeComplete, setSwipeComplete) = remember {
                        mutableStateOf(false)
                    }
                    var result by  remember { mutableStateOf("") }



                    when (selectedToken) {
                        is SelectedTokenUiState.Unselected -> {
//                            SwipeButton(
//                                text = "Swipe to send",
//                                icon = Icons.Rounded.ArrowForward,
//                                completeIcon = Icons.Rounded.Check,
//                                enabled = false,
//                                onSwipe =  {},
//                                swipeableState = swipeableState,
//                                swipeComplete = swipeComplete,
//                                setSwipeComplete = setSwipeComplete
//
//                            )
                            ethOSButton(
                                onClick = {
                                },
                                enabled = false,
                                text = "Send"
                            )
                        }
                        is SelectedTokenUiState.Selected -> {
                            ethOSButton(
                                onClick = {
                                    sendTransaction(
                                            SendData(
                                                amount = value.toFloat(),
                                                address = address,
                                                selectedToken.tokenAsset.chainId
                                            )
                                        )

                                },
                                enabled = true,
                                text = "Send"
                            )
//                            SwipeButton(
//                                text = "Swipe to send",
//                                icon = Icons.Rounded.ArrowForward,
//                                completeIcon = Icons.Rounded.Check,
//                                enabled = true,
//                                onSwipe =  {
//                                    //send Transaction
//                                    if (value == "") {
//                                        val amount = "0.0".toFloat().toString()
//                                        var address = address
//
//
//
//
//                                        // Check if phone is connected to internet using NetworkManager
//                                        val connectivityManager = ContextCompat.getSystemService(context, android.net.ConnectivityManager::class.java)
//                                        val activeNetwork = connectivityManager?.activeNetwork
//                                        val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
//                                        val hasInternet = capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
//
//                                        if(hasInternet) {
//                                            CoroutineScope(Dispatchers.IO).launch {
////
//                                                val wallet = WalletSDK(
//                                                    context = context,
//                                                )
//
//                                                val res = wallet.sendTransaction(
//                                                    to = address,
//                                                    value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(), // 1 eth in wei
//                                                    data = ""
//                                                )
//                                                //Toast.makeText(context, "swipebutton", Toast.LENGTH_LONG).show()
//                                                //println(res)
//                                                result = res
//                                                //Log.e("Test",res)
//                                            }
//
//                                        } else {
//                                            (context as Activity).runOnUiThread {
//                                                Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
//                                            }
//                                        }
////                                        sendTransaction(
////                                            SendData(
////                                                amount = "0.0".toFloat(),
////                                                address = address,
////                                                selectedToken.tokenAsset.chainId
////                                            )
////                                        )
//                                    }
//                                    else{
////                                        sendTransaction(
////                                            SendData(
////                                                amount = value.toFloat(),
////                                                address = address,
////                                                selectedToken.tokenAsset.chainId
////                                            )
////                                        )
//
//
//                                            val amount = value.toFloat().toString()
//                                            var address = address
//
//
//                                            // Check if phone is connected to internet using NetworkManager
//                                            val connectivityManager = ContextCompat.getSystemService(context, android.net.ConnectivityManager::class.java)
//                                            val activeNetwork = connectivityManager?.activeNetwork
//                                            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
//                                            val hasInternet = capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
//
//                                            if(hasInternet) {
//                                                CoroutineScope(Dispatchers.IO).launch {
////
//                                                    val wallet = WalletSDK(
//                                                        context = context,
//                                                    )
//
//                                                    val res = wallet.sendTransaction(
//                                                        to = address,
//                                                        value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(), // 1 eth in wei
//                                                        data = ""
//                                                    )
//                                                    //Toast.makeText(context, "swipebutton", Toast.LENGTH_LONG).show()
//                                                    //println(res)
//                                                    setSwipeComplete(false)
//                                                }
//
//                                            } else {
//                                                (context as Activity).runOnUiThread {
//                                                    Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
//                                                }
//                                            }
//
//                                    }
//
//                                },
//                                swipeableState = swipeableState,
//                                swipeComplete = swipeComplete,
//                                setSwipeComplete = setSwipeComplete
//
//                            )
                        }
                    }

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

                            onChangeAssetClicked(it)

                            //maxAmount = it.balance

//                            network = when(it.chainId) {
//                                1 -> "Mainnet"
//                                5 -> "Görli"
//                                10 -> "Optimism"
//                                137 -> "Polygon"
//                                42161 -> "Arbitrum"
//                                else -> ""
//                            }

                            //network =  it.chainId


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



private fun getBalance(assetsUiState: AssetUiState, chainid: Int): TokenAsset {
    lateinit var info: TokenAsset


    return info
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