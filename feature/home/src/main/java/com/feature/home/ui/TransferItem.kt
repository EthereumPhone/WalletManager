package com.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.designsystem.theme.background
import com.core.designsystem.theme.primary
import com.core.designsystem.theme.primaryVariant
import com.core.model.TransferItem
import com.core.ui.WmListItem
import kotlinx.datetime.Clock


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransferItemCard(
    modifier: Modifier = Modifier,
    transfer: TransferItem,
    onCardClick: () -> Unit
) {
    val headlineText = remember {
        buildString {
            append(if (transfer.userSent) "Sent " else "Received ")
            append(transfer.value)
            append(" ")
            append(transfer.asset)
            append(if (transfer.userSent) " to " else " from ")
            append(transfer.address)
        }
    }
    val networkName = remember { chainToNetworkName(transfer.chainId) }

//    WmListItem(
//        headlineContent = {
//            Text(text = headlineText)
//        },
//        supportingContent = {
//            Row {
//                Text(
//                    text = networkName,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(text = transfer.timeStamp)
//            }
//        }
//    )
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors =  CardDefaults.cardColors(
            containerColor= primaryVariant,
            contentColor= Color.White,
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = {
            //Show Dialog w/ more info
            onCardClick()
        }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.padding(12.dp)
        ) {

            Box (
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(42.dp)
            ){
                Icon(
                    imageVector = if (transfer.userSent) Icons.Rounded.NorthEast else Icons.Rounded.ArrowDownward,
                    contentDescription = "Send",
                    tint = Color(0xFF24303D)
                )
            }
            Column (
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
//                    Text(
//                        text = if (transfer.userSent) "Sent to" else "Received from",
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                    Icon(imageVector = Icons.Rounded.MoreHoriz, contentDescription = "More Info")
                 }

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,

                        ) {
                            Text(

                                text = transfer.address,
                                color = Color.White,//(0xFF9FA2A5),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                modifier = Modifier.width(125.dp)
                            )
                            Text(
                                text = chainToNetworkName(transfer.chainId),
                                color = Color(0xFF9FA2A5)
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = transfer.value+" "+transfer.asset,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                    text = transfer.timeStamp,
                            color = Color(0xFF9FA2A5)
                            )
                        }

                    }




            }
//            Row (
//                modifier = modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
////                Text(
////                    text = networkName,
////                    modifier = Modifier.weight(1f)
////                )
//
//            }
        }
        }


    }
}

private fun chainToNetworkName(chainId: Int): String = when(chainId) {
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        42161 -> "Arbitrum"
        else -> ""
    }

@Preview
@Composable
fun TransferItemPreview() {
    TransferItem(
        chainId = 5,
        asset = "ether",
        address = "0x123123",
        value = "2.24",
        timeStamp = Clock.System.now().toString(),
        userSent = true
    )
//    TransferItemCard(
//        transfer = TransferItem(
//            chainId = 5,
//            asset = "ETH",
//            address = "0x123123123123123123123123",
//            value = "2.24",
//            timeStamp = "12:12:00",
//            userSent = true
//        )
//    )

}