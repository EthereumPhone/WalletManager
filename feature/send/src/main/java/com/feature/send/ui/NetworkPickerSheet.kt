package com.feature.send.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.ui.WmListItem
import com.core.ui.WmTextField
import com.feature.send.AssetUiState

@Composable
fun NetworkPickerSheet(
    balancesState: AssetUiState,
    onSelectAsset: (TokenAsset) -> Unit //method, when asset is selected
    //swapTokenUiState: SwapTokenUiState,
    //searchQuery: String,
    //onQueryChange: (String) -> Unit,
    //onSelectAsset: (TokenAsset) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp
            ))
            
            .background(Color(0xFF24303D))
    ) {

        LazyColumn {
            when(balancesState) {
                is AssetUiState.Loading -> {}

                is AssetUiState.Success -> {
                    //list all balances
                    balancesState.assets.forEach { tokenAsset ->
                        item(key = tokenAsset.address) {
                            WmListItem(
                                headlineContent = {
                                    Text(
                                        text = tokenAsset.symbol.uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        text = tokenAsset.name,
                                        color = Color(0xFF9FA2A5)
                                    )
                                },
                                trailingContent = {
                                    Text(
                                        text = tokenAsset.balance.toString() + " " + if(tokenAsset.chainId == 137) "MATIC" else "ETH",
                                        fontSize = 18.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium

                                    )
                                },
                                modifier = Modifier.clickable {
                                    onSelectAsset(tokenAsset)
                                }
                            )
                        }

                    }
                }

                is AssetUiState.Error -> {}
                else -> {}
            }
//            when(swapTokenUiState) {
//                is SwapTokenUiState.Loading -> {
//
//                }
//                is SwapTokenUiState.Success -> {
//
//                    swapTokenUiState.tokenAssets.forEach { tokenAsset ->
//                        item(key = tokenAsset.address) {
//                            WmListItem(
//                                headlineContent = {
//                                    Text(
//                                        text = tokenAsset.symbol.uppercase(),
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.SemiBold
//                                    )
//                                },
//                                supportingContent = {
//                                    Text(
//                                        text = tokenAsset.name,
//                                        color = Color(0xFF9FA2A5)
//                                    )
//                                },
//                                trailingContent = {
//                                    Text(
//                                        text = tokenAsset.balance.toString(),
//                                        fontSize = 18.sp,
//                                        color = Color.White,
//                                        fontWeight = FontWeight.Medium
//
//                                    )
//                                },
//                                modifier = Modifier.clickable {
//                                    onSelectAsset(tokenAsset)
//                                }
//                            )
//                        }
//                    }
//                }
//
//                is SwapTokenUiState.Error -> {
//
//                }
//            }


        }




    }
}



@Preview
@Composable
fun TokenPickerSheetPreview() {
//    TokenPickerSheet(
//        SwapTokenUiState.Success(
//            listOf(
//                TokenAsset("1", 1, "test", "test test", 123.2),
//                TokenAsset("2", 5, "test", "test test", 123.2),
//                TokenAsset("3", 10, "test", "test test", 123.2),
//                TokenAsset("4", 137, "test", "test test", 123.2),
//                TokenAsset("5", 42161, "test", "test test", 123.2),
//            )
//        ),
//        "",
//        {},
//        {}
//    )
}