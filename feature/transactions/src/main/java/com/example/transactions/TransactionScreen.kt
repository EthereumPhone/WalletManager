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
import com.example.transactions.ui.TransactionDetailItem
import com.example.transactions.ui.TransferListItem

@Composable
fun TransactionRoute(
    modifier: Modifier = Modifier,
//    onBackClick: () -> Unit,
//    initialAddress: String = "",
    navigateToTxDetail: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val transfersUIState: TransfersUiState by viewModel.transferState.collectAsStateWithLifecycle()

    TransactionScreen(
        transfersUIState = transfersUIState,
        navigateToTxDetail = navigateToTxDetail
    )
}

@Composable
fun TransactionScreen(
    modifier: Modifier = Modifier,
    transfersUIState: TransfersUiState,
    navigateToTxDetail: (String) -> Unit,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp, vertical = 32.dp)
    ) {

        //Header

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = "Transactions",
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
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

                Spacer(modifier = Modifier.height(32.dp))


            if (transfers.size > 0){
                LazyColumn (
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ){
                    var month = ""


                    transfers.reversed().forEach { transfer ->
                        item {
                            //TODO: Month sections
//                            Log.e("Month", month)
//                            if (transfer.timeStamp.substring(0,2) != month){
//                                month = transfer.timeStamp.substring(0,2)
//                                val monthtext = getMonth(transfer.timeStamp.substring(0,2).toInt())
//                                Text(text = monthtext,color = Color(0xFF9FA2A5))
//
//                            }
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
                    Text(text = "No Transfers yet", color = Color(0xFF9FA2A5), fontSize = 24.sp, fontWeight = FontWeight.Medium)
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
    //TransactionScreen()
}
