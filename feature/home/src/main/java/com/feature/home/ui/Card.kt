package com.feature.home.ui

import android.graphics.drawable.Drawable
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun Card(
    modifier: Modifier = Modifier,
//    amount: Int,
//    tokenName: String,
//    fiatAmount: Int,
//    chainList: List<String>,
//    backgroundColor: Color,
//    icon: Drawable,
){
    Surface(
        modifier = Modifier.aspectRatio(16f/9f).size(300.dp).background(Color.Red)
    ) {
        Box {

        }
    }
}

@Preview
@Composable
fun PreviewCard(){
    Card()
}