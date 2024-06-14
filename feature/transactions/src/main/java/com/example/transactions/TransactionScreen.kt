package com.example.transactions

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.model.TransferItem
import com.core.ui.TopHeader
import com.core.ui.util.shimmerEffect
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
            .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
    ) {

        //Header

        ethOSHeader(title = "Transactions")
        Spacer(modifier = Modifier.height(48.dp))
        when(transfersUIState){
            is TransfersUiState.Loading -> {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    Column(
                        modifier = modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()
                        LoadingTransferListItem()


                    }
                }
            }
            is TransfersUiState.Success -> {

                val transfers = transfersUIState.transfers


                if (transfers.isNotEmpty()){
                    Box(
                        Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn (
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {


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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Fit,
                                painter = painterResource(id = com.core.ui.R.drawable.no_transfer),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)
                            )
                            Text(text = "No transfers", color = Colors.GRAY, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                        }
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
    val list = listOf(
        TransferItem(
            chainId = 1,
            from= "0xfwyhyg4w541wywbv4wy8wuw",
            to= "0xfwyhyg4w541wywbv4wy8wuw",
            asset = "ETH",
            value = "2.24",
            timeStamp = "10-19-01",
            userSent = false,
            txHash = "0xfwyhyg4w541wywbv4wy8wuw"

        )
    )

    TransactionScreen(
        transfersUIState = TransfersUiState.Success(
            list),
        refreshState = false,
        onRefresh = {},
        navigateToTxDetail = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingTransferListItem(
    modifier: Modifier = Modifier,
) {

    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column (
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ){
                Box(
                    contentAlignment = Alignment.Center ,
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(16.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                ) {

                }
                Box(
                    contentAlignment = Alignment.Center ,
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .height(16.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                ) {

                }
           }

            Row (
                verticalAlignment = Alignment.CenterVertically,
            ){
                Column(
                    modifier = Modifier,
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ){

                    Box(
                        contentAlignment = Alignment.Center ,
                        modifier = Modifier
                            .fillMaxWidth(0.35f)
                            .height(24.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    ) {

                    }
                    //Text(fiatAmount, color = Colors.GRAY, fontSize = 18.sp, fontWeight = FontWeight.Medium )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Box(
                    contentAlignment = Alignment.Center ,
                    modifier = Modifier
                        .width(24.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                ) {

                }
            }
        }


    }



}




@Composable
@Preview
fun TransferListItemPreview(
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colors.BLACK)
            .padding(24.dp)
    ) {
        LoadingTransferListItem()
    }

}
