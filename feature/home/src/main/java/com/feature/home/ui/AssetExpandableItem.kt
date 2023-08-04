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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.designsystem.theme.primary
import com.core.designsystem.theme.primaryVariant
import com.core.model.TokenAsset
import com.core.ui.ExpandableListItem
import com.feature.home.R

@Composable
fun AssetExpandableItem(
    modifier: Modifier = Modifier,
    title: String,
    assets: List<TokenAsset>
) {
    ExpandableListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(Color.Transparent),
        headline = {
            Row {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Text(
                    text = ": ${assets.sumOf { it.balance }}",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
        },
        support = {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                items(assets) { asset ->
                    chainToIconMapper(chainId = asset.chainId, size = 20.dp)
                }
            }

        },
        expandedContent = {
            Column(
                Modifier
                    .clip(RoundedCornerShape(0, 0, 5, 5))
                    .background(primaryVariant)
                ,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                assets.forEach {
                    IndividualAssetItem(it)
                }
            }
        }
    )
}

@Composable
private fun IndividualAssetItem(
    tokenAsset: TokenAsset
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        chainToIconMapper(tokenAsset.chainId, 25.dp)
        Text(
            text = tokenAsset.balance.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = primary
        )
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
        5 -> Box(
            modifier = modifier
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) { Text(
            text = "Gö",
            fontSize = 14.sp
        ) }
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
                title = "test",
                assets =
                listOf(
                    TokenAsset("", 1, "test", "test test", 123.2),
                    TokenAsset("", 5, "test", "test test", 123.2),
                    TokenAsset("", 10, "test", "test test", 123.2),
                    TokenAsset("", 137, "test", "test test", 123.2),
                    TokenAsset("", 42161, "test", "test test", 123.2),
                )

            )
        }
    }
}


