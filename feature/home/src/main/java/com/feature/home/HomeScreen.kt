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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.ui.InfoDialog
import com.feature.home.ui.AddressBar
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.TransferDialog
import com.feature.home.ui.WalletTabRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userData: WalletDataUiState by viewModel.walletDataState.collectAsStateWithLifecycle()
    val transfersUiState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val assetsUiState: AssetUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState: Boolean by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    var updater by remember {mutableStateOf(true)}

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    HomeScreen(
        userData = userData,
        transfersUiState = transfersUiState,
        assetsUiState = assetsUiState,
        refreshState = refreshState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToReceive = navigateToReceive,
        onRefresh = viewModel::refreshData,
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    userData: WalletDataUiState,
    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {

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


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement=  Arrangement.spacedBy(56.dp),

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E2730))
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        AddressBar(
            userData,
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
        )

        FunctionsRow(
            navigateToSwap,
            navigateToSend,
            navigateToReceive
        )

        WalletTabRow(
            transfersUiState,
            assetsUiState,
            refreshState,
            onRefresh = { onRefresh() } ,
        )
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        userData = WalletDataUiState.Success(UserData("0x123...123", "TEst")),
        transfersUiState = TransfersUiState.Loading,
        assetsUiState = AssetUiState.Success(

            listOf(
                    TokenAsset(
                        address = "String",
                        chainId =  5,
                        symbol=  "ETH",
                        name= "Ether",
                        balance= 0.00000245
                    ),
                    TokenAsset(
                        address = "yuooyvyuv",
                        chainId =  10,
                        symbol=  "ETH",
                        name= "Ether",
                        balance= 0.00000245
                    )
                )


        ),
        refreshState = false,
        navigateToReceive = { },
        navigateToSend = { },
        navigateToSwap = { },
        onRefresh = { },
    )
}