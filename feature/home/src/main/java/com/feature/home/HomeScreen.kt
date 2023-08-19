package com.feature.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TransferItem
import com.core.model.UserData
import com.core.ui.InfoDialog
import com.feature.home.ui.AddressBar
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.TransferDialog
import com.feature.home.ui.WalletTabRow
import org.ethereumphone.walletsdk.WalletSDK
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService

@Composable
internal fun HomeRoute(
    modifier: Modifier = Modifier,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userData by viewModel.userData.collectAsStateWithLifecycle()
    val transfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val assetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val localContext = LocalContext.current

    val web3j = Web3j.build(HttpService("https://rpc.ankr.com/eth"))
    val wallet = WalletSDK(
        context = localContext,
        web3jInstance = web3j
    )


    HomeScreen(
        userData = userData,
        transfersUiState = transfersUiState,
        assetsUiState = assetsUiState,
        refreshState = refreshState,
        onAddressClick = {
            // had to do it here because I need the local context.
            copyTextToClipboard(localContext, userData.walletAddress)
        },
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToReceive = navigateToReceive,
        onRefresh = viewModel::refreshData,
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    userData: UserData,
    transfersUiState: TransfersUiState,
    assetsUiState: AssetUiState,
    refreshState: Boolean,
    onAddressClick: () -> Unit,
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
            title = "Info",
            text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt"
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
            onAddressClick,
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

//        Column (
//            modifier = modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//        ){
//            Text(
//                text = "Balance",
//                fontWeight = FontWeight.Normal,
//                fontSize = 16.sp,
//                color = Color(0xFF9FA2A5)
//            )
//            Text(
//                text = "2.45 ETH",
//                fontWeight = FontWeight.SemiBold,
//                color = Color.White,
//                fontSize = 56.sp,
//                textAlign = TextAlign.Center
//
//            )
//        }
        FunctionsRow(
            navigateToSwap,
            navigateToSend,
            navigateToReceive
        )

        WalletTabRow(
            //MockData
//            TransfersUiState.Success(
//                listOf(
//                    TransferItem(
//                        1,
//                        "0x123123123123123123123123",
//                        "ETH",
//                        "1.5445",
//                        "12:12:12",
//                        true
//                    )
//                )
//            ),
            transfersUiState,
            assetsUiState,
            refreshState,
            onRefresh = { onRefresh() }
        )
    }
}

@SuppressLint("ServiceCast")
private fun copyTextToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    clipboardManager.setText(AnnotatedString(text))
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        userData = UserData("0x123...123"),
        transfersUiState = TransfersUiState.Loading,
        assetsUiState = AssetUiState.Loading,
        refreshState = false,
        onAddressClick = { },
        navigateToReceive = { },
        navigateToSend = { },
        navigateToSwap = { },
        onRefresh = { }
    )
}