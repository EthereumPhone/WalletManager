package com.example.transactions

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.transactions.ui.LogEntry
import com.example.transactions.ui.TxEntry
import com.example.transactions.ui.TxType


@Composable
fun LogView(
    onNavigateBack: () -> Unit = {}
){


    val txs = listOf(
        TxEntry(
            fromAmount = 20.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SENT
        ),
        TxEntry(
            fromAmount = 40.56,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 2.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SENT
        ),
        TxEntry(
            fromAmount = 40.56,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 2.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SENT
        ),
        TxEntry(
            fromAmount = 40.56,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 2.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SENT
        ),
        TxEntry(
            fromAmount = 40.56,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 2.0,
            toAmount = 0.0,
            fromTokenName = "USDC",
            toTokenName = "",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.RECEIVED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),
        TxEntry(
            fromAmount = 20.0,
            toAmount = 245532.0,
            fromTokenName = "USDC",
            toTokenName = "DEGEN",
            fromAddress = "0x369D9A3163ba0F5b703FDFe41471E20650523e68",
            toAddress = "0x422C8d8eD47262De2C45F4628563Cc0FdDD6DE0E",
            txType = TxType.SWAPED
        ),

        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dgenBlack)

    ) {
        Row (
            modifier = Modifier.fillMaxWidth().padding(end = 4.dp,
                start = 4.dp,top = 8.dp)
        ){
            IconButton(modifier = Modifier, onClick = onNavigateBack) {
                Icon(
                    modifier = Modifier.width(16.dp),
                    painter = painterResource(R.drawable.back_icon),
                    contentDescription = "Back",
                    tint = dgenTurqoise
                )
            }
        }

        Box(modifier = Modifier
            .fillMaxSize()){
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                }
                items(txs){ tx ->
                    LogEntry(tx)
                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .align(Alignment.TopCenter)// Adjust thickness of fading border
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(dgenBlack,Color.Transparent)
                        )
                    )

            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)// Adjust thickness of fading border
                    .height(40.dp) // Adjust thickness of fading border
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, dgenBlack)
                        )
                    )

            )
        }

    }

}

@Preview(
    showBackground = true,
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun LogViewPreview(){
//    val list = listOf(
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFF1E5A9C),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFF9C27B0),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFF8BC34A),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFFE91E63),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFF009688),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFFFFEB3B),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFF00BCD4),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//        Asset(
//            amount = 120.00,
//            tokenName = "USDC",
//            fiatAmount = 120.00,
//            backgroundColor = Color(0xFFF44336),
//            chainList = listOf(1,10,8453,42161),
//            icon = R.drawable.usdc
//
//        ),
//
//        )
    LogView(

    )
}