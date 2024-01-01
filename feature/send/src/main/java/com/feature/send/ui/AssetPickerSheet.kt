package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.core.data.model.dto.Contact
import com.core.model.TokenAsset
import com.core.ui.WmListItem
import com.feature.send.AssetUiState
import com.feature.send.R

@Composable
fun AssetPickerSheet(
    //balancesState: AssetUiState,
    //getContacts: (Context) -> Unit,
    assets: AssetUiState,
    onChangeAssetClicked: (TokenAsset) -> Unit, //method, when asset is selected
    chainId: Int
    //swapTokenUiState: SwapTokenUiState,
    //searchQuery: String,
    //onQueryChange: (String) -> Unit,
    //onSelectAsset: (TokenAsset) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(
                RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
            //.background(Color(0xFF262626))
            .padding(start = 12.dp, end = 12.dp, bottom = 48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ){

            Text(
                text = "Assets",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            contentAlignment = Alignment.Center
        ) {
            when(assets){
                is AssetUiState.Loading -> {

                        Text(
                            text = "Loading...",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Normal,
                        )

                }
                is AssetUiState.Empty -> {

                    Text(
                        text = "No Assets",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                    )
                }
                is AssetUiState.Error -> {
                    Text(
                        text = "Error",
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal,
                    )
                }
                is AssetUiState.Success -> {

                    val filteredAssets = assets.assets

                    LazyColumn(
                    ) {
                        filteredAssets.forEach { tokenasset ->
                            item(key = tokenasset.name) {
                                WmListItem(
                                    headlineContent = {
                                            Text(
                                                text = tokenasset.symbol,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                    },
                                    supportingContent = {
                                        Text(
                                            text = tokenasset.name,
                                            color = Color(0xFF9FA2A5)
                                        )
                                    },
                                    trailingContent = {
                                        //contact.
                                        Text(
                                            text = formatDouble(tokenasset.balance),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        onChangeAssetClicked(tokenasset)
                                    }
                                )
                            }
                        }





                    }
                }
            }
        }






    }
}

@Composable
@Preview
fun AssetPickerSheetPreview(){
    AssetPickerSheet(

        AssetUiState.Success(
            listOf(
                TokenAsset(
                    chainId = 10,
                    name = "Optimism",
                    symbol = "ETH",
                    balance = 0.23,
                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
                ),
                TokenAsset(
                    chainId = 1,
                    name = "Mainnet",
                    symbol = "ETH",
                    balance = 1.43,
                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
                ),
                TokenAsset(
                    chainId = 1,
                    name = "DAI",
                    symbol = "ETH",
                    balance = 123.0,
                    address = "0xf2nd73b8gg74d880bds9fh042ybcdjn47bs92"
                ),
            )
        )
        ,
        {},
        1
    )
}