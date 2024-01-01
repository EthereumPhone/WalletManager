package org.ethereumphone.walletmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.feature.swap.ui.getEtherscanDomainForChain
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


@Composable
fun PendingTransactionStateUi(
    transactionHash: String,
    transactionChainId: Int
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 48.dp)
            .fillMaxHeight(.5f)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column (
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            if(transactionHash.startsWith("0x")) {
                Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(110.dp)
                        .border(
                            5.dp, Colors.SUCCESS,
                            CircleShape
                        )
                ){
                    Icon(
                        Icons.Rounded.Check,
                        "Approve",
                        tint = Colors.SUCCESS,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Transaction Succeeded",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontFamily = Fonts.INTER,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "View Details",
                    color = Color(0xFF71B5FF),
                    fontSize = 18.sp,
                    fontFamily = Fonts.INTER,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.clickable {
                        val domain = getEtherscanDomainForChain(transactionChainId)
                        val link = "${domain}tx/${transactionHash}"


                        // Open link in browser
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                        intent.data = android.net.Uri.parse(link)
                        context.startActivity(intent)
                    }
                )

            }
            else {

                Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(110.dp)
                        .border(
                            5.dp, Colors.ERROR,
                            CircleShape
                        )
                ){
                    Icon(
                        Icons.Rounded.Close,
                        "Failed",
                        tint = Colors.ERROR,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Transaction Failed",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontFamily = Fonts.INTER,
                    fontWeight = FontWeight.SemiBold
                )

            }
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewEmptyPendingTransactionStateUi() {
    var showSheet by remember { mutableStateOf(false) }
    val transactionSheetState = rememberModalBottomSheetState(true)
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = {
            showSheet = true
        }) {
            Text(text = "Error")
        }


        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch {
                        transactionSheetState.hide()
                    }.invokeOnCompletion {
                        if (!transactionSheetState.isVisible) {
                            showSheet = false

                        }
                    }
                },
                sheetState = transactionSheetState,
                containerColor = Color.Black,
                contentColor = Color.White
            ) {

                PendingTransactionStateUi("error" , 0)
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewPendingTransactionStateUi() {


       var showSheet by remember { mutableStateOf(false) }
        val transactionSheetState = rememberModalBottomSheetState(true)
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = {
                showSheet = true
            }) {
                Text(text = "Test")
            }


            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        coroutineScope.launch {
                            transactionSheetState.hide()
                        }.invokeOnCompletion {
                            if (!transactionSheetState.isVisible) {
                                showSheet = false

                            }
                        }
                    },
                    sheetState = transactionSheetState,
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {

                    PendingTransactionStateUi(
                        "0x81aeb93d0216fb83f58c50db274c6229a858f66d06dffe3358d0a9b457dec7b0",
                        1
                    )
                }
            }
        }

}