package com.feature.swap.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenWhite

@Composable
fun ExchangeRate(
    fromToken: String,
    toToken: String,
    toValue: String,
    fiatAmount: Double?,
    fontSize: TextUnit = 12.sp
) {
    Text(
        text = "1 \$$fromToken = $toValue $toToken ($fiatAmount)",
        fontFamily = SpaceMono,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        lineHeight = fontSize,
        letterSpacing = 0.sp,
        textDecoration = TextDecoration.None,
        color = dgenWhite
    )
}