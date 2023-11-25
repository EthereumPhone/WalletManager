package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.database.model.TransferEntity
import com.core.model.SendData
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.feature.home.ui.AddressBar
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.TransferDialog
import com.feature.home.ui.TransferListItem
import com.feature.home.ui.WalletTabRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.reflect.KFunction1

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userData: UserData by viewModel.userData.collectAsStateWithLifecycle()
    val transfersUiState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    val currencyPrice: String by viewModel.exchange.collectAsStateWithLifecycle("")
    val currentChain: Int by viewModel.currentChain.collectAsStateWithLifecycle(1)

    var updater by remember {mutableStateOf(true)}

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    HomeScreen(
//        userData = userData,
//        currentChain = currentChain,
//        getCurrentChain = viewModel::getCurrentChain,
//        transfersUiState = transfersUiState,
//        assetsUiState = assetsUiState,
//        refreshState = refreshState,
//        currencyPrice = currencyPrice,
//        onCurrencyChange = viewModel::getExchange,
//        onAddressClick = {
//            // had to do it here because I need the local context.
//            copyTextToClipboard(localContext, userData.walletAddress)
//        },
//        navigateToSwap = navigateToSwap,
//        navigateToSend = navigateToSend,
//        navigateToReceive = navigateToReceive,
//        onRefresh = viewModel::refreshData,
//        onDelete = { tx ->
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    //Deletes pending tx out of database
//                    viewModel.deletePendingTransfer(tx)
//                }
//
//        },
//        modifier = modifier

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
//    userData: UserData,
//    currentChain: Int,
//    getCurrentChain: (Context) -> Unit,
//    transfersUiState: TransfersUiState,
//    assetsUiState: AssetUiState,
//    refreshState: Boolean,
//    currencyPrice: String,
//    onCurrencyChange: (String) -> Unit,
//    onAddressClick: () -> Unit,
//    navigateToSwap: () -> Unit,
//    navigateToSend: () -> Unit,
//    navigateToReceive: () -> Unit,
//    onRefresh: () -> Unit,
//    onDelete: (TransferItem) -> Unit,
    modifier: Modifier = Modifier
) {



    var showSheet by remember { mutableStateOf(false) }
    val modalSheetState = rememberModalBottomSheetState(true)
    // Declaring Coroutine scope
    val coroutineScope = rememberCoroutineScope()

    val showInfoDialog =  remember { mutableStateOf(false) }
    if(showInfoDialog.value){
        InfoDialog(
            setShowDialog = {
                showInfoDialog.value = false
            },
            title = "Home",
            text = "Send and receive crypto, all natively from inside the WalletManager."
        )
    }


    var showTransferInfoDialog = remember { mutableStateOf(false) }
    var txInfo =  remember { mutableStateOf(
        TransferItem(
            chainId = 1,
            from="",
            to="",
            asset = "",
            value = "",
            timeStamp = "",
            userSent = true,
            txHash = "",
            ispending = false
        )
    ) }

//    if(showTransferInfoDialog.value){
//        TransferDialog(
//            setShowDialog = {
//                showTransferInfoDialog.value = false
//            },
//            //title = "Transfer",
//            transfer = txInfo.value,
//            currencyPrice = currencyPrice,
//            onCurrencyChange = onCurrencyChange
//
//        )
//    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement=  Arrangement.SpaceBetween,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        AddressBar(
//            userData,
//            onAddressClick,
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
            },
//            currentChain,
//            getCurrentChain
        )






        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "My Balance",fontWeight = FontWeight.SemiBold, color = Color(0xFF9FA2A5), fontSize = 20.sp)
            Text(text = "$${7600.35}",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)
        }

        //, fontWeight = FontWeight.Medium, fontSize = 16.sp, Color.White)



        Column (
            verticalArrangement = Arrangement.spacedBy(56.dp)
        ){
            FunctionsRow(
//            navigateToSwap,
//            navigateToSend,
//            navigateToReceive
            )
            WalletTabRow(
//            transfersUiState,
//            assetsUiState,
//            refreshState,
//            onTxOpen = {
//                txInfo.value = it
//                showTransferInfoDialog.value = true
//            },
//            onRefresh = { onRefresh() } ,
//            userAddress= userData.walletAddress,
//            onDelete =  onDelete
//            currencyPrice = currencyPrice,
//            onCurrencyChange = onCurrencyChange
            )
        }





        if(showSheet) {
            ModalBottomSheet(
                containerColor = Color(0xFF24303D),
                contentColor = Color.White,

                onDismissRequest = {
                    coroutineScope.launch {
                        modalSheetState.hide()
                    }.invokeOnCompletion {
                        if(!modalSheetState.isVisible) showSheet = false
                    }
                },
                sheetState = modalSheetState
            ) {

//                NetworkPickerSheet(
//                    balancesState = balances,
//                    onSelectAsset = {
//                        //onChangeAssetClicked(it)
//                        //hides ModelBottomSheet
//                        coroutineScope.launch {
//                            modalSheetState.hide()
//                        }.invokeOnCompletion {
//                            if(!modalSheetState.isVisible) showSheet = false
//                        }
//                    }
//                )
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
fun AssetScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {

        //Header

        TopHeader(
            onBackClick = {},//onBackClick ,
            title = "My Assets",
            icon = {
                IconButton(
                    onClick = {
                        //opens InfoDialog
//                        showInfoDialog.value = true
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
        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Ether",
            balance = 0.61
        )

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            items(3){
                AssetListItem(token, true)
            }

        }

    }
}

@Composable
fun AssetDetailScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.Start,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {

        //Header
        Row (
            modifier = modifier
                .fillMaxWidth()
               ,
            //.background(Color.Red),
            Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ){


                IconButton(
                    onClick = {
                        //onBackClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Go back",
                        tint = Color.White
                    )
                }



                    Text(
                        text = "Home",
                        color = Color.White,
                        fontSize = 18.sp,

                        )

        }

        Spacer(modifier = modifier.height(64.dp))

        Text(text = "WETH",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

        Spacer(modifier = modifier.height(84.dp))

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Mainnet",
            balance = 0.61
        )

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            items(3){
                AssetListItem(token, false)
            }

        }
    }
}

@Composable
fun AssetListItem(
    tokenAsset: TokenAsset,
    withMoreDetail: Boolean=false,
    linkTo: () -> Unit = {},
){
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(tokenAsset.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ){
                Row (
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text("${tokenAsset.balance}", color = Color.White)

                    Text(tokenAsset.symbol, color = Color.White)
                }
                Text("$0.00", color = Color(0xFF9FA2A5), )
            }
            if(withMoreDetail){
                IconButton(
                    onClick = linkTo,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForwardIos,
                        contentDescription = "Go back",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

        }
    }
}


@Composable
fun TransactionDetailItem(
    title: String,
    info: String
){
    Column (
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(text = title, color = Color(0xFF9FA2A5), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = info, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}




@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {

        //Header

        Row (
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)

        ){
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "Transactions",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Ether",
            balance = 0.61
        )

        val list = listOf(
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = true,
                txHash= "pfpfnopjfpfn",
                ispending = false

            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "10-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "11-19-01",//Clock.System.now().toString(),
                userSent = true,
                txHash= "pfpfnopjfpfn",
                ispending = false

            ),
            TransferItem(
                chainId = 5,
                from = "0x123123123123123123123123",
                to =  "0x123123123123123123123123",
                asset = "ETH",
                //address = "0x123123123123123123123123",
                value = "2.24",
                timeStamp = "11-19-01",//Clock.System.now().toString(),
                userSent = false,
                txHash= "pfpfnopjfpfn",
                ispending = false
            )

        )
        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ){
            var month = ""
            list.reversed().forEach { transfer ->
                item {
                    Log.e("Month", month)
                    if (transfer.timeStamp.substring(0,2) != month){
                        month = transfer.timeStamp.substring(0,2)
                        val monthtext = getMonth(transfer.timeStamp.substring(0,2).toInt())
                        Text(text = monthtext,color = Color(0xFF9FA2A5))

                    }
                    TransferListItem(
                        transfer = transfer ,
                        onCardClick = {}
                    )
                }
            }
//            items(3){
//                //AssetListItem(token, true)
//                TransferListItem(
//                    transfer = ,
//                    onCardClick = {}
//                )
//            }
        }

    }
}

@Composable
fun TransactionDetailScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.Start,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {

        //Header
        Row (
            modifier = modifier
                .fillMaxWidth()
            ,
            //.background(Color.Red),
            Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ){


            IconButton(
                onClick = {
                    //onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Go back",
                    tint = Color.White
                )
            }



            Text(
                text = "Home",
                color = Color.White,
                fontSize = 18.sp,

                )

        }

        Spacer(modifier = modifier.height(64.dp))

        Text(text = "WETH",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

        Spacer(modifier = modifier.height(84.dp))

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Mainnet",
            balance = 0.61
        )

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            items(3){
                AssetListItem(token, false)
            }

        }
    }
}


fun getMonth(month: Int): String {
    var res = "This month"
    when(month){
        11 -> res = "November"
        10 -> res = "Oktober"
    }

    return res
}

@Composable
fun TransctionDetailScreen(
    modifier: Modifier = Modifier
){
    Column(
        horizontalAlignment = Alignment.Start,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {

        //Header
        Row (
            modifier = modifier
                .fillMaxWidth()
            ,
            //.background(Color.Red),
            Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ){


            IconButton(
                onClick = {
                    //onBackClick()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Go back",
                    tint = Color.White
                )
            }



            Text(
                text = "Transactions",
                color = Color.White,
                fontSize = 18.sp,

                )

        }

        Spacer(modifier = modifier.height(64.dp))

        Text(text = "Sent ETH",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

        Spacer(modifier = modifier.height(84.dp))

        val token = TokenAsset(
            address = "",
            chainId = 1,
            symbol = "ETH",
            name = "Mainnet",
            balance = 0.61
        )

        Column (
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            TransactionDetailItem("Date","Hallo")
            TransactionDetailItem("Address","Hallo")
            TransactionDetailItem("Chain","Hallo")
            TransactionDetailItem("Amount","Hallo")
        }

        Spacer(modifier = Modifier.height(56.dp))
        Button(
            onClick = { /*TODO*/ },
           colors = ButtonDefaults.buttonColors(
               containerColor = Color.Transparent,
               contentColor = Color.White,
           ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "View on etherscan", color = Color(0xFF71B5FF), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}


@Preview
@Composable
fun PreviewHomeScreen() {
    TransctionDetailScreen()
    //HomeScreen()
//    HomeScreen(
//        userData = UserData("0x123...123"),
//        transfersUiState = TransfersUiState.Loading,
//        assetsUiState = AssetUiState.Success(
//
//            listOf(
//                    TokenAsset(
//                        address = "String",
//                        chainId =  5,
//                        symbol=  "ETH",
//                        name= "Ether",
//                        balance= 0.00000245
//                    ),
//                    TokenAsset(
//                        address = "yuooyvyuv",
//                        chainId =  10,
//                        symbol=  "ETH",
//                        name= "Ether",
//                        balance= 0.00000245
//                    )
//                )
//
//
//        ),
//        refreshState = false,
//        onAddressClick = { },
//        navigateToReceive = { },
//        navigateToSend = { },
//        navigateToSwap = { },
//        onRefresh = { },
//        onDelete = {},
//        onCurrencyChange = {},
//        currencyPrice = "1650.00",
//        currentChain = 1,
//        getCurrentChain = {}
//    )
}