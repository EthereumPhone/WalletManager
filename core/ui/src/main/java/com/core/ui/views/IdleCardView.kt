package com.core.ui.views


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.R
import com.core.ui.util.formatSmart
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import com.example.dgenlibrary.ui.theme.dgenWhite
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
) {

    val decimalFormat = DecimalFormat("0.00").apply {
        decimalFormatSymbols = DecimalFormatSymbols(Locale.US) // Forces the decimal point
    }

    Box(
        modifier = Modifier.alpha(0.4f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier.size(400.dp).offset(x = 100.dp, y = 40.dp),
            painter = painterResource(R.drawable.placeholer_icon_5),
            contentDescription = "Ethereum"
        )
    }

    Column (
        verticalArrangement =  Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp,bottom = 8.dp, start = 16.dp, end = 16.dp),
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

//                if (icon != null){
//                    AsyncImage(
//                        modifier = Modifier.size(40.dp),
//                        model = icon,
//                        contentDescription = "Ethereum"
//                    )
//                } else {
                    Image(
                        modifier = Modifier
                            .padding(bottom = 2.dp)
                            .size(40.dp),
                        painter = painterResource(R.drawable.placeholer_icon_5),
                        contentDescription = "Ethereum"
                    )
//                }

                Text(
                    text = tokenName,
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

            Text(
                text= formatSmart(amount),
                style = TextStyle(
                    fontFamily = PitagonsSans,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 64.sp,
                    lineHeight = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }
    }
}


@Preview(
    widthDp = 447,
    heightDp = 447,
)
@Composable
fun IdlePreview(){
    IdleView(
        amount = 0.13,
        tokenName = "USDC",
        fiatAmount = 209.47,
        icon = R.drawable.placeholer_icon_5.toString()

    )
}
