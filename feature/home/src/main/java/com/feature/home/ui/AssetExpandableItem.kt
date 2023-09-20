package com.feature.home.ui

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.designsystem.theme.primary
import com.core.model.TokenAsset
import com.core.ui.ExpandableListItem
import com.feature.home.R
import java.text.DecimalFormat

@Composable
fun AssetExpandableItem(
    modifier: Modifier = Modifier,
    title: String,
    assets: List<TokenAsset>,
//    currencyPrice: String,
//    onCurrencyChange: (String) -> Unit,
) {
    ExpandableListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = Color(0xFF24303D),
            supportingColor = Color.White,
            headlineColor = Color.White
        ),
        headline = {
//            Row (
//                modifier =  Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.White
                )


            //}
        },
        icons = {
            LazyRow(
                modifier=Modifier.padding(top=4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(assets) { asset ->
                    chainToIconMapper(chainId = asset.chainId, size = 20.dp)

                }
            }
        },
        support = {
            Text(
                text = "${formatDouble(assets.sumOf { it.balance })}",
                fontSize = 32.sp,
                color = Color.White,
                fontWeight = FontWeight.Normal
            )

        },
        expandedContent = {
            Column(
                Modifier
                    .background(Color(0xFF24303D))

                ,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                assets.forEach {
                    IndividualAssetItem(it)//,currencyPrice,onCurrencyChange)
                }
            }
        }
    )
}

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}

@Composable
private fun IndividualAssetItem(
    tokenAsset: TokenAsset,
//    currencyPrice: String,
//    onCurrencyChange: (String) -> Unit,
) {
   val network= when(tokenAsset.chainId) {
       1 -> "Mainnet"
       5 -> "Görli"
       10 -> "Optimism"
       137 -> "Polygon"
       42161 -> "Arbitrum"
       else -> ""
   }
//    var currencychange = when(tokenAsset.chainId){
//        137 -> "MATICUSDT"
//        else -> "ETHUSDT"
//    }
//    onCurrencyChange(currencychange)
//    var current = ""+currencyPrice
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chainToIconMapper(tokenAsset.chainId, 24.dp)
        Text(
            text = network,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = primary
        )
        Text(
            text = "${formatDouble(tokenAsset.balance)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary
        )
//        if(current != ""){
//            Text(
//                text = " $${tokenAsset.balance.toFloat() * current.toFloat()}",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Normal,
//                color = primary
//            )
//        }


    }
}


@Composable
private fun chainToIconMapper(
    chainId: Int,
    size: Dp
) {
    val modifier = Modifier
        .size(size)
    when(chainId) {
        1 -> Icon(
            painterResource(id = R.drawable.ethereum_logo),
            "Mainnet Icon",
            modifier = modifier,
            tint = Color.Unspecified
        )
        5 ->
            Icon(
                painterResource(id = R.drawable.goerli),
                "Goerli Icon",
                modifier = modifier,
                tint = Color.Unspecified
            )
        10 -> Icon(
            painterResource(id = R.drawable.optimism_logo),
            "Optimism Icon",
            modifier = modifier,
            tint = Color.Unspecified
        )
        137 -> Icon(
            painterResource(id = R.drawable.polygon_logo),
            "Polygon icon",
            modifier = modifier,
            tint = Color.Unspecified
        )
        42161 -> Icon(
            painterResource(id = R.drawable.arbitrum_logo),
            "Arbitrum Icon",
            modifier = modifier,
            tint = Color.Unspecified
        )
    }
}

@Preview
@Composable
fun AssetExapndableItemPreview() {
    LazyColumn {
        item {
            AssetExpandableItem(
                //modifier = Modifier.background(Color.DarkGray),
                title = "Ether",
                assets =
                listOf(
                    TokenAsset("", 1, "ETH", "test test", 123.2),
                    TokenAsset("", 5, "ETH", "test test", 123.2),
                    TokenAsset("", 10, "ETH", "test test", 123.2),
                    TokenAsset("", 42161, "ETH", "test test", 123.2),
                )

            )
        }
    }
}


