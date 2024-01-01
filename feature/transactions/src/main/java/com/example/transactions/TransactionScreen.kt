package com.example.transactions

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.ui.TopHeader
import com.example.transactions.ui.TransactionDetailItem
import com.example.transactions.ui.TransferListItem
import org.ethosmobile.components.library.core.ethOSHeader
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
import org.ethosmobile.components.library.walletmanager.ethOSTransferListItem

@Composable
fun TransactionRoute(
    modifier: Modifier = Modifier,
//    onBackClick: () -> Unit,
    navigateToTxDetail: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transfersUIState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()
    val refreshState by viewModel.isRefreshing.collectAsStateWithLifecycle()

    TransactionScreen(
        transfersUIState = transfersUIState,
        navigateToTxDetail = navigateToTxDetail,
        refreshState = refreshState,
        onRefresh = viewModel::refreshData
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    transfersUIState: TransfersUiState,
    refreshState: Boolean,
    onRefresh: () -> Unit,
    navigateToTxDetail: (String) -> Unit,
) {

    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshState,
        onRefresh = {
            onRefresh()
        }
    )



    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Colors.BLACK)
            .padding(start = 32.dp,end = 32.dp, bottom = 32.dp)
    ) {

        //Header

//        TopHeader(title = "Transactions")
        ethOSHeader(title = "Transactions")
        Spacer(modifier = Modifier.height(48.dp))
        when(transfersUIState){
            is TransfersUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontFamily = Fonts.INTER, fontWeight = FontWeight.SemiBold)
                }
            }
            is TransfersUiState.Success -> {

                val transfers = transfersUIState.transfers


                if (transfers.isNotEmpty()){
                    Box(Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            var month = ""

                        transfers.reversed().forEach { transfer ->
                            item {

                                ethOSTransferListItem(

                                    asset = transfer.asset,
                                    value = transfer.value,
                                    timeStamp = transfer.timeStamp,//Clock.System.now().toString(),
                                    userSent = transfer.userSent,
                                    onCardClick = {
                                        navigateToTxDetail(transfer.txHash)
                                    }
                                )
                            }
                        }

                        PullRefreshIndicator(
                            refreshing = refreshState,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }

                    
                }else{
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(text = "No Transfers yet", color = Colors.GRAY, fontSize = 24.sp, fontFamily = Fonts.INTER, fontWeight = FontWeight.SemiBold)
                    }
                }




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
@Preview
fun PreviewTransactionScreen(
    modifier: Modifier = Modifier
){
    TransactionScreen(
        transfersUIState = TransfersUiState.Loading,
        refreshState = false,
        onRefresh = {},
        navigateToTxDetail = {}
    )
}
