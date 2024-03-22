package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.data.model.dto.Contact
import com.core.data.util.chainToApiKey
import com.core.model.TokenAsset
import com.core.ui.ethOSCenterTextFieldInline
import com.core.ui.ethOSTextButton
import com.core.ui.ethOSTextField
import com.core.ui.util.chainIdToName
import com.core.ui.util.formatDouble
import com.feature.send.ui.AssetPickerSheet
import com.feature.send.ui.ContactPickerSheet
import com.feature.send.ui.ToolbarCaptureActivity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSButton
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.core.ethOSTagButton
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.walletmanager.ethOSContactPill
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils
import java.math.BigDecimal
import java.util.concurrent.CompletableFuture
import kotlin.math.max


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String?,
    viewModel: SendViewModel = hiltViewModel()
) {
    val currentNetwork by viewModel.currentChain.collectAsStateWithLifecycle(initialValue = "loading")
    val walletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val toAddress by viewModel.toAddress.collectAsStateWithLifecycle(initialValue = initialAddress ?: "")
    val assets by viewModel.tokensAssetState.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())
    val txComplete by viewModel.txComplete.collectAsStateWithLifecycle()



    SendScreen(
        currentNetwork = currentNetwork,
        initialAddress = initialAddress,
        modifier = Modifier,
        onBackClick = onBackClick,
        toAddress = toAddress,
        amount = amount,
        walletDataUiState = walletDataUiState,
        assets = assets,
        selectedToken = selectedToken,
        contacts = contacts,
        onChangeAssetClicked = viewModel::updateSelectedAsset,
        onAmountChange = viewModel::updateAmount,
        onToAddressChanged= viewModel::updateToAddress,
        sendTransaction = viewModel::send,
        txComplete = txComplete,
        getContacts = viewModel::getContacts,
        setMaxAmount = viewModel::setMaxAmount
    )
}
@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SendScreen(
    currentNetwork: String,
    modifier: Modifier = Modifier,
    toAddress: String,
    amount: String,
    walletDataUiState: WalletDataUiState,
    assets: AssetUiState,
    onChangeAssetClicked: (TokenAsset) -> Unit,
    onAmountChange: (String) -> Unit,
    onToAddressChanged: (String) -> Unit,
    sendTransaction: (() -> Unit) -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState,
    onBackClick: () -> Unit,
    getContacts: (Context) -> Unit,
    contacts: List<Contact>,
    initialAddress: String?,
    setMaxAmount: (BigDecimal,Int) -> Unit,
) {




    if(initialAddress != null && initialAddress !=""){
        onToAddressChanged(initialAddress)
    }
//    initialAddress?.let(onToAddressChanged)

    var validSendAddress by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Create a BottomSheetScaffoldState
    var showContactSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)

    var showAssetSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)

    var showCameraWithPerm by remember {
        mutableStateOf(false)
    }

    //initalize values


    val context = LocalContext.current

    val barCodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if(result.contents == null) {
            } else {
                val address = result.contents.removePrefix("ethereum:")
                onToAddressChanged(address)
            }
        }
    )
    val contactsPermissionsToRequest = listOf(
        Manifest.permission.READ_CONTACTS
    )

    val contactsPermissionState = rememberMultiplePermissionsState(permissions = contactsPermissionsToRequest)

    val scanningPermissionsToRequest = listOf(
        Manifest.permission.CAMERA
    )

    if (showCameraWithPerm) {
        val multiplePermissionsState = rememberMultiplePermissionsState(permissions = scanningPermissionsToRequest)

        LaunchedEffect(key1 = multiplePermissionsState.allPermissionsGranted) {
            if (multiplePermissionsState.allPermissionsGranted) {
                showCamera(barCodeLauncher)
            } else {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }
    }


    var selectedContact by remember { mutableStateOf(Contact())  }
    var isContactSelected by remember { mutableStateOf(false) }

    when (txComplete) {
        is TxCompleteUiState.UnComplete -> {

        }
        is TxCompleteUiState.Complete -> {
            onBackClick()
        }
    }

    val network = chainIdToName(currentNetwork)

//    val network = when(userData) {
//        is WalletDataUiState.Loading -> {
//            "..."
//        }
//        is WalletDataUiState.Success -> {
//            chainName(userData.userData.walletNetwork)
//        }
//    }







    val tokenBalance = when(selectedToken) {
        is SelectedTokenUiState.Unselected -> { 0.0 }
        is SelectedTokenUiState.Selected -> {
            selectedToken.tokenAsset.balance
        }
    }

    var testAddress by remember {
        mutableStateOf("")
    }

    val focusManager = LocalFocusManager.current

    Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .background(Colors.BLACK)
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                //.padding(horizontal = 32.dp, vertical = 32.dp)
        ){
            //Breadcrumb w/ backbutton
        ethOSHeader(title="Send", isBackButton = true, onBackClick = onBackClick, isBottomContent = true, bottomContent = {
            Text(
                text = network,
                fontSize = 16.sp,
                color = Colors.GRAY,
                fontFamily = Fonts.INTER,
                fontWeight = FontWeight.Normal,

                )
        })

        val currentChainId = when (selectedToken) {
            is SelectedTokenUiState.Selected -> {
                selectedToken.tokenAsset.chainId
            }

            else -> {0}
        }

        val abbriviation = when(currentChainId) {
            11155111 -> "SepolaETH"
            137 -> "MATIC"//Polygon
            else -> "ETH"
        }

        val chain = when(walletDataUiState) {
            is WalletDataUiState.Loading -> {
                "loading"
            }
            is WalletDataUiState.Success -> {
                walletDataUiState.userData.walletNetwork
            }
        }

        var startTokenSet by remember { mutableStateOf(false) }

        when(assets){
            is AssetUiState.Success -> {
                if(!startTokenSet){
                    val filteredAssets = assets.assets
                    val starttoken = filteredAssets.filter { it.symbol.contains("ETH", ignoreCase = true) }[0]
                    onChangeAssetClicked(starttoken)
                    startTokenSet = true
                }

            }

            else -> {

            }
        }


        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)

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
                        if (contactsPermissionState.allPermissionsGranted) {
                            ethOSContactPill(name = selectedContact.name, image = selectedContact.image) {
                                selectedContact = Contact()
                                onToAddressChanged("")
                                isContactSelected = false
                            }
                        }
                        LaunchedEffect(key1 = contactsPermissionState.allPermissionsGranted) {
                            if (!contactsPermissionState.allPermissionsGranted) {
                                contactsPermissionState.launchMultiplePermissionRequest()
                            }
                        }
                    }
                    false -> {

                        ethOSCenterTextFieldInline(
                            text = toAddress,//testAddress,
                            label = "address or ENS",
                            modifier = Modifier.weight(1f), //.background(Color.Red),//.fillMaxWidth(0.95f).
                            singleLine = false,
//                            center = true,
                            onTextChanged = {
//                                Log.d("Debug1",testAddress)
//                                Log.d("Debug",it)
//                                testAddress = it

                                onToAddressChanged(it)
                                CompletableFuture.runAsync {
                                    if (it.endsWith(".eth")) {
                                        if (ENSName(it.lowercase()).isPotentialENSDomain()) {
                                            // It is ENS
                                            println("Checking ENS")
                                            val ens = ENS(
                                                HttpEthereumRPC(
                                                    "https://eth-mainnet.g.alchemy.com/v2/${
                                                        chainToApiKey("eth-mainnet")
                                                    }"
                                                )
                                            )
                                            val ensAddr = ens.getAddress(ENSName(it.lowercase()))
                                            ensAddr?.let { address ->
                                                onToAddressChanged(address.hex)
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
                                }
                            },
                            size = 20,
                        )
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ){
                    IconButton(onClick = {
                        showContactSheet = true
                        if (!contactsPermissionState.allPermissionsGranted) {
                            contactsPermissionState.launchMultiplePermissionRequest()
                        }
                    }) {
                        Icon(imageVector = Icons.Rounded.Person, contentDescription = "Contact",tint=Colors.WHITE, modifier = modifier.size(32.dp))
                    }
                    IconButton(onClick = {
                        showCameraWithPerm = true
                    }) {
                        Icon(imageVector = Icons.Rounded.QrCodeScanner, contentDescription = "QR Scan",tint=Colors.WHITE, modifier = modifier.size(32.dp))
                    }
                }
            }

            Column(
                horizontalAlignment =  Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)


            ) {

                ethOSTextField(
                    text = amount,
                    label = "0",
                    singleLine = true,
                    onTextChanged = { text ->
                        if (text.isEmpty() || text == "." || text.matches("-?\\d*(\\.\\d*)?".toRegex())) {
                            // If it's a valid format or empty, call onAmountChange with the text
                            onAmountChange(text)
                        }
                    },
                    size = 64,
                    maxChar = 10,
                    color = if(amount.isNotEmpty() && amount!="." && amount!=",") {
                        if((amount.toDoubleOrNull() ?: 0.0) < tokenBalance){
                            Colors.WHITE
                        }else{
                            Colors.ERROR
                        }
                    } else {
                        Colors.WHITE
                    },
                    numberInput = true,
//                    focusManager = focusManager
                )

                when(selectedToken) {
                    is SelectedTokenUiState.Unselected -> {}
                    is SelectedTokenUiState.Selected -> {
                        Text(
                            text = "${formatDouble(tokenBalance)} available",
                            fontSize = 20.sp,
                            color = Colors.GRAY,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))



                val token = when(selectedToken){
                    is SelectedTokenUiState.Unselected -> {
                        "Select"
                    }

                    is SelectedTokenUiState.Selected -> {
                        selectedToken.tokenAsset.symbol //"Selected"
                    }
                }

//                val tagtext = when(chain){
//                    "Mainnet" -> {
//                        selectedToken = SelectedTokenUiState.Selected()
//                        "ETH"
//                    }
//                    "Optimism" -> "ETH"
//                    "Mainnet" -> "ETH"
//                    "Mainnet" -> "ETH"
//
//
//                }
//                val tokentest = when(selectedToken){
//                    is SelectedTokenUiState.Unselected -> {
//                        "Select"
//                    }
//
//                    is SelectedTokenUiState.Selected -> {
//                        Log.d("TOKEN"," address: ${selectedToken.tokenAsset.address}" +
//                                "    val chainId: ${selectedToken.tokenAsset.chainId}" +
//                                "    val symbol: ${selectedToken.tokenAsset.symbol}" +
//                                "    val name: ${selectedToken.tokenAsset.name}" +
//                                "    val balance: ${selectedToken.tokenAsset.balance}" +
//                                "    val decimals: ${selectedToken.tokenAsset.decimals}" +
//                                "    val swappable: Boolean = false")
//                        selectedToken.tokenAsset //"Selected"
//                    }
//                }

                ethOSTagButton(text = token) {
                    focusManager.clearFocus()
                    showAssetSheet = true
                }

            }

            Column (
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ){


                //Max Button Feature
                when(selectedToken) {
                        is SelectedTokenUiState.Unselected -> {}
                        is SelectedTokenUiState.Selected -> {
                            ethOSTextButton(
                                text = "Send all ${formatDouble(tokenBalance)} ${selectedToken.tokenAsset.symbol.uppercase()}",
                                enabled = true,
                                onClick = {
                                    setMaxAmount(formatDouble(tokenBalance).toBigDecimal(),currentNetwork.toInt())
                                    //onAmountChange(tokenBalance.toString())
                                }
                            )
                        }
                    }


                if(amount.isNotEmpty()){
                    ethOSButton(
                        text = "Send",
                        enabled = (selectedToken is SelectedTokenUiState.Selected) && allowedReceipient(toAddress) && allowedAmount(amount,tokenBalance),

                        onClick = {
                            if(amount.toDouble() < tokenBalance) {
                                sendTransaction {
                                    onBackClick()
                                }
                            }
                        }
                    )
                }

            }
        }

        if(showContactSheet && contactsPermissionState.allPermissionsGranted) {
            ModalBottomSheet(
                containerColor= Colors.BLACK,
                contentColor= Colors.WHITE,
                onDismissRequest = {
                    coroutineScope.launch {
                        modalSheetState.hide()
                    }.invokeOnCompletion {
                        if(!modalSheetState.isVisible) showContactSheet = false
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
                        if (!modalSheetState.isVisible) showContactSheet = false
                    }
                    onToAddressChanged(contact.address)
                    isContactSelected = true
                    selectedContact = contact
                }

            }
        }



        if(showAssetSheet){
            ModalBottomSheet(
                containerColor= Colors.BLACK,
                contentColor= Colors.WHITE,

                onDismissRequest = {
                    coroutineScope.launch {
                        modalAssetSheetState.hide()
                    }.invokeOnCompletion {
                        if(!modalAssetSheetState.isVisible) showAssetSheet = false
                    }
                },
                sheetState = modalAssetSheetState
            ) {



                AssetPickerSheet(
                    assets = assets,
                    onChangeAssetClicked = { tokenAsset ->
                        //Toast.makeText(context, "Clicked - ${tokenAsset.symbol}", Toast.LENGTH_SHORT).show()

                        Log.d("TOKEN2",tokenAsset.name)
                        val test = tokenAsset
                        onChangeAssetClicked(tokenAsset)
                        //SelectedTokenUiState.Selected(tokenAsset)
                        coroutineScope.launch {
                            modalAssetSheetState.hide()
                        }.invokeOnCompletion {
                            if (!modalAssetSheetState.isVisible) showAssetSheet = false
                        }
                    },
                    chainId = if(network != "N/A") chain.toInt() else 1

                )

            }
        }

    }
}





fun allowedReceipient(receipient: String):Boolean{
    return receipient.isNotEmpty()
}

fun allowedAmount(amount: String, tokenBalance: Double):Boolean{
    return amount.isNotEmpty() && (amount.toDoubleOrNull() ?: 0.0 ) != 0.0 && ((amount.toDoubleOrNull()
        ?: (tokenBalance + 1)) <= tokenBalance)// tokenbalance +1 if null to make it impossible

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
    cameraLauncher: ManagedActivityResultLauncher<ScanOptions?, ScanIntentResult?>
) {
    val options = ScanOptions()
    options.setCaptureActivity(ToolbarCaptureActivity::class.java)
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
        currentNetwork = "1",
        onBackClick= {},
        toAddress="qwertyuio",
        assets=  AssetUiState.Success(listOf(
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
        walletDataUiState = WalletDataUiState.Loading,
        onChangeAssetClicked = { tokenasset ->  },
        onToAddressChanged ={ tokenasset ->  },
        sendTransaction= { },
        amount = "",
        selectedToken = SelectedTokenUiState.Selected(
            TokenAsset(
                "0xFjeiu54h44h5h643o4o4",
                1,
                "ETH",
                "Ether",
                1.2145)
            ),
        onAmountChange = {},
        txComplete = TxCompleteUiState.Complete,
        getContacts = {},
        contacts = emptyList(),
        initialAddress = null,

        setMaxAmount = { i, j ->}

    )
}

