package com.feature.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.ui.InfoDialog
import com.core.ui.TopHeader
import com.feature.home.ui.AddressBar
import com.feature.home.ui.AssetList
import com.feature.home.ui.FunctionsRow
import com.feature.home.ui.OnboardingModalBottomSheet

import kotlinx.coroutines.launch
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.core.ethOSInfoDialog
import org.ethosmobile.components.library.core.ethOSNetworkPill
import org.ethosmobile.components.library.core.ethOSOnboardingModalBottomSheet
import org.ethosmobile.components.library.models.OnboardingItem
import org.ethosmobile.components.library.models.OnboardingObject
import org.ethosmobile.components.library.theme.Colors
import java.text.DecimalFormat

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
    val assetsUiState: AssetsUiState by viewModel.tokenAssetState.collectAsStateWithLifecycle()

    var updater by remember {mutableStateOf(true)}

    if(updater) {
        Log.d("automatic updater", "TEST")
        viewModel.refreshData()
        updater = false
    }

    HomeScreen(
        userData = walletDataUiState,
        assetsUiState = assetsUiState,
//        refreshState = refreshState,
        navigateToSwap = navigateToSwap,
        navigateToSend = navigateToSend,
        navigateToReceive = navigateToReceive,
        setOnboardingComplete= viewModel::setOnboardingComplete,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    userData: WalletDataUiState,
    assetsUiState: AssetsUiState,
//    refreshState: Boolean,
    navigateToSwap: () -> Unit,
    navigateToSend: () -> Unit,
    navigateToReceive: () -> Unit,
    setOnboardingComplete: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val onboardingComplete = when(userData) {
        is WalletDataUiState.Success -> {
            !userData.userData.onboardingCompleted
        }

        is WalletDataUiState.Loading -> {
            false
        }

        else -> {
            false
        }
    }

    val modalSheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()


    val showInfoDialog =  remember { mutableStateOf(false) }
    if(showInfoDialog.value){
        ethOSInfoDialog(
            setShowDialog = {
                showInfoDialog.value = false
            },
            title = "Home",
            text = "Send and receive crypto, all natively from inside the WalletManager."
        )
    }
    if(onboardingComplete) {
        ethOSOnboardingModalBottomSheet(
            onDismiss = {
                coroutineScope.launch {
                    modalSheetState.hide()
                }.invokeOnCompletion {

                }
                setOnboardingComplete(true)
            },
            sheetState = modalSheetState,
            onboardingObject = walletOnboarding

        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement=  Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.BLACK)

    ) {
        ethOSHeader(title="Wallet", isBottomContent = true, bottomContent = {
            Spacer(modifier = modifier.height(12.dp))
            val address = when(userData) {
                is WalletDataUiState.Loading -> {
                    "..."
                }
                is WalletDataUiState.Success -> {
                    userData.userData.walletAddress
                }
            }

            val network = when(userData) {
                is WalletDataUiState.Loading -> {
                    "..."
                }
                is WalletDataUiState.Success -> {
                    chainName(userData.userData.walletNetwork)
                }
            }

            var chainColor = when(userData) {
                is WalletDataUiState.Success -> {
                    val data = userData.userData
                    when(data.walletNetwork) {
                        "1" -> Color(0xFF32CD32)
                        "5" -> Color(0xFFF0EAD6)
                        "137" -> Color(0xFF442fb2) // Polygon
                        "10" -> Color(0xFFc82e31) // Optimum
                        "42161" -> Color(0xFF2b88b8) // Arbitrum
                        "8453" -> Color(0xFF053BCB) // Base
                        "7777777" -> Color(0xFF777777) // Zora

                        else -> {
                            Color(0xFF030303)
                        }
                    }
                }
                is WalletDataUiState.Loading -> {
                    Color.Transparent
                }
            }
            ethOSNetworkPill(address = address, network = network, chainColor = chainColor)
        })

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(text = "Balance",fontWeight = FontWeight.SemiBold, color = Color(0xFF9FA2A5), fontSize = 20.sp)
            when(userData) {
                is WalletDataUiState.Loading -> {

                }
                is WalletDataUiState.Success -> {

                    val fiatAmount = when(assetsUiState) {
                        is AssetsUiState.Success -> {
                            if(assetsUiState.assets.isEmpty()) {
                                0.0
                            } else {
                                (assetsUiState.assets.filter { it.chainId == userData.userData.walletNetwork.toInt()}.firstOrNull() ?: assetsUiState.assets.filter { it.chainId == 1}.firstOrNull())?.balance ?: 0.0
                            }
                        } else -> 0.0
                    }
                    Text(text = formatDouble(fiatAmount) ,fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)
                }
            }
        }


        Column (
            verticalArrangement = Arrangement.spacedBy(56.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp, vertical = 32.dp)
        ){
            FunctionsRow(
                navigateToSwap,
                navigateToSend,
                navigateToReceive
            )

            AssetList(assetsUiState, userData)
        }




    }

}


private val walletOnboarding = OnboardingObject(
    imageVector = R.drawable.wallet_icon,
    title = "Wallet",
    items = listOf(
        OnboardingItem(
            imageVector = Icons.Filled.Wallet,
            title= "Generated System Wallet",
            subtitle = "Your personal ethOS address is managed by the wallet manager when connecting to ethOS native apps."
        ),
        OnboardingItem(
            imageVector = Icons.Outlined.Send,
            title= "Send & Receive Crypto",
            subtitle = "Seamlessly send and receive ethereum across chains through the system wallet."
        ),
        OnboardingItem(
            imageVector = Icons.Rounded.SwapVert,
            title= "Swap Tokens",
            subtitle = "Swap any ERC-20 Token directly on your phone."
        )

    )
)

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}

private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." //+ text.takeLast(3) + " "
    }
    return text
}


fun chainName(chainId: String) = when(chainId) {
    "1" -> "Mainnet"
    "5" -> "Görli"
    "10" -> "Optimism"
    "137" -> "Polygon"
    "8453" -> "Base"
    "84531" -> "Base Testnet"
    "42161" -> "Arbitrum"
    "7777777" -> "Zora"
    else -> "Loading..."
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