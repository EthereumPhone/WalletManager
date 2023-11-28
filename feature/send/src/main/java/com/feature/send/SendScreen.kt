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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.data.util.chainToApiKey
import com.core.database.model.RawContract
import com.core.database.model.TransferEntity
import com.core.model.SendData
import com.core.model.TokenAsset
import com.core.ui.InfoDialog
import com.core.ui.SelectedNetworkButton
import com.core.ui.TopHeader
import com.core.ui.ethOSButton

import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.CompoundBarcodeView

import org.ethereumphone.walletsdk.WalletSDK
import java.text.DecimalFormat
import com.core.ui.ethOSTextField
import com.core.ui.ethOSCenterTextField
import com.feature.send.ui.NetworkPickerSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import kotlin.math.max


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String = "",
    viewModel: SendViewModel = hiltViewModel()
) {

    var initialAddress = initialAddress

    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val maxAmount by viewModel.maxAmount.collectAsStateWithLifecycle(initialValue = "")
    val balances by viewModel.networkBalanceState.collectAsStateWithLifecycle()

    //val toAddress by viewModel.toAddress.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val walletSDK = WalletSDK(context)
    val coroutineScope = rememberCoroutineScope()
    val txComplete by viewModel.txComplete.collectAsStateWithLifecycle()

    val currencyprice by viewModel.exchange.collectAsStateWithLifecycle("")




    SendScreen(
        modifier = Modifier,
        onBackClick = onBackClick,
        initialAddress = initialAddress,
        balances = balances,
        currencyPrice = currencyprice,
        onChangeAssetClicked = viewModel::changeSelectedAsset,
        onToAddressChanged= viewModel::changeToAddress,
        onCurrencyChange = viewModel::getExchange,
        sendTransaction = { sendData ->

            val amount = sendData.amount.toString()
            var address = sendData.address
            val chainid = sendData.chainId
            //TODO: Replace with actual method to get rpcUrl
            val chainName = when(chainid) {
                1 -> "eth-mainnet"
                5 -> "eth-goerli"
                10 -> "opt-mainnet"
                137 -> "polygon-mainnet"
                42161 -> "arb-mainnet"
                else -> "eth-mainnet"
            }

            var rpcurl = "https://${chainName}.g.alchemy.com/v2/${chainToApiKey(chainName)}"

            if (chainid == 137) {
                rpcurl = "https://rpc.ankr.com/polygon"
            }

            println("RPC: $rpcurl")

            Log.e("not complete","$txComplete")
            // Check if phone is connected to internet using NetworkManager
            val connectivityManager = ContextCompat.getSystemService(context, android.net.ConnectivityManager::class.java)
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val hasInternet = capabilities != null && capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)

            if(hasInternet) {


                CoroutineScope(Dispatchers.IO).launch {



                    val web3jInstance = Web3j.build(HttpService(rpcurl))

                    val wallet = WalletSDK(
                        context = context,
                        web3jInstance = web3jInstance
                    )

                    if(wallet.getChainId() != chainid){
                        wallet.changeChain(chainid,rpcurl)
                    }

                    // TODO: Find out why polygon transaction is underpriced
                    var gasPrice = web3jInstance.ethGasPrice().send().gasPrice
                    gasPrice = gasPrice.add(gasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))


                    var res = try {
                         wallet.sendTransaction(
                            to = address,
                            value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(), // 1 eth in wei
                            data = "",
                            gasPrice = gasPrice.toString()
                         )
                    } catch (exception: NullPointerException) {
                        "error"
                    }

                    Log.e("Test",res)
                    //Toast.makeText(context, "swipebutton", Toast.LENGTH_LONG).show()
                    println(res)
                    if(res != "decline" && res != "error"){
                        //onBackClick()
                        (context as Activity).runOnUiThread {
                            //TODO: Snackbar
                            Toast.makeText(context, "Successfully sent tx.", Toast.LENGTH_LONG).show()
                        }

                        //TODO: put pending transfer into db
                        try {
                            Log.d("insert tx", "itas in ")
                            viewModel.insertPendingTransfer(
                                transfer = TransferEntity(
                                    uniqueId = (0..100).random().toString(),
                                    asset = "",
                                    chainId = chainid,
                                    blockNum = (100..200).random().toString(),
                                    category =  "external",
                                    erc1155Metadata = emptyList(),
                                    erc721TokenId = "",
                                    fromaddress = userAddress,
                                    hash =  (200..300).random().toString(),
                                    rawContract = RawContract(
                                        address = "",
                                        decimal = "",
                                        value = ""
                                    ),
                                    toaddress = address,
                                    tokenId =  "",
                                    value = amount.toDouble(),//BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toDouble(), // 1 eth in wei
                                    blockTimestamp = Clock.System.now(),
                                    userIsSender =  true,
                                    ispending = true

                                )
                            )

                        }catch (exception: NullPointerException) {
//                        "error"
                        }




                        viewModel.changeTxComplete()

                    }
                    if (res == "error") {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Sending tx failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    if (res != "decline" && res != "error" && !res.startsWith("0x")) {
                        (context as Activity).runOnUiThread {
                            Toast.makeText(context, "Sending tx failed: $res", Toast.LENGTH_LONG).show()
                        }
                    }



                }





            } else {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
                }
            }
        },
        txComplete = txComplete,
        selectedToken = selectedToken
    )



}
@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String,
    balances:  AssetUiState,
    currencyPrice: String,
    onChangeAssetClicked: (TokenAsset) -> Unit,
    onToAddressChanged: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    sendTransaction: (SendData) -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState

) {

    var address by remember { mutableStateOf("")}//initialAddress) }
    if (ENSName(initialAddress).isPotentialENSDomain()) {
        CompletableFuture.runAsync {
            var rpcurl = "https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}"
            val ens = ENS(HttpEthereumRPC(rpcurl))
            val ensAddr = ens.getAddress(ENSName(initialAddress))
            address = ensAddr?.hex.toString()
        }
    }
    var value by remember { mutableStateOf("") }
    //var showDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var validSendAddress by remember { mutableStateOf(false) }

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
    var enableButton by remember { mutableStateOf(false) }
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
//    if(showInfoDialog.value){
//        InfoDialog(
//            setShowDialog = {
//                showInfoDialog.value = false
//            },
//            title = "Send crypto",
//            text = "Here you can send crypto to any address or ENS domain."
//        )
//    }

    when (txComplete) {
        is TxCompleteUiState.UnComplete -> {

        }
        is TxCompleteUiState.Complete -> {
            onBackClick()
        }
    }



    Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 32.dp, vertical = 32.dp)
        ){
            //Breadcrumb w/ backbutton
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopHeader(
                onBackClick = onBackClick,
                title = "Send",
                icon = {
                    IconButton(
                        onClick = {
                            //opens InfoDialog
                            showDialog.value = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = "QR Code",
                            tint = Color.White,//(0xFF9FA2A5),
                            modifier = modifier
                                .clip(CircleShape)
                                .size(32.dp)

                        )
                    }
                }
            )
            Column(
                horizontalAlignment =  Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(250.dp)


            ) {
                Text(
                    text = "To",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9FA2A5),
                    fontSize = 20.sp
                )
                ethOSCenterTextField(
                    text = address,
                    label = "(Enter address, ENS or QR scan)",
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    onTextChanged = { text -> address = text },
                    size = 16
                )
            }
        }





        Column(
            horizontalAlignment =  Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()


        ) {
            when (selectedToken) {
                is SelectedTokenUiState.Unselected -> {
                    Row(
                    ){

                        Row (
                            modifier = Modifier.widthIn(0.dp,300.dp),
                            horizontalArrangement = Arrangement.Center
                        ){

                            ethOSTextField(
                                text = value,
                                label = "0",
                                singleLine = true,
                                onTextChanged = { text -> value = text },
                                size = 64,
                                maxChar = 10,
                                numberInput = true

                            )
                            Spacer(modifier = Modifier.width(12.dp))



                            Text(
                                text = "ETH",
                                fontSize = calculateFontSize(value.length,64),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(IntrinsicSize.Min)
                            )
                        }

                    }
                    Text(
                        text = "0 ETH available",
                        fontSize = 20.sp,
                        color = Color(0xFF9FA2A5),
                        fontWeight = FontWeight.SemiBold
                    )


                }
                is SelectedTokenUiState.Selected -> {
                    Row(
                    ){

                        Row (
                            modifier = Modifier.widthIn(0.dp,300.dp),
                            horizontalArrangement = Arrangement.Center
                        ){

                                        ethOSTextField(
                                            text = value,
                                            label = "0",
                                            singleLine = true,
                                            onTextChanged = { text -> value = text },
                                            size = 64,
                                            maxChar = 10

                                        )
                            Spacer(modifier = Modifier.width(12.dp))



                                Text(
                                    text = if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH",
                                    fontSize = calculateFontSize(value.length,64),
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(IntrinsicSize.Min)
                                )
                        }

                    }
                    Text(
                        text = "${value.length} ${if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH"} available",
                        fontSize = 20.sp,
                        color = Color(0xFF9FA2A5),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }



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



        when (selectedToken) {
            is SelectedTokenUiState.Unselected -> {

                ethOSButton(
                    onClick = {
                    },
                    enabled = false,
                    text = "Send"
                )
            }

            is SelectedTokenUiState.Selected -> {
                ethOSButton(
                    text = "Send",
                    enabled = true,
                    onClick = {
                        sendTransaction(
                            SendData(
                                amount = value.toFloat(),
                                address = address,
                                chainId = selectedToken.tokenAsset.chainId
                            )
                        )
                    }
                )
            }

        }











        }



    if(showSheet) {
        ModalBottomSheet(
            containerColor= Color(0xFF1A1A1A),
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
                    var currencychange = when(it.chainId){
                        137 -> "MATICUSDT"
                        else -> "ETHUSDT"
                    }
                    onCurrencyChange(currencychange)

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




fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}

private fun getBalance(assetsUiState: AssetUiState, chainid: Int): TokenAsset {
    lateinit var info: TokenAsset


    return info
}



fun calculateFontSize(length: Int, defaultSize: Int ): TextUnit {
    val defaultFontSize = defaultSize.sp
    val maxChars = 2
    val scaleFactor = 0.8

    var adjustedSize = if (length > maxChars) {
        val scaledSize = (defaultFontSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        result.sp
    } else {
        defaultFontSize
    }

    if (length > maxChars*2) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        Log.e("Length: ", "${ adjustedSize }")
    }

    if (length > maxChars*3) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        Log.e("Length: ", "${ adjustedSize }")
    }

//    if (length > (maxChars*2) - 15) {
//        val scaledSize = (adjustedSize * scaleFactor).value
//        val minValue = 12f // Minimum font size
//        val result = max(scaledSize, minValue)
//        adjustedSize = result.sp
//    }
    return adjustedSize
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
            color = Color.Black,
            border= BorderStroke(1.dp, Color.White),
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
//    SendScreen()
}