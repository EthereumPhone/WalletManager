package com.example.transactions

import android.content.Intent
import android.net.Uri
import android.view.SurfaceControl.Transaction
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.core.model.TokenAsset
import com.core.ui.util.chainIdToName
import com.example.transactions.ui.TransactionDetailItem


@Composable
fun TransctionDetailRoute(
    modifier: Modifier = Modifier,
    navigateToTransaction: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
) {

    val txUiState: TransactionDetailUiState by viewModel.currentTxState.collectAsStateWithLifecycle()
    val txHash = viewModel.txHash
    TransctionDetailScreen(
        txUiState = txUiState,
        navigateToTransaction = navigateToTransaction,
        txHash = txHash

    )
}


@Composable
fun TransctionDetailScreen(
    modifier: Modifier = Modifier,
    txUiState: TransactionDetailUiState,
    navigateToTransaction: () -> Unit,
    txHash: String

){
    val context = LocalContext.current

    when(txUiState){
        is TransactionDetailUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Loading...", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        is TransactionDetailUiState.Success -> {
            val txs = txUiState.txs // all txs
            val tx = txs.filter { it.txHash == txHash }[0] // get Tx with specific tx hash

            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ){
                //Header
                Row (
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 24.dp, top = 24.dp)
                    ,
                    Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ){


                    Button(
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor=  Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp,0.dp,16.dp,0.dp),
                        onClick = navigateToTransaction
                    ) {
                        Row (
                            modifier = modifier,
                            Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically,
                        ){
                            Icon(
                                imageVector = Icons.Rounded.ArrowBackIosNew,
                                contentDescription = "Go back",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Transactions",
                                color = Color.White,
                                fontSize = 18.sp,
                            )
                        }
                    }

                }
                Column(
                    horizontalAlignment = Alignment.Start,

                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(horizontal = 24.dp)
                ) {

                    Spacer(modifier = modifier.height(64.dp))

                    Text(text = if(tx.userSent) "Sent ${tx.asset}" else "Received ${tx.asset}",fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 48.sp)

                    Spacer(modifier = modifier.height(84.dp))


                    Column (
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ){
                        TransactionDetailItem("Tx hash",tx.txHash)
                        TransactionDetailItem(if(tx.userSent) "To" else "From",if(tx.userSent) tx.to else tx.from)
                        TransactionDetailItem("Chain",chainIdToName(tx.chainId.toString()))
                        TransactionDetailItem("Amount",tx.value)

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                //send intent to etherscann
                                val domain = getEtherscanDomainForChain(tx.chainId)
                                val link = "${domain}tx/${txHash}"


                                // Open link in browser
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(link)
                                startActivity(context, intent, null)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "View on etherscan", color = Color(0xFF71B5FF), fontSize = 20.sp, fontWeight = FontWeight.Normal)
                        }
                    }


                }
            }
        }
    }

}

fun getEtherscanDomainForChain(chainId: Int): String {
    return when(chainId) {
        1 -> "https://etherscan.io/"
        5 -> "https://goerli.etherscan.io/"
        10 -> "https://optimistic.etherscan.io/"
        137 -> "https://polygonscan.com/"
        42161 -> "https://arbiscan.io/"
        8453 -> "https://basescan.org/"
        else -> ""
    }
}
