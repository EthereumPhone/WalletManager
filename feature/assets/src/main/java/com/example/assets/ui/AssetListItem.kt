package com.example.assets.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

import com.core.model.TokenAsset
import com.feature.assets.R
import org.ethosmobile.components.library.theme.Colors
import java.text.DecimalFormat

@Composable
fun AssetListItem(
    title: String,
    assets: List<TokenAsset>,
    linkTo: () -> Unit = {},
) {


    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                linkTo()
            }
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,

        ){
            if(!assets[0].logoUrl.isNullOrEmpty()) {
                AssetListItemHeaderImage(assets[0].logoUrl)
            }


            Text(
                truncateString(title.uppercase()),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Text(
                        formatDouble(assets.sumOf { it.balance }),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                //TODO: Dollar Amount

            }
            Spacer(modifier = Modifier.width(24.dp))

            IconButton(
                onClick = linkTo,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    contentDescription = "Forward",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }


        }
    }
}


@Composable
fun AssetListDetailItem(
    tokenAsset: TokenAsset,
){
    val networkname = when(tokenAsset.chainId){
        1 -> "Mainnet"
        5 -> "Görli"
        10 -> "Optimism"
        137 -> "Polygon"
        8453 -> "Base"
        42161 -> "Arbitrum"
        7777777 -> "Zora"
        else -> { ""}
    }
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ){
        Text(networkname, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ){
                Row (
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,

                    ){
                    Text(formatDouble(tokenAsset.balance), color = Color.White,fontSize = 16.sp, fontWeight = FontWeight.Medium)

                    Text(tokenAsset.symbol.uppercase(), color = Color.White,fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                //Text("$0.00-", color = Color(0xFF9FA2A5),fontSize = 16.sp,fontWeight = FontWeight.Medium )
            }

        }
    }
}

@Composable
fun AssetListItemHeaderImage(
    headerImageUrl: String?
) {
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val imageLoader = rememberAsyncImagePainter(
        model = headerImageUrl,
        onState = { state ->
            isLoading = state is AsyncImagePainter.State.Loading
            isError = state is AsyncImagePainter.State.Error
        }
    )

    // check if previewMode
    val isLocalInspection = LocalInspectionMode.current
    Box(
        contentAlignment = Alignment.Center ,
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Colors.DARK_GRAY)
    ) {
        if (isLoading) {

        }
        Image(
            modifier = Modifier,
            contentScale = ContentScale.Crop,
            painter = if(isError.not() && !isLocalInspection) {
                imageLoader
            } else {
                //TODO: add placeholder
                painterResource(id = R.drawable.placeholder_icon)
            },
            contentDescription = null
        )

    }

}


fun formatDouble(input: Double): String {
    val decimalFormat = DecimalFormat("#.#####")
    return decimalFormat.format(input)
}

fun truncateString(input: String): String {
    return if (input.length > 14) input.substring(0, 14) + "..." else input
}



@SuppressLint("SuspiciousIndentation")
@Composable
@Preview
fun PreviewAssetListItem(){


    val token = TokenAsset(
        address = "",
        chainId = 1,
        symbol = "ETH",
        name = "Super long spammy name",
        balance = 0.61,
        decimals = 3,
        logoUrl = "https://icons.iconarchive.com/icons/cjdowner/cryptocurrency-flat/256/Ethereum-ETH-icon.png"//"https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/9720e55c-d222-4769-90b8-aec2262c0988/ddvtmz1-cadfaa7f-6da9-4b59-a0fe-6ed5742af38c.jpg/v1/fill/w_1192,h_670,q_70,strp/bruh_by_jukeboxfromao_ddvtmz1-pre.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NzIwIiwicGF0aCI6IlwvZlwvOTcyMGU1NWMtZDIyMi00NzY5LTkwYjgtYWVjMjI2MmMwOTg4XC9kZHZ0bXoxLWNhZGZhYTdmLTZkYTktNGI1OS1hMGZlLTZlZDU3NDJhZjM4Yy5qcGciLCJ3aWR0aCI6Ijw9MTI4MCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.OTeZgFcV45DZqyg43rAeGzSld3mOIMTCffVyi3SGM8o"
    )

    val list = listOf(
        token,
        token,
        token
    )

    AssetListItem(token.name,list, {})


}
