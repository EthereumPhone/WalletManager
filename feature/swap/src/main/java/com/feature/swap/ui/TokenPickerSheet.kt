package com.feature.swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.core.model.TokenMetadata
import com.core.ui.WmListItem
import com.core.ui.WmTextField

@Composable
fun TokenPickerSheet(
    tokenMetadata: List<TokenMetadata>,
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

        LazyColumn {
            tokenMetadata.forEach {
                item(key = it.contractAddress) {
                    WmListItem(
                        headlineContent = {

                        },
                        supportingContent = {

                        }
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun BottomSheetDialogPreview() {
    TokenPickerSheet(
        emptyList(),
        "",
        {}
    )
}