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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.core.data.util.chainToApiKey
import com.core.database.model.RawContract
import com.core.model.SendData
import com.core.model.TokenAsset
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.core.ui.ethOSButton
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.NetworkPickerSheet
import com.core.ui.SelectedNetworkButton
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
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.lang.NullPointerException
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.util.Currency
import com.core.database.model.TransferEntity
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.Random


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String = "",
    viewModel: SendViewModel = hiltViewModel()
) {

    val initialAddress = initialAddress

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

                    //TODO: add Transfer into db
                    try {
                        viewModel.insertPendingTransfer(
                            transfer = TransferEntity(
                                uniqueId = (0..100).random().toString(),
                                asset = "",
                                chainId = chainid,
                                blockNum = (100..200).random().toString(),
                                category =  "external",
                                erc1155Metadata = emptyList(),
                                erc721TokenId = "",
                                from = userAddress,
                                hash =  (200..300).random().toString(),
                                rawContract = RawContract(
                                    address = "",
                                    decimal = "",
                                    value = ""
                                ),
                                to = address,
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

                    (context as Activity).runOnUiThread {

                        Toast.makeText(context, "Pending tx sent.", Toast.LENGTH_LONG).show()
                    }


                    viewModel.changeTxComplete()



//                    val web3jInstance = Web3j.build(HttpService(rpcurl))
//
//                    val wallet = WalletSDK(
//                        context = context,
//                        web3jInstance = web3jInstance
//                    )
//
//                    if(wallet.getChainId() != chainid){
//                        wallet.changeChain(chainid,rpcurl)
//                    }

//                    // TODO: Find out why polygon transaction is underpriced
//                    var gasPrice = web3jInstance.ethGasPrice().send().gasPrice
//                    gasPrice = gasPrice.add(gasPrice.multiply(BigInteger.valueOf(4)).divide(BigInteger.valueOf(100)))


//                    var res = try {
//                         wallet.sendTransaction(
//                            to = address,
//                            value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toBigInteger().toString(), // 1 eth in wei
//                            data = "",
//                            gasPrice = gasPrice.toString()
//                         )
//                    } catch (exception: NullPointerException) {
//                        "error"
//                    }

                    //Log.e("Test",res)
                    //Toast.makeText(context, "swipebutton", Toast.LENGTH_LONG).show()
                    //println(res)
//                    if(res != "decline" && res != "error"){
//                        //onBackClick()
//                        (context as Activity).runOnUiThread {
//                            //TODO: Snackbar
//                            Toast.makeText(context, "Successfully sent tx.", Toast.LENGTH_LONG).show()
//                        }
//                        //TODO: put mock transfer into db
//                        (context as Activity).runOnUiThread {
//                            Toast.makeText(context, "Sending TransferEntity: ", Toast.LENGTH_LONG).show()
//
//                        }
//                        viewModel.insertPendingTransfer(
//                            transfer = TransferEntity(
//                                uniqueId = res,
//                                asset = "",
//                                chainId = chainid,
//                                blockNum = res,
//                                category =  "",
//                                erc1155Metadata = emptyList(),
//                                erc721TokenId = "",
//                                from = userAddress,
//                                hash =  res,
//                                rawContract = RawContract(
//                                    address = "",
//                                    decimal = "",
//                                    value = ""
//                                ),
//                                to = address,
//                                tokenId =  "",
//                                value = BigDecimal(amount.replace(",",".").replace(" ","")).times(BigDecimal.TEN.pow(18)).toDouble(), // 1 eth in wei
//                                blockTimestamp = Clock.System.now(),
//                                userIsSender =  true,
//                                ispending = true
//
//                            )
//                        )
//
//                        viewModel.changeTxComplete()
//                        //Log.e("complete","$txComplete")
//
//                    }
//                    if (res == "error") {
//                        (context as Activity).runOnUiThread {
//                            Toast.makeText(context, "Sending tx failed.", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    if (res != "decline" && res != "error" && !res.startsWith("0x")) {
//                        (context as Activity).runOnUiThread {
//                            Toast.makeText(context, "Sending tx failed: $res", Toast.LENGTH_LONG).show()
//                        }
//                    }



                }




//                    viewModel.send(
//                        to = address,
//                        chainId = chainid,
//                        amount = amount
//                    )
//                }

            } else {
                (context as Activity).runOnUiThread {
                    Toast.makeText(context, "No internet connection found", Toast.LENGTH_LONG).show()
                }
            }

            //onBackClick()
        },
        //userAddress = userAddress,
        //toAddress = toAddress,
        txComplete = txComplete,
        selectedToken = selectedToken
//        onAddressClick = {
//            copyTextToClipboard(localContext, userAddress)
//        }
    )

    //SendScreen()


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
    //toAddress: String,
    //onAddressClick: () -> Unit,
) {

    var address by remember { mutableStateOf(initialAddress) }
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

    val maxFontSize = 68.sp
    val minFontSize = 42.sp
    val characterCount = value.length
    amountfontSize =
        when {
        characterCount <= 2 -> 68.sp
        characterCount > 2 && characterCount <= 4 -> 64.sp
        characterCount > 4 && characterCount <= 6 -> 54.sp
            characterCount > 4 && characterCount <= 6 -> 50.sp
            characterCount > 6 && characterCount <= 9 -> 48.sp
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
            title = "Send crypto",
            text = "Here you can send crypto to any address or ENS domain."
        )
    }

    when (txComplete) {
        is TxCompleteUiState.UnComplete -> {

        }
        is TxCompleteUiState.Complete -> {
            onBackClick()
        }
    }



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



                // Creating a values and variables to remember
                // focus requester, manager and state
                val focusRequester = remember { FocusRequester() }
                val focusManager = LocalFocusManager.current

                    //Textfield
                    Row(
                        verticalAlignment = Alignment.Bottom,
                    ){

                        ethOSTextField(
                            text = address,
                            label = "Address or ENS",
                            focusRequester = focusRequester,
                            onTextChanged = {
                                address = it.lowercase()
                                if (address.endsWith(".eth")) {
                                    if (ENSName(address).isPotentialENSDomain()) {
                                        // It is ENS
                                        CompletableFuture.runAsync {
                                            val ens = ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
                                            val ensAddr = ens.getAddress(ENSName(address))
                                            address = ensAddr?.hex.toString()
                                            validSendAddress = true
                                            focusManager.clearFocus()
                                        }
                                    } else {
                                        if (address.isNotEmpty()) validSendAddress = WalletUtils.isValidAddress(address)
                                        focusManager.clearFocus()
                                    }
                                    if(address.isEmpty()) {
                                        validSendAddress = false
                                        focusManager.clearFocus()
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
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                            },
                            modifier = Modifier
                                .weight(1f)
//                                .onFocusChanged {
//
//                                    if (ENSName(address).isPotentialENSDomain()) {
//                                        // It is ENS
//                                        CompletableFuture.runAsync {
//                                            val ens =
//                                                ENS(HttpEthereumRPC("https://cloudflare-eth.com"))
//                                            val ensAddr = ens.getAddress(ENSName(address))
//                                            address = ensAddr?.hex.toString()
//                                        }
//                                        focusManager.clearFocus()
//                                    } else {
//                                        if (address.isNotEmpty()) validSendAddress =
//                                            isValidAddress(address)
//                                    }
//                                    if (address.isEmpty()) validSendAddress = true
//
//                                }
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
                                            text = "",//"${walletAmount.ethAmount} ETH",
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
                                            text = "Available: ${formatDouble(selectedToken.tokenAsset.balance)} "+ if(selectedToken.tokenAsset.chainId == 137) "MATIC" else "ETH",//"${walletAmount.ethAmount} ETH",
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




                                            //Log.e("currenccy",currencyPrice)
                                        if(selectedToken.tokenAsset.chainId != 5){
                                            Text(
                                                text = if (value == "" || value == "0." || currencyPrice=="" ) {
                                                    "$${"0.0".toFloat() * "0.0".toFloat()}"
                                                } else {
                                                    "$${formatDouble((value.toFloat() * currencyPrice.toFloat()).toDouble())}"
                                                },//"$${value.toFloat()}",
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp,
                                                color = Color(0xFF9FA2A5)
                                            )
                                        }else{
                                            Text(
                                                text = "$0.0",//$${value.toFloat()},
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 16.sp,
                                                color = Color(0xFF9FA2A5)
                                            )
                                        }



                                    }

                                    else -> {}
                                }
                                //max amount



                            }

                            //select network
                            //var id by remember {mutableStateOf(1) }
                            //different network -> different color
                            when (selectedToken) {
                                is SelectedTokenUiState.Unselected -> {
                                    SelectedNetworkButton(
                                        chainId = 1,
                                        onClickChange = {
                                            showSheet = true
                                            focusManager.clearFocus()
                                        },
                                    )
                                }
                                is SelectedTokenUiState.Selected -> {
                                    SelectedNetworkButton(
                                        chainId = selectedToken.tokenAsset.chainId,
                                        onClickChange = {
                                            showSheet = true
                                            focusManager.clearFocus()

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
                    if (selectedToken is SelectedTokenUiState.Unselected) {
                        // Select eth token as default
                        if (balances is AssetUiState.Success) {
                            try {
                                val ethMainnet = balances.assets.filter { it.address == "1" }[0]
                                onChangeAssetClicked(
                                    ethMainnet
                                )
                            } catch (
                                exception: IndexOutOfBoundsException
                            ) {
                                onChangeAssetClicked(
                                    TokenAsset(
                                        address = "1",
                                        balance = 0.0,
                                        name = "Ethereum",
                                        symbol = "ETH",
                                        chainId = 1
                                    )
                                )
                            }

                        }

                    }
                    var activeNumpad = when (selectedToken) {
                        is SelectedTokenUiState.Unselected -> {
                            false
                        }
                        is SelectedTokenUiState.Selected -> {
                            true
                        }
                    }
                    NumPad(
                            value = value,
                            modifier = Modifier,
                            onValueChange = {
                                value = it
                                focusManager.clearFocus()

                            },
                            enabled = activeNumpad
                        )
                        Spacer(modifier = Modifier.height(16.dp))







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
                            //var test = if(value.toDouble() > selectedToken.tokenAsset.balance && value != "") true else false
                            ethOSButton(
                                onClick = {

                                    sendTransaction(
                                            SendData(
                                                amount = value.toFloat(),
                                                address = address,
                                                chainId = selectedToken.tokenAsset.chainId
                                            )
                                        )


                                },
                                enabled = true,//value != "" && validSendAddress && (value.toDouble() <= selectedToken.tokenAsset.balance) && (value.toDouble() != 0.0),
                                text = "Send"
                            )
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



}

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
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