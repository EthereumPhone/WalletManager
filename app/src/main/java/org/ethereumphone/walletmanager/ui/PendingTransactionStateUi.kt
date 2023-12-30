package org.ethereumphone.walletmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feature.swap.ui.getEtherscanDomainForChain

@Composable
fun PendingTransactionStateUi(
    transactionHash: String,
    transactionChainId: Int
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .fillMaxHeight(.5f)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if(transactionHash.startsWith("0x")) {
            Icon(
                Icons.Default.CheckCircleOutline,
                "",
                tint = Color.Green,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Transaction Succeeded",
                color = Color.White,
                fontSize = 32.sp,
                )

            Text(
                text = "view details",
                color = Color(0xFF71B5FF),
                fontSize = 16.sp,
                modifier = Modifier.clickable {
                    val domain = getEtherscanDomainForChain(transactionChainId)
                    val link = "${domain}tx/${transactionHash}"


                    // Open link in browser
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                    intent.data = android.net.Uri.parse(link)
                    context.startActivity(intent)
                }
            )

        } else {
            Icon(
                Icons.Outlined.Cancel,
                "",
                modifier = Modifier.size(64.dp),
                tint = Color.Red,
            )

            Spacer(modifier = Modifier.size(8.dp))


            Text(
                text = "Transaction Failed",
                fontSize = 32.sp,
                color = Color.White,
            )
        }
    }
}

@Preview
@Composable
fun PreviewEmptyPendingTransactionStateUi() {
    Column(
        Modifier.background(Color.Black)
    ) {
        PendingTransactionStateUi("error" , 0)
    }

}

@Preview
@Composable
fun PreviewPendingTransactionStateUi() {
    Column(
        Modifier.background(Color.Black)
    ) {
        PendingTransactionStateUi("0x81aeb93d0216fb83f58c50db274c6229a858f66d06dffe3358d0a9b457dec7b0" , 1)
    }
}