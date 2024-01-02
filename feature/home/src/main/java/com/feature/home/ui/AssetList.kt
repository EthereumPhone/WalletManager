package com.feature.home.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.model.UserData
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
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Text(text = "No assets", color = Colors.GRAY, fontSize = 20.sp, fontWeight = FontWeight.Medium)

                    }
                }
                is AssetsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center){
                        Text(text = "Loading...", color = Colors.GRAY, fontSize = 20.sp, fontWeight = FontWeight.Medium)

                    }
                }
                is AssetsUiState.Success -> {
                    if (userData is WalletDataUiState.Success) {
                        val assets = assetsUiState.assets
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val sortedAssets = assets.sortedWith(compareBy { it.chainId != (userData.userData.walletNetwork.toIntOrNull() ?: 0) })

                            sortedAssets.forEach { item ->
                                item(key = item.address) {
                                    val value = formatDouble(item.balance)
                                    //TODO: use meethod in composable
                                    ethOSSimpleAssetListItemCustom(title = item.symbol, value = value.replace(",", ".").toDouble())
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                        }
                    }
                }
                is AssetsUiState.Error -> { }
            }
        }
    }
}

fun formatString(input: String): String {
    return input.uppercase()
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
        Text(text = title.uppercase(), fontFamily = Fonts.INTER, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.weight(0.4f), color = Color.White)
        Text(text = formatDouble(value),fontFamily = Fonts.INTER, fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.End, modifier = Modifier.weight(0.30f), color = Color.White )
    }
}

@Preview
@Composable
fun PreviewAssetList() {
    AssetList(
        AssetsUiState.Success(
            listOf(
                TokenAsset(
                    address = "",
                    chainId = 1,
                    symbol = "ETH",
                    name = "ETHER",
                    balance = 1.0,
                    decimals = 18,
                    swappable = true
                )
            )
        ),
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