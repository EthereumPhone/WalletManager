package com.example.transactions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.util.shimmerEffect
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts

@Composable
fun TransactionDetailItem(
    title: String,
    info: String
){
    Column (
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(text = title, color = Colors.GRAY,fontFamily = Fonts.INTER, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = info, color = Colors.WHITE, fontFamily = Fonts.INTER, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun LoadingTransactionDetailItem(){
    Column (
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Box(
            contentAlignment = Alignment.Center ,
            modifier = Modifier
                .height(14.dp)
                .width(100.dp)
                .clip(CircleShape)
                .shimmerEffect()

        ) {

        }

        Box(
            contentAlignment = Alignment.Center ,
            modifier = Modifier
                .height(24.dp)
                .width(240.dp)
                .clip(CircleShape)
                .shimmerEffect()

        ) {

        }
    }
}