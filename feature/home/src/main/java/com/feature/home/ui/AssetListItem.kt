package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun AssetListItem(
    //modifier: Modifier = Modifier,
    title: String,
    value: Double,
//    currencyPrice: String,
//    onCurrencyChange: (String) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title.uppercase(),  fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(0.4f), color = Color.White)
        Text(text = "${value}", fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.30f), color = Color.White )
        Text(text = "$0.0-", fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.30f), color = Color(0xFF9FA2A5))
    }
}

fun getTotalAssetAmount(
    assets: List<Asset>
): Double{
    var amount = 0.0

    assets.forEach{ asset ->
        amount += asset.amount
    }

    return amount
}

fun getTotalAssetFiatAmount(
    assets: List<Asset>
): Double{
    var fiatamount = 0.0

    assets.forEach{ asset ->
        fiatamount += asset.fiatAmount
    }

    return fiatamount
}

@Composable
@Preview
fun PreviewAssetListItem() {

    val assets = mutableListOf(
        AssetItem(
            "Ether",
            mutableListOf(
                Asset(
                    1,
                    0.1,
                    201.51
                )
            ),
            "ETH"
        ),
        AssetItem(
            "DAI",
            mutableListOf(
                Asset(
                    1,
                    1234.56 ,
                    1234.56
                )
            ),
            "DAI"
        ),
        AssetItem(
            "Dogecoin",
            mutableListOf(
                Asset(
                    1,
                    50000.0,
                    3896.67
                )
            ),
            "DOGE"
        ),
        AssetItem(
            "WETH",
            mutableListOf(
                Asset(
                    1,
                    0.41,
                    826.20
                ),
                Asset(
                    2,
                    0.2,
                    403.02
                )

            ),
            "WETH"
        )
    )

    LazyColumn(){
        assets.forEach { item ->
            item(key=item.assetname){
                //AssetListItem(item.abbriviation,item.assets)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    
    
}