package com.core.ui

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dgenlibrary.ui.theme.dgenOcean
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import com.core.ui.views.IdleView
import com.example.dgenlibrary.ui.theme.dgenBlack
import com.example.dgenlibrary.ui.theme.dgenTurqoise
import java.util.Locale

@SuppressLint("SuspiciousIndentation")
@Composable
fun Card(
    isFirst: Boolean = true,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    frontSide: @Composable () -> Unit = {},
    backSide: @Composable () -> Unit = {},
){

    val backgroundColor by animateColorAsState(
        targetValue = if (isFirst) dgenOcean else dgenBlack,
        animationSpec = tween(durationMillis = 500),
        label = "ColorAnimation"
    )
    val frontVisible = rotation < 90f
    val backVisible = rotation > 90f

    Surface(
        color = backgroundColor,
        modifier = modifier
            .aspectRatio(16f / 9f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp))
            .border(1.dp, dgenTurqoise, RoundedCornerShape(0.dp))
    ) {

        if (frontVisible) {
            frontSide()
        }
        if (backVisible) {
            backSide()
        }

    }

}

data class Asset(
    val amount: Double,
    val tokenName: String,
    val fiatAmount: Double,
    val icon: Int,
)
enum class TOKENACTION{
    IDLE,
    SEND,
    SWAP,
    GET
}

@Preview
@Composable
fun PreviewCard(){
//    Card(
//        frontSide = {
//            IdleView(
//                amount = 0.13,
//                tokenName = "USDC",
//                fiatAmount = 209.47,
//                icon = R.drawable.optimism_logo,
//            )
//        },
//    )
}