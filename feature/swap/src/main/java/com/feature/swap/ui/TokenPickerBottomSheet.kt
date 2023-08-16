package com.feature.swap.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.TokenAsset
import com.core.model.TokenMetadata
import com.core.ui.WmListItem
import com.core.ui.WmTextField
import com.feature.swap.SwapTokenUiState

@Composable
fun TokenPickerSheet(
    swapTokenUiState: SwapTokenUiState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSelectAsset: (TokenAsset) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // query field
        WmTextField(
            value = searchQuery,
            onChange = { onQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search",
                tint = Color.White
            ) },
            placeHolder = {
                Text(text = "Search tokens")
            }
        )

        LazyColumn {
            when(swapTokenUiState) {
                is SwapTokenUiState.Loading -> {

                }
                is SwapTokenUiState.Success -> {

                    swapTokenUiState.tokenAssets.forEach { tokenAsset ->
                        item(key = tokenAsset.address) {
                            WmListItem(
                                headlineContent = {
                                    Text(text = tokenAsset.symbol)
                                },
                                supportingContent = {
                                    Text(text = tokenAsset.name)
                                },
                                trailingContent = {
                                    Text(text = tokenAsset.balance.toString())
                                },
                                modifier = Modifier.clickable {
                                    onSelectAsset(tokenAsset)
                                }
                            )
                        }
                    }
                }

                is SwapTokenUiState.Error -> {

                }
            }


        }




    }
}



@Preview
@Composable
fun TokenPickerSheetPreview() {
    TokenPickerSheet(
        SwapTokenUiState.Success(
            listOf(
                TokenAsset("1", 1, "test", "test test", 123.2),
                TokenAsset("2", 5, "test", "test test", 123.2),
                TokenAsset("3", 10, "test", "test test", 123.2),
                TokenAsset("4", 137, "test", "test test", 123.2),
                TokenAsset("5", 42161, "test", "test test", 123.2),
            )
        ),
        "",
        {},
        {}
    )
}