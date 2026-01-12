package com.core.ui.views

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.core.ui.Card
import com.core.ui.R
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenWhite
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * NFT Card View - displays an NFT with its image as background
 * Shows name, floor price in ETH and USD, with a send button
 */
@Composable
fun NftCardView(
    modifier: Modifier = Modifier,
    nftName: String,
    collectionName: String,
    imageUrl: String?,
    floorPriceEth: Double?,
    floorPriceUsd: Double?,
    navigateToSendNft: () -> Unit,
    primaryColor: Color,
) {
    val context = LocalContext.current
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    val ethFormat = DecimalFormat("0.####").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
    }

    // GIF-Loader for background images
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()

    Box(modifier = modifier.fillMaxSize()) {
        // NFT Image Background
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            imageLoader = gifEnabledLoader,
            contentDescription = nftName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.placeholer_icon_5),
            error = painterResource(R.drawable.placeholer_icon_5)
        )

        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Content
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        ) {
            // Top row: NFT Name and USD price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // NFT Name (replaces logo and token symbol)
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (nftName.length > 16) {
                            nftName.take(16).uppercase() + "..."
                        } else {
                            nftName.uppercase()
                        },
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.Normal,
                            fontSize = 24.sp,
                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (collectionName.length > 20) {
                            collectionName.take(20) + "..."
                        } else {
                            collectionName
                        },
                        style = TextStyle(
                            fontFamily = PitagonsSans,
                            color = dgenWhite.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // USD Price (floor price)
                val usdPrice = if (floorPriceUsd == null || floorPriceUsd == 0.0) {
                    ""
                } else if (floorPriceUsd < 0.01) {
                    "‹ $0.01"
                } else {
                    "$" + decimalFormat.format(floorPriceUsd)
                }

                Text(
                    text = usdPrice,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }

            // Bottom row: ETH balance and Send button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // ETH Floor Price (replaces big amount)
                val ethPrice = if (floorPriceEth == null || floorPriceEth == 0.0) {
                    "---"
                } else {
                    ethFormat.format(floorPriceEth) + " ETH"
                }
                val fontSize = calculateNftFontSize(ethPrice)

                Text(
                    text = ethPrice,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        lineHeight = fontSize / 3,
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )

                // Send NFT Button
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp, end = 16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                navigateToSendNft()
                            }
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.baseline_arrow_outward_24),
                            contentDescription = "Send NFT",
                            tint = primaryColor
                        )
                        Text(
                            text = "SEND",
                            style = TextStyle(
                                fontFamily = SpaceMono,
                                color = primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.sp,
                                textDecoration = TextDecoration.None
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Calculates font size based on the length of the text to display.
 */
private fun calculateNftFontSize(text: String): TextUnit {
    return when {
        text.length <= 6 -> 68.sp
        text.length <= 8 -> 56.sp
        text.length <= 10 -> 48.sp
        text.length <= 12 -> 40.sp
        text.length <= 14 -> 36.sp
        else -> 32.sp
    }
}

@Preview(
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun NftCardPreview() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        frontSide = {
            NftCardView(
                nftName = "Bored Ape #1234",
                collectionName = "Bored Ape Yacht Club",
                imageUrl = null,
                floorPriceEth = 12.5,
                floorPriceUsd = 25000.0,
                navigateToSendNft = { },
                primaryColor = Color.Red,
            )
        },
        primaryColor = Color.Red,
        secondaryColor = Color.Blue
    )
}

@Preview(
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun NftCardPreviewNoPrice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        frontSide = {
            NftCardView(
                nftName = "CryptoPunk #9999",
                collectionName = "CryptoPunks",
                imageUrl = null,
                floorPriceEth = null,
                floorPriceUsd = null,
                navigateToSendNft = { },
                primaryColor = Color.Cyan,
            )
        },
        primaryColor = Color.Cyan,
        secondaryColor = Color.Blue
    )
}
