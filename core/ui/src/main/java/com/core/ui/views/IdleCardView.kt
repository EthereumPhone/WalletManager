package com.core.ui.views


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import coil3.compose.AsyncImage
import com.core.ui.Card
import com.core.ui.R
import com.core.ui.util.formatSmart
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
import kotlinx.coroutines.launch
import org.ethosmobile.components.library.utils.SnackbarState
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
    navigateToSend: () -> Unit
) {

    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    Box(
        modifier = Modifier.alpha(0.4f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .size(700.dp)
                .offset(x = 100.dp, y = 50.dp),
            painter = painterResource(R.drawable.iso),
            contentDescription = "Ethereum"
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
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                when(icon){
                    "ETH" -> {
                        Image(
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .size(32.dp),
                            painter = painterResource(R.drawable.ethereum_placeholder),
                            contentDescription = "Ethereum"
                        )
                    }
                    "" -> {
                        Image(
                            modifier = Modifier
                                .padding(bottom = 2.dp)
                                .size(32.dp),
                            painter = painterResource(R.drawable.placeholer_icon_5),
                            contentDescription = "Ethereum"
                        )
                    }
                    else -> {
                        AsyncImage(
                            modifier = Modifier.padding(bottom = 2.dp).clip(CircleShape).size(32.dp),
                            model = icon,
                            contentDescription = "Translated description of what the image contains"
                        )
                    }
                }

                Text(
                    text = tokenName.uppercase(),
                    style = TextStyle(
                        fontFamily = SpaceMono,
                        color = dgenTurqoise,
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,

                        letterSpacing = 0.sp,
                        textDecoration = TextDecoration.None
                    )
                )
            }


            Text(
                text="$" + decimalFormat.format(fiatAmount),
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenWhite,
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,

                    textDecoration = TextDecoration.None
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ){
            val formattedAmount = formatSmart(amount)
            val fontSize = calculateFontSize(formattedAmount)
            
            Text(
                text = formattedAmount,
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
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

            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.baseline_arrow_outward_24),
                        contentDescription = "Back",
                        tint = dgenTurqoise
                    )
                    Text(
                        text= "SEND",
                        style = TextStyle(
                            fontFamily = SpaceMono,
                            color = dgenTurqoise,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
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
            .fillMaxWidth()
        ,
        frontSide = {
            IdleView(
                amount = 0.13,
                tokenName = "USDC",
                fiatAmount = 209.47,
                icon = "",//R.drawable.placeholer_icon_5.toString()
                navigateToSend = {  },

            )
        },
    )

}


