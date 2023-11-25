package com.example.transactions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionDetailItem(
    title: String,
    info: String
){
    Column (
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Text(text = title, color = Color(0xFF9FA2A5), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = info, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}