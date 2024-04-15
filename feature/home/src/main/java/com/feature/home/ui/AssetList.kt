package com.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.model.UserData
import com.core.ui.R
import com.core.ui.util.shimmerEffect
import com.feature.home.AssetsUiState
import com.feature.home.WalletDataUiState
import com.feature.home.formatDouble
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts
@Composable
fun AssetList(
    assetsUiState: AssetsUiState,
    userData: WalletDataUiState
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.45f)
    ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(text = "Overview", color = Colors.GRAY, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            when(assetsUiState){
                is AssetsUiState.Empty ->{
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(42.dp),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = R.drawable.no_assets),
                                contentDescription = null
                            )
                            Text(text = "No assets", fontFamily = Fonts.INTER, color = Colors.GRAY, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                        }
                    }
                }
                is AssetsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LoadingSimpleAssetListItem()
                            LoadingSimpleAssetListItem()
                            LoadingSimpleAssetListItem()

                        }


                    }
                }
                is AssetsUiState.Success -> {
                    if (userData is WalletDataUiState.Success) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ){
                            val assets = assetsUiState.assets
                            val filteredassets = assets.filter { it.balance > 0.0 }

                            if (filteredassets.isNotEmpty()){
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    //                            assets.forEach { item ->
//                                item(key = item.address) {
//                                    val value = formatDouble(item.balance)
//                                    //TODO: use meethod in composable
//                                    ethOSSimpleAssetListItemCustom(title = item.symbol, value =  formatDouble(main.balance).replace(",", ".").toDouble())
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                }
//                            }

                                    val main = filteredassets.find { it.chainId == 1 }
                                    if (main != null) {
                                        ethOSSimpleAssetListItemCustom(title = main.symbol, value = formatDouble(main.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val arb = filteredassets.find { it.chainId == 42161 }
                                    if (arb != null) {
                                        ethOSSimpleAssetListItemCustom(title = arb.symbol, value =  formatDouble(arb.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val base = filteredassets.find { it.chainId == 8453 }
                                    if (base != null) {
                                        ethOSSimpleAssetListItemCustom(title = base.symbol, value =  formatDouble(base.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val basegoerli = filteredassets.find { it.chainId == 84531 }
                                    if (basegoerli != null) {
                                        ethOSSimpleAssetListItemCustom(title = basegoerli.symbol, value =  formatDouble(basegoerli.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val op = filteredassets.find { it.chainId == 10 }
                                    if (op != null) {
                                        ethOSSimpleAssetListItemCustom(title = op.symbol, value =  formatDouble(op.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val sep = filteredassets.find { it.chainId == 11155111 }
                                    if (sep != null) {
                                        ethOSSimpleAssetListItemCustom(title = sep.symbol, value =  formatDouble(sep.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    val zora = filteredassets.find { it.chainId == 7777777 }
                                    if (zora != null) {
                                        ethOSSimpleAssetListItemCustom(title = zora.symbol, value =  formatDouble(zora.balance).replace(",", ".").toDouble())
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        modifier = Modifier.size(42.dp),
                                        contentScale = ContentScale.Crop,
                                        painter = painterResource(id = R.drawable.no_assets),
                                        contentDescription = null
                                    )
                                    Text(text = "No assets", fontFamily = Fonts.INTER, color = Colors.GRAY, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                                }
                            }



                        }

                    }
                }
                is AssetsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                modifier = Modifier.size(42.dp),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(id = R.drawable.baseline_error_outline_24),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(Colors.GRAY)

                            )
                            Text(text = "No assets", fontFamily = Fonts.INTER, color = Colors.GRAY, fontSize = 18.sp, fontWeight = FontWeight.Medium)

                        }
                    }
                }
            }
        }
    }
}

fun String.capitalizeFirstCharAndLowercaseRest(): String {
    if (this.isEmpty()) return this
    return this[0].uppercaseChar() + this.substring(1).lowercase()
}

@Composable
fun ethOSSimpleAssetListItemCustom(
    title: String,
    value: Double,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title.capitalizeFirstCharAndLowercaseRest(), fontFamily = Fonts.INTER, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(0.4f), color = Color.White)
        Text(text = formatDouble(value),fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.30f), color = Color.White )
    }
}

@Composable
fun LoadingSimpleAssetListItem(
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            contentAlignment = Alignment.Center ,
            modifier = Modifier
                .height(18.dp)
                .width(180.dp)
                .clip(CircleShape)
                .shimmerEffect()

        ) {

        }
        Box(
            contentAlignment = Alignment.Center ,
            modifier = Modifier
                .height(18.dp)
                .width(64.dp)
                .clip(CircleShape)
                .shimmerEffect()

        ) {

        }
    }
}

@Preview
@Composable
fun PreviewAssetList() {
    AssetList(
        AssetsUiState.Loading,
//        AssetsUiState.Success(
//            listOf(
//                TokenAsset(
//                    address = "",
//                    chainId = 1,
//                    symbol = "ETH",
//                    name = "ETHER",
//                    balance = 1.0,
//                    decimals = 18,
//                    swappable = true
//                )
//            )
//        ),
        WalletDataUiState.Success(
            UserData(
                "",
                "",
                true,
                ""
            )
        )
    )
}