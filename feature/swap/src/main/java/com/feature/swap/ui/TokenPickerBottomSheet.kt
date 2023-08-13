package com.feature.swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.TokenAsset
import com.core.model.TokenMetadata
import com.core.ui.WmListItem
import com.core.ui.WmTextField

@Composable
fun TokenPickerSheet(
    tokenAssetList: List<TokenAsset>,
    query: String,
    onQueryChange: (String) -> Unit
) {
    Column {

        // query field
        WmTextField(
            value = query,
            onChange = { onQueryChange(it) },
            leadingIcon = { Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search",
                tint = Color.White
            ) }
        )

        // selectable
        LazyColumn {
            tokenAssetList.forEach { tokenAsset ->
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
                        }
                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun TokenPickerSheetPreview() {
    TokenPickerSheet(
        listOf(
            TokenAsset("", 1, "test", "test test", 123.2),
            TokenAsset("", 5, "test", "test test", 123.2),
            TokenAsset("", 10, "test", "test test", 123.2),
            TokenAsset("", 137, "test", "test test", 123.2),
            TokenAsset("", 42161, "test", "test test", 123.2),
        ),
        "",
        {}
    )
}