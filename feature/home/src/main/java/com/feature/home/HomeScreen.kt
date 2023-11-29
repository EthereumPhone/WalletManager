package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.ui.InfoDialog
import com.feature.home.ui.AddressBar
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.WalletTabRow
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val walletDataUiState: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val transfersUiState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current
    //val currencyPrice: String by viewModel.exchange.collectAsStateWithLifecycle("")

    var updater by remember {mutableStateOf(true)}

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    HomeScreen(
        userData = walletDataUiState,
//        transfersUiState = transfersUiState,
        assetsUiState = assetsUiState,
//        refreshState = refreshState,
//        currencyPrice = currencyPrice,
//        onCurrencyChange = viewModel::getExchange,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToReceive = navigateToReceive,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    userData: WalletDataUiState,
//    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
//    refreshState: Boolean,
//    currencyPrice: String,
//    onCurrencyChange: (String) -> Unit,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
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


    val fiatBalance = when(assetsUiState){
        is AssetUiState.Loading -> {
            emptyList<TokenAsset>()
        }
        is AssetUiState.Empty -> {
            0.0
        }
        is AssetUiState.Error -> {
            emptyList<TokenAsset>()
        }
        is AssetUiState.Success -> {
            val assetsbalance = assetsUiState.assets.filter { it.balance > 0.0 }

            var res = 0.0

            assetsbalance.forEach {
                res += it.balance
                //get fiat value
            }
            res
        }
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement=  Arrangement.SpaceBetween,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {
        AddressBar(userData)


        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "My Balance",fontWeight = FontWeight.SemiBold, color = Color(0xFF9FA2A5), fontSize = 20.sp)
            Text(text = "$${fiatBalance}",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)
        }

        Column (
            verticalArrangement = Arrangement.spacedBy(56.dp)
        ){
            FunctionsRow(
            navigateToSwap,
            navigateToSend,
            navigateToReceive
            )
            WalletTabRow(
//            transfersUiState,
            assetsUiState,
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
                containerColor = Color.Black,
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

















@Preview
@Composable
fun PreviewHomeScreen() {
//    HomeScreen()
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