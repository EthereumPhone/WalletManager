package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.data.model.dto.Contact
import com.core.data.util.chainToApiKey
import com.core.model.SendData
import com.core.model.TokenAsset
import com.core.ui.SelectedNetworkButton
import com.core.ui.TopHeader
import com.core.ui.ethOSButton
import org.ethereumphone.walletsdk.WalletSDK
import java.text.DecimalFormat
import com.core.ui.ethOSTextField
import com.core.ui.ethOSCenterTextField
import com.feature.send.ui.ContactPickerSheet
import com.feature.send.ui.ContactPill
import com.feature.send.ui.NetworkPickerSheet
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import kotlin.math.abs
import kotlin.math.max


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String = "",
    viewModel: SendViewModel = hiltViewModel()
) {
    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val maxAmount by viewModel.maxAmount.collectAsStateWithLifecycle(initialValue = "")
    val balances by viewModel.networkBalanceState.collectAsStateWithLifecycle()
    val toAddress by viewModel.toAddress.collectAsStateWithLifecycle(initialValue = initialAddress)
    val selectedToken by viewModel.selectedAsset.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val walletSDK = WalletSDK(context)
    val coroutineScope = rememberCoroutineScope()
    val txComplete by viewModel.txComplete.collectAsStateWithLifecycle()

    val userNetwork by viewModel.userNetwork.collectAsStateWithLifecycle()

    val currencyprice by viewModel.exchange.collectAsStateWithLifecycle("")


    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())



    SendScreen(
        modifier = Modifier,
        onBackClick = onBackClick,
        toAddress = toAddress,
        balances = balances,
        currencyPrice = currencyprice,
        onChangeAssetClicked = viewModel::changeSelectedAsset,
        onToAddressChanged= viewModel::changeToAddress,
        onCurrencyChange = {  },
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
        selectedToken = selectedToken,
        userNetwork = userNetwork,
        getContacts = viewModel::getContacts,
        contacts = contacts
    )
}
@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    toAddress: String,
    balances:  AssetUiState,
    currencyPrice: String,
    onChangeAssetClicked: (TokenAsset) -> Unit,
    onToAddressChanged: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    sendTransaction: (SendData) -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState,
    userNetwork: String,
    getContacts: (Context) -> Unit,
    contacts: List<Contact>
    //getContacts: (Context, (List<Contact>) -> Unit) -> Unit,
) {




    var validSendAddress by remember { mutableStateOf(false) }

    if (toAddress.endsWith(".eth")) {
        if (ENSName(toAddress.lowercase()).isPotentialENSDomain()) {
            // It is ENS
            println("Checking ENS")
            CompletableFuture.runAsync {
                val ens = ENS(HttpEthereumRPC("https://eth-mainnet.g.alchemy.com/v2/${chainToApiKey("eth-mainnet")}"))
                val ensAddr = ens.getAddress(ENSName(toAddress.lowercase()))
                onToAddressChanged(ensAddr?.hex.toString())
                validSendAddress = true
            }
        } else {
            if (toAddress.isNotEmpty()) validSendAddress = WalletUtils.isValidAddress(toAddress.lowercase())
        }
        if(toAddress.isEmpty()) {
            validSendAddress = false
        }
    }

    var value by remember { mutableStateOf("") }
    //var showDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    var tokeninfo by remember { mutableStateOf(TokenAsset("",0,"","",7.0)) }

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Create a BottomSheetScaffoldState
    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)

    //initalize values


    val context = LocalContext.current
    //val selectedNetwork = selectedNetworkState.value

    val barCodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if(result.contents == null) {

            } else {
                onToAddressChanged(result.contents)
            }
        }
    )

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if(isGranted) {
                hasCameraPermission = isGranted
            } else {
                //TODO: Add
            }
        }
    )

    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val requestContactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if(isGranted) {
                hasContactsPermission = isGranted
            } else {
                //TODO: Add
            }
        }
    )


    var selectedContact by remember { mutableStateOf(Contact())  }
    var isContactSelected by remember { mutableStateOf(false) }





    LaunchedEffect(key1 = true) {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(key1 = true) {
        requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
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
                onClick = {},
                imageVector = Icons.Filled.QrCodeScanner,
                onlyTitle = false,
                trailIcon = false
            )


            val network = when(userNetwork){
                "1" -> "Mainnet"
                "5" -> "Görli"
                "10" -> "Optimism"
                "137" -> "Polygon"
                "42161" -> "Arbitrum"
                "8453" -> "Base"
                else -> ""
            }

            Text(
                text = "on ${network}",
                fontSize = 16.sp,
                color = Color(0xFF9FA2A5),
                fontWeight = FontWeight.Normal,

            )

        }




        val tokenbalance = when(balances){
            is AssetUiState.Success -> {
                balances.assets.filter { it.chainId == userNetwork.toInt()}.get(0).balance
            }
            else -> {
                0.0
            }
        }

        val currentChainId = when (selectedToken) {
            is SelectedTokenUiState.Selected -> {
                selectedToken.tokenAsset.chainId
            }

            else -> {0}
        }

        val abbriviation = when(currentChainId) {
            5 -> "GoerliETH"
            137 -> "MATIC"//Polygon
            else -> "ETH"
        }

        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()

        ){

            Column (
                modifier = Modifier
                    .padding(top = 42.dp)
                    .width(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                when(isContactSelected){
                    true -> {
                        ContactPill(contact = selectedContact) {
                            selectedContact = Contact()
                            onToAddressChanged("")
                            isContactSelected = false
                        }
                    }
                    false -> {
                        ethOSCenterTextField(
                            text = toAddress,
                            label = "(Enter address, ENS or QR scan)",
                            modifier = Modifier.weight(1f),
                            singleLine = false,
                            onTextChanged = {
                                if (it.endsWith(".eth")) {
                                    if (ENSName(it.lowercase()).isPotentialENSDomain()) {
                                        // It is ENS
                                        println("Checking ENS")
                                        CompletableFuture.runAsync {
                                            val ens = ENS(
                                                HttpEthereumRPC(
                                                    "https://eth-mainnet.g.alchemy.com/v2/${
                                                        chainToApiKey("eth-mainnet")
                                                    }"
                                                )
                                            )
                                            val ensAddr = ens.getAddress(ENSName(it.lowercase()))
                                            onToAddressChanged(ensAddr?.hex.toString())
                                            validSendAddress = true
                                        }
                                    } else {
                                        if (it.isNotEmpty()) validSendAddress =
                                            WalletUtils.isValidAddress(it.lowercase())
                                    }
                                    if (it.isEmpty()) {
                                        validSendAddress = false
                                    }

                                }
                            },
                            size = 20
                        )
                    }
                }



                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    IconButton(onClick = {
                        showSheet = true
                    }) {
                        Icon(imageVector = Icons.Rounded.Person, contentDescription = "QR Scan",tint=Color.White)
                    }
                    IconButton(onClick = {
                        //opens InfoDialog
                        showCamera(barCodeLauncher)
                    }) {
                        Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = "QR Scan",tint=Color.White)
                    }
                }
            }

            Column(
                horizontalAlignment =  Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth()


            ) {



                ethOSTextField(
                    text = value,
                    label = "0",
                    singleLine = true,
                    onTextChanged = { text ->
//                        val sanitizedValue = text
//                            .replace(",", "")
//                            .replace("_", "")
//                            .replace("-", "")
//                            .replace("\\.{2,}".toRegex(), ".")


                        if ((value.isEmpty() && text == ".") ||
                            (value.isEmpty() && text == ",") ||
                            (value.isEmpty() && text == "-") ||
                            (value.isEmpty() && text == "_")
                            ) {
                            Log.d("DEBUG",value)
                            Log.d("DEBUG2",text)
                            // Remove the dot if it's the first character
                            value = ""
                        }
                        if ((value.isNotEmpty() && !value.contains(".")) ||
                            (value.isNotEmpty() && !value.contains(",")) ||
                            (value.isNotEmpty() && !value.contains("-")) ||
                            (value.isNotEmpty() && !value.contains("_"))
                        ) {
                            val regex = Regex("^(?![-.,_])[0-9]*\\.?[0-9]*\$")
                            if (value.isEmpty() || regex.matches(value)) {
                                value = text
                            }

                        }
//                        // Perform any additional validation if needed
//                        // For example, you can check if it starts with a dot and adjust the input accordingly
//                        if (sanitizedValue.isNotEmpty() && (sanitizedValue.first() == '.' || sanitizedValue.first() == ',' || sanitizedValue.first() == '_')) {
//                            // Remove the dot if it's the first character
//                            value = sanitizedValue.substring(1)
//                        } else {
//                            value = text
//                        }
                                    },
                    size = 64,
                    maxChar = 10,
                    color = if(value.isNotEmpty() && value!="." && value!=","){
                        if(value.toFloat() < tokenbalance){
                            Color.White
                        }else{
                            Color(0xFFc82e31)
                        }
                    }else {
                        Color.White
                    },
                    numberInput = true
                )
                Text(
                    text = "$tokenbalance available",
                    fontSize = 20.sp,
                    color = Color(0xFF9FA2A5),
                    fontWeight = FontWeight.Normal
                )


                Surface(
                    shape= CircleShape,
                    color = Color(0xFF262626),
                    modifier = Modifier.padding(vertical=8.dp)

                ) {
                    Text(
                        text = abbriviation,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(16.dp ,4.dp)
                    )
                }
                /*Row{
                    ethOSTextField(
                        text = value,
                        label = "0",
                        singleLine = true,
                        onTextChanged = { text -> value = text },
                        size = 64,
                        maxChar = 10,
                        color = if(value.isNotEmpty()){
                            if(value.toFloat() < tokenbalance){
                                Color.White
                            }else{
                                Color(0xFFc82e31)
                            }
                        }else {
                            Color.White
                        },
                        numberInput = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))



                    Text(
                        text = abbriviation,
                        fontSize = calculateFontSize(value.length, 64),
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                }*/


            }
            ethOSButton(
                text = "Send",
                enabled = true,
                onClick = {
                    if(value.toFloat() < tokenbalance){
                        sendTransaction(
                            SendData(
                                amount = value.toFloat(),
                                address = toAddress,
                                chainId = currentChainId
                            )
                        )
                        onBackClick()
                    }

                }
            )
        }

        if(showSheet) {
            ModalBottomSheet(
                containerColor= Color(0xFF262626),
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
                coroutineScope.launch {
                    getContacts(context)
                }
                val allowedContacts = contacts.filter { it.address.isNotEmpty() }
                ContactPickerSheet(allowedContacts) { contact ->

                    coroutineScope.launch {
                        modalSheetState.hide()
                    }.invokeOnCompletion {
                        if (!modalSheetState.isVisible) showSheet = false
                    }
                    onToAddressChanged(contact.address)
                    isContactSelected = true
                    selectedContact = contact
                }

            }
        }

    }
}




fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
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


fun showCamera(
    cameraLauncher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>
) {
    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("Scan QR Code")
    options.setCameraId(0)
    options.setBeepEnabled(false)
    options.setOrientationLocked(false)
    cameraLauncher.launch(options)
}


@Preview
@Composable
fun PreviewSendScreen() {
    val context = LocalContext.current
    SendScreen(
        onBackClick= {},
    toAddress="qwertyuio",
    balances=  AssetUiState.Success(listOf(
        TokenAsset(
            "0xggrh32335n5dsiwjr",
            1,
            "ETH",
            "Ether",
            1.977
        ),
        TokenAsset(
            "0xFjeiu54h44h5h643o4o4",
            1,
            "ETH",
            "Ether",
            1.2145
        ),
        TokenAsset(
            "0xew34n4i55i5i6nwp20dgheru",
            1,
            "ETH",
            "Ether",
            0.23
        ),

    )),
    currencyPrice= "0.5",
    onChangeAssetClicked = { tokenasset ->  },
    onToAddressChanged ={ tokenasset ->  },
    onCurrencyChange= { tokenasset ->  },
    sendTransaction= { tokenasset ->  },
    selectedToken = SelectedTokenUiState.Selected(
        TokenAsset(
            "0xFjeiu54h44h5h643o4o4",
            1,
            "ETH",
            "Ether",
            1.2145
        ),
    ),
    txComplete = TxCompleteUiState.Complete,
        userNetwork = "Mainnet",
        getContacts = {},
        contacts = emptyList()
    )
}