package com.example.assets.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import java.text.DecimalFormat

@Composable
fun AssetListItem(
    title: String,
    assets: List<TokenAsset>,
    linkTo: () -> Unit = {},
) {


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
    ) {
        Text(
            truncateString(title.uppercase()),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Text(
                        formatDouble(assets.sumOf { it.balance }),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                //TODO: Dollar Amount

            }
            Spacer(modifier = Modifier.width(24.dp))

            IconButton(
                onClick = linkTo,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Forward",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }


        }
    }
}


@Composable
fun AssetListDetailItem(
    tokenAsset: TokenAsset,
){
    val networkname = when(tokenAsset.chainId){
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        8453 -> "Base"
        42161 -> "Arbitrum"
        7777777 -> "Zora"
        else -> { ""}
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(networkname, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ){
                Row (
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text(formatDouble(tokenAsset.balance), color = Color.White,fontSize = 16.sp, fontWeight = FontWeight.Medium)

                    Text(tokenAsset.symbol.uppercase(), color = Color.White,fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                //Text("$0.00-", color = Color(0xFF9FA2A5),fontSize = 16.sp,fontWeight = FontWeight.Medium )
            }

        }
    }
}


fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}

fun truncateString(input: String): String {
    return if (input.length > 14) input.substring(0, 14) + "..." else input
}



@SuppressLint("SuspiciousIndentation")
@Composable
@Preview
fun PreviewAssetListItem(){


    val token = TokenAsset(
        address = "",
        chainId = 1,
        symbol = "ETH",
        name = "Super long spammy name",
        balance = 0.61
    )

    val list = listOf(
        token,
        token,
        token
    )

    AssetListItem(token.name,list, {})


}
