package com.feature.send

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.QrCodeScanner
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import com.core.model.TokenAsset
import com.core.ui.TopHeader
import com.core.ui.ethOSButton
import java.text.DecimalFormat
import com.core.ui.ethOSTextField
import com.core.ui.ethOSCenterTextField
import com.feature.send.ui.AssetPickerSheet
import com.core.ui.util.chainIdToName
import com.feature.send.ui.ContactPickerSheet
import com.feature.send.ui.ContactPill
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import org.kethereum.eip137.model.ENSName
import org.kethereum.ens.ENS
import org.kethereum.ens.isPotentialENSDomain
import org.kethereum.rpc.HttpEthereumRPC
import org.web3j.crypto.WalletUtils

import java.util.concurrent.CompletableFuture
import kotlin.math.max


@Composable
fun SendRoute(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    initialAddress: String = "",
    viewModel: SendViewModel = hiltViewModel()
) {
    val currentNetwork by viewModel.currentChain.collectAsStateWithLifecycle(initialValue = "loading")
    val walletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val amount by viewModel.amount.collectAsStateWithLifecycle()
    val toAddress by viewModel.toAddress.collectAsStateWithLifecycle(initialValue = initialAddress)
    val assets by viewModel.tokensAssetState.collectAsStateWithLifecycle()
    val selectedToken by viewModel.selectedAssetUiState.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle(initialValue = emptyList())
    val txComplete by viewModel.txComplete.collectAsStateWithLifecycle()

    SendScreen(
        currentNetwork = currentNetwork,
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
    )
}
@SuppressLint("CoroutineCreationDuringComposition", "SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
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
    sendTransaction: () -> Unit,
    selectedToken: SelectedTokenUiState,
    txComplete: TxCompleteUiState,
    onBackClick: () -> Unit,
    getContacts: (Context) -> Unit,
    contacts: List<Contact>,

) {

    var validSendAddress by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    // Create a BottomSheetScaffoldState
    var showContactSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)

    var showAssetSheet by remember { mutableStateOf(false) }
    val modalAssetSheetState = rememberModalBottomSheetState(true)

    //initalize values


    val context = LocalContext.current

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


    var amountfontSize by remember { mutableStateOf(68.sp) }

    val maxFontSize = 68.sp
    val minFontSize = 42.sp
    val characterCount = amount.length
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

    val showInfoDialog =  remember { mutableStateOf(false) }

    when (txComplete) {
        is TxCompleteUiState.UnComplete -> {

        }
        is TxCompleteUiState.Complete -> {
            onBackClick()
        }
    }

    val network = chainIdToName(currentNetwork)


    val tokenBalance = when(selectedToken) {
        is SelectedTokenUiState.Unselected -> { 0.0 }
        is SelectedTokenUiState.Selected -> {
            selectedToken.tokenAsset.balance
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
                onClick = {},
                imageVector = Icons.Filled.QrCodeScanner,
                onlyTitle = false,
                trailIcon = false
            )



            Text(
                text = "on $network",
                fontSize = 16.sp,
                color = Color(0xFF9FA2A5),
                fontWeight = FontWeight.Normal,

            )
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
                        showContactSheet = true
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
                    text = amount,
                    label = "0",
                    singleLine = true,
                    onTextChanged = { text ->


                        if ((amount.isEmpty() && text == ".") ||
                            (amount.isEmpty() && text == ",") ||
                            (amount.isEmpty() && text == "-")

                        ) {
                            Log.d("DEBUG",amount)
                            Log.d("DEBUG2",text)
                            // Remove the dot if it's the first character


                            Log.d("DEBUG VALUE1",amount)
                        }else{
                            if ((amount.contains(".") && text == ".") ||
                                (amount.contains(".") && text == ",") ||
                                (amount.contains(".") && text == "-")

                            ) {

                                Log.d("DEBUG VALUE2",amount)
                            }else{
                                if(text != "_"){
                                    onAmountChange(amount)
                                    Log.d("DEBUG VALUE3",amount)
                                }
                            }

                        }
                    },
                    size = 64,
                    maxChar = 10,
                    color = if(amount.isNotEmpty() && amount!="." && amount!=",") {
                        if(amount.toFloat() < tokenBalance){
                            Color.White
                        }else{
                            Color(0xFFc82e31)
                        }
                    } else {
                        Color.White
                    },
                    numberInput = true
                )
                Text(
                    text = "$amount available",
                    fontSize = 20.sp,
                    color = Color(0xFF9FA2A5),
                    fontWeight = FontWeight.Normal
                )


                Spacer(modifier = Modifier.height(4.dp))
                val token = when(selectedToken){
                    is SelectedTokenUiState.Unselected -> {
                        abbriviation//"Unselected"
                    }

                    is SelectedTokenUiState.Selected -> {
                        selectedToken.tokenAsset.symbol //"Selected"
                    }
                }

                Button(
                    onClick = {
                        showAssetSheet = true
                    },
                    contentPadding = PaddingValues(start = 12.dp,end = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF262626),
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    content = {
                        Row (
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            
                            Text(token,fontWeight = FontWeight.Medium, fontSize = 18.sp)
                            Icon(
                                imageVector = Icons.Rounded.ChevronRight,
                                contentDescription = "",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(90f)
                            )
                        }
                    }
                )
            }
            ethOSButton(
                text = "Send",
                enabled = true,
                onClick = {
                    if(amount.toFloat() < tokenBalance) {
                        sendTransaction()
                        onBackClick()
                    }
                }
            )
        }

        if(showContactSheet) {
            ModalBottomSheet(
                containerColor= Color(0xFF262626),
                contentColor= Color.White,

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

        val chain = when(walletDataUiState) {
            is WalletDataUiState.Loading -> {
                "loading"
            }
            is WalletDataUiState.Success -> {
                walletDataUiState.userData.walletNetwork
            }
        }

        if(showAssetSheet){
            ModalBottomSheet(
                containerColor= Color(0xFF262626),
                contentColor= Color.White,

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
        contacts = emptyList()
    )
}