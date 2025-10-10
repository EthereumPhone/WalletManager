package com.core.ui.views


import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.core.ui.Card
import com.core.ui.R
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.formatWithSuffix
import com.core.ui.util.dgenWhite
import com.core.ui.util.TokenLogoFallback
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Composable
fun IdleView(
    modifier: Modifier = Modifier,
    amount: Double,
    tokenName: String,
    fiatAmount: Double,
    icon: String?,
    navigateToSend: () -> Unit,
    enableSend: Boolean,
    primaryColor: Color,
) {

    val context = LocalContext.current
    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    //GIF-Loader for wireframe globe gif
    val gifEnabledLoader = ImageLoader.Builder(context)
        .components {
            if ( SDK_INT >= 28 ) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()



        Box(
            modifier = Modifier.alpha(0.2f).offset(x = 100.dp, y = 100.dp).scale(0.9f).aspectRatio(1f),
            contentAlignment = Alignment.CenterEnd
        ) {

            AsyncImage(
                imageLoader = gifEnabledLoader,
                model = R.drawable.wireframe_torus,
                contentDescription = null,
                colorFilter = ColorFilter.tint(primaryColor)
            )
        }

        Column (
            verticalArrangement =  Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
        ){

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Check if we have a valid icon URL, otherwise use fallback
                    val effectiveIcon = when {
                        !icon.isNullOrEmpty() && icon != "ETH" && icon != "MATIC" -> icon
                        else -> {
                            // Try to get fallback logo for this token
                            val fallbackLogo = TokenLogoFallback.getFallbackLogo(tokenName)
                            when (fallbackLogo) {
                                is TokenLogoFallback.LogoSource.Url -> fallbackLogo.url
                                is TokenLogoFallback.LogoSource.LocalResource -> null // Will handle local resources below
                                null -> null
                            }
                        }
                    }

                    when {
                        // Handle special case for ETH hardcoded icon
                        icon == "ETH" -> {
                            Image(
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationX = 5f
                                    }
                                    .padding(bottom = 2.dp)
                                    .height(48.dp)
                                    .width(46.dp)
                                    .clip(RoundedCornerShape(95)),
                                contentScale = ContentScale.Crop,
                                painter = painterResource(R.drawable.ethereum_placeholder),
                                contentDescription = "Ethereum"
                            )
                        }
                        // Handle local resource fallbacks
                        effectiveIcon == null -> {
                            val fallbackLogo = TokenLogoFallback.getFallbackLogo(tokenName)
                            when (fallbackLogo) {
                                is TokenLogoFallback.LogoSource.LocalResource -> {
                                    Image(
                                        modifier = Modifier
                                            .graphicsLayer {
                                                rotationX = 5f
                                            }
                                            .padding(bottom = 2.dp)
                                            .height(48.dp)
                                            .width(46.dp)
                                            .clip(RoundedCornerShape(95)),
                                        contentScale = ContentScale.Crop,
                                        painter = painterResource(fallbackLogo.resourceId),
                                        contentDescription = tokenName
                                    )
                                }
                                else -> {
                                    // Default placeholder if no fallback exists
                                    Image(
                                        modifier = Modifier
                                            .graphicsLayer {
                                                rotationX = 5f
                                            }
                                            .padding(bottom = 2.dp)
                                            .height(48.dp)
                                            .width(46.dp)
                                            .clip(RoundedCornerShape(95)),
                                        contentScale = ContentScale.Crop,
                                        painter = painterResource(R.drawable.placeholer_icon_5),
                                        colorFilter = ColorFilter.tint(primaryColor),
                                        contentDescription = "Token placeholder"
                                    )
                                }
                            }
                        }
                        // Handle URL icons (either from API or fallback)
                        else -> {
                            val shouldTint = effectiveIcon.isEmpty()
                            
                            Log.d("IdleCardView", "Loading image URL: $effectiveIcon")

                            // AsyncImage will automatically use the ImageLoader from ImageLoaderFactory
                            AsyncImage(
                                modifier = Modifier
                                    .padding(bottom = 2.dp)
                                    .height(48.dp)
                                    .width(46.dp)
                                    .clip(RoundedCornerShape(95)),
                                contentScale = ContentScale.Crop,
                                model = ImageRequest.Builder(context)
                                    .data(effectiveIcon)
                                    .crossfade(true)
                                    .listener(
                                        onSuccess = { _, _ ->
                                            Log.d("IdleCardView", "Successfully loaded image: $effectiveIcon")
                                        },
                                        onError = { _, result ->
                                            Log.e("IdleCardView", "Failed to load image: $effectiveIcon", result.throwable)
                                        }
                                    )
                                    .build(),
                                contentDescription = tokenName,
                                placeholder = painterResource(R.drawable.placeholer_icon_5),
                                error = painterResource(R.drawable.placeholer_icon_5),
                                colorFilter = if (shouldTint) ColorFilter.tint(primaryColor) else null
                            )
                        }
                    }

                    Text(
                        text = if (tokenName.length > 10) {
                            tokenName.take(10).uppercase() + "..."
                        } else {
                            tokenName.uppercase()
                        },
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = primaryColor,
                            fontWeight = FontWeight.Normal,
                            fontSize = 30.sp,

                            letterSpacing = 0.sp,
                            textDecoration = TextDecoration.None
                        )
                    )
                }

                val price = if (fiatAmount == 0.0 ) {
                    "N/A"
                } else if (fiatAmount < 0.01 && fiatAmount != 0.0) {
                    "‹ $0.01"
                } else {
                    "$" + decimalFormat.format(fiatAmount)

                }


                Text(
                    text = price,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = dgenWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,

                        textDecoration = TextDecoration.None
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ){
                val formattedAmount = amount.formatWithSuffix() //formatSmart(amount)
                val fontSize = calculateFontSize(formattedAmount)

                Text(
                    text = formattedAmount,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        lineHeight = fontSize / 3,  // Adjust lineHeight proportionally
                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )

                Box(
                    modifier = Modifier.padding(bottom = 8.dp, end = 16.dp)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                navigateToSend()
                            }
                        }
                        .alpha(if (enableSend) 1f else 0.2f)

                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.baseline_arrow_outward_24),
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                        Text(
                            text= "SEND",
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

/**
 * Calculates font size based on the length of the text to display.
 * Shorter text gets larger font size, longer text gets smaller font size.
 */
private fun calculateFontSize(text: String): TextUnit {
    return when {
        text.length <= 4 -> 80.sp
        text.length <= 6 -> 68.sp
        text.length <= 8 -> 56.sp
        text.length <= 10 -> 48.sp
        text.length <= 12 -> 40.sp
        else -> 36.sp
    }
}

@Preview(
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun IdlePreview(){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        frontSide = {
            IdleView(
                amount = 0.13,
                tokenName = "USDCaaaaaaaaaaaaaaaaaaaaaa",
                fiatAmount = 209.47,
                icon = "",//R.drawable.placeholer_icon_5.toString()
                navigateToSend = { },
                enableSend = false,
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
fun IdlePreviewEmtpy(){
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        frontSide = {
            IdleView(
                amount = 0.13,
                tokenName = "USDCaaaaaaaaaaaaaaaaaaaaaa",
                fiatAmount = 0.0,
                icon = "",//R.drawable.placeholer_icon_5.toString()
                navigateToSend = { },
                enableSend = false,
                primaryColor = Color.Red,
            )
        },
        primaryColor = Color.Red,
        secondaryColor = Color.Blue
    )



}


