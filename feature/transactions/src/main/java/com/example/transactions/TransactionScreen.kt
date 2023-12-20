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
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {

        //Header

        TopHeader(title = "Transactions")
        Spacer(modifier = Modifier.height(48.dp))
        when(transfersUIState){
            is TransfersUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            is TransfersUiState.Success -> {

                val transfers = transfersUIState.transfers


                if (transfers.size > 0){
                    LazyColumn (
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ){
                        var month = ""


                        transfers.reversed().forEach { transfer ->
                            item {
                                TransferListItem(
                                    transfer = transfer ,
                                    onCardClick = {
                                        navigateToTxDetail(transfer.txHash)
                                    }
                                )
                            }
                        }
                    }
                }else{
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Text(text = "No Transfers yet", color = Color(0xFF9FA2A5), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
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
