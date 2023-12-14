package com.feature.swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.model.TokenAsset
import com.core.model.TokenMetadata
import com.core.ui.WmListItem
import com.core.ui.WmTextField
import com.feature.swap.SwapTokenUiState
import java.text.DecimalFormat
import kotlin.math.pow

fun getEtherscanDomainForChain(chainId: Int): String {
    return when(chainId) {
        1 -> "https://etherscan.io/"
        5 -> "https://goerli.etherscan.io/"
        10 -> "https://optimistic.etherscan.io/"
        137 -> "https://polygonscan.com/"
        42161 -> "https://arbiscan.io/"
        else -> ""
    }
}

@Composable
fun TokenPickerSheet(
    swapTokenUiState: SwapTokenUiState,
    searchQuery: String,
    filterByChain: Int? = null,
    onQueryChange: (String) -> Unit,
    onSelectAsset: (TokenAsset) -> Unit
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp
            ))
            .background(Color(0xFF262626))
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
            placeholder = {
                Text(
                    text = "Search tokens",
                    fontSize = 18.sp,
                    color = Color(0xFF9FA2A5)
                )
            }
        )

        LazyColumn {
            when(swapTokenUiState) {
                is SwapTokenUiState.Loading -> {

                }
                is SwapTokenUiState.Success -> {
                    var sortedTokens = swapTokenUiState.tokenAssets.sortedByDescending {
                        it.balance
                    }
                    filterByChain?.let {
                        sortedTokens = sortedTokens.filter {
                            it.chainId == filterByChain
                        }
                    }
                    sortedTokens.forEach { tokenAsset ->
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
                                    Row {
                                        Text(
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            text = formatDouble(tokenAsset.balance),
                                            fontSize = 18.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                        IconButton(
                                            onClick = {
                                                //opens InfoDialog
                                                val domain = getEtherscanDomainForChain(tokenAsset.chainId)
                                                val link = "${domain}token/${tokenAsset.address}"

                                                // Open link in browser
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                                intent.data = android.net.Uri.parse(link)
                                                context.startActivity(intent)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.OpenInNew,
                                                contentDescription = "Information",
                                                tint = Color(0xFF9FA2A5),
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }

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

                else -> {}
            }


        }




    }
}

fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
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
        null,
        {},
        {}
    )
}