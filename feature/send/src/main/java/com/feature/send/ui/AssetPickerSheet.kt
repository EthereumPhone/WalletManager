package com.feature.send.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.core.data.model.dto.Contact
import com.core.model.TokenAsset
import com.core.ui.WmListItem
import com.feature.send.AssetUiState
import com.feature.send.R
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

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
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading...",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }



                }
                is AssetUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Assets",
                            fontSize = 20.sp,
                            color = Colors.WHITE,
                            fontWeight = FontWeight.Medium,
                        )
                    }


                }
                is AssetUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ){
                        Text(
                            text = "Error",
                            fontSize = 20.sp,
                            color = Colors.GRAY,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                }
                is AssetUiState.Success -> {

                    val filteredAssets = assets.assets
                    var sortedTokens = filteredAssets.sortedByDescending {
                        it.balance
                    }

                    LazyColumn(
                    ) {
                        sortedTokens.forEach { tokenasset ->
                            item(key = tokenasset.name) {
                                ethOSListItem(
                                    header=tokenasset.symbol,
                                    withSubheader = true,
                                    subheader = tokenasset.name,
                                    trailingContent = {
                                        //contact.
                                        Text(
                                            text = formatDouble(tokenasset.balance),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = Fonts.INTER,
                                            color = Color.White
                                        )
                                    },
                                    onClick={onChangeAssetClicked(tokenasset)}
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
fun ethOSListItem(
    withImage:Boolean = false,
    image: @Composable () -> Unit = {},
    header: String = "Header",
    withSubheader: Boolean = false,
    subheader: String = "Subheader",
    trailingContent: @Composable (() -> Unit)? = null,
    backgroundColor: Color = Colors.TRANSPARENT,
    colorOnBackground: Color = Colors.WHITE,
    subheaderColorOnBackground: Color = Colors.WHITE,
    onClick: () -> Unit = {},
    modifier: Modifier= Modifier

) {


    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() },
    ) {
        if(withImage){
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier
                    .clip(CircleShape)
                    .background(Colors.DARK_GRAY)
                    .size(56.dp)
            ) {
                image()
            }
        }

        ListItem(
            headlineContent = {
                Text(
                    text = header,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Colors.WHITE,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.width(160.dp)
                )
            },
            supportingContent = {
                if(withSubheader){
                    Text(
                        text = subheader,
                        color = Colors.GRAY,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.width(160.dp)
                    )
                }

            },
            trailingContent = trailingContent,

            colors = ListItemDefaults.colors(
                headlineColor = colorOnBackground,
                supportingColor = subheaderColorOnBackground,
                containerColor = backgroundColor
            )
        )

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