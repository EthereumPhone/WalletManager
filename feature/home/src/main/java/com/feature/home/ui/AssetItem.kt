package com.feature.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.core.model.TokenAsset
import com.core.ui.WmListItem
import com.feature.home.R
import java.text.DecimalFormat

@Composable
fun AssetItem(
    asset: TokenAsset,
    modifier: Modifier = Modifier
) {
    Card(
    ) {
        WmListItem(
            headlineContent = {
                  Text(
                    text = asset.name
                  )
            },

            trailingContent = {
                Text(
                    text = asset.balance.toString()
                )
            }
        )
    }
}

@Composable
@Preview
private fun previewAssetItem() {
    val asset = TokenAsset(
        "0x123",
        1,
        "test",
        "test",
        0.0,
        10
    )

    AssetItem(asset = asset)
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
        5 ->
            Icon(
                painterResource(id = R.drawable.goerli),
                "Goerli Icon",
                modifier = modifier,
                tint = Color.Unspecified
            )
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