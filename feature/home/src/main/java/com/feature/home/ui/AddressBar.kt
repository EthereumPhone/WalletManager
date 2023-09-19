package com.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.UserData
import com.core.ui.SelectedNetworkButton
import java.time.LocalTime
import java.util.Calendar

@Composable
internal fun AddressBar(
    userData: UserData,
    onclick: () -> Unit,
    icon: @Composable () -> Unit,
    currentChain: Int,
    modifier: Modifier = Modifier
) {

    Column (
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Address
        Row (
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ){
            IconButton(
                onClick = {}
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color.Transparent,
                    modifier = modifier
                        .clip(CircleShape)
                    //.background(Color.Red)
                )
            }
            Box(
                modifier = Modifier
                    .clickable { onclick() },
            ){
                Text(
                    modifier = Modifier
                        .clickable { onclick() },
                    text = truncateText(userData.walletAddress),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9FA2A5)
                )
            }
            icon()
        }
        //Networkpill
        val chainName = when(currentChain) {
            1 -> "Mainnet"
            5 -> "Goerli"
            10 -> "Optimism"
            137 -> "Polygon"
            42161 -> "Arbitrum"
            else -> "Mainnet"
        }
        Surface (
            modifier = Modifier
                .clip(CircleShape),
            color = Color(0xFF24303D),
            contentColor = Color.White
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                text = chainName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

}

private fun truncateText(text: String): String {
    if (text.length > 19) {
        return text.substring(0, 5) + "..." + text.takeLast(3) + " "
    }
    return text
}

@Preview
@Composable
fun previewAddressBar() {
    AddressBar(
        UserData("0x123123123123123123"),
        { },
        icon = {
            IconButton(
                onClick = {
                    //opens InfoDialog
                    //showInfoDialog.value = true
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Information",
                    tint = Color(0xFF9FA2A5),

                )
            }
        },
        currentChain = 5

    )
}