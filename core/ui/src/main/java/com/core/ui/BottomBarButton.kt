package com.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
fun BottomBarButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
){
    IconButton(modifier = Modifier
        .clip(RoundedCornerShape(0.dp))
        .width(110.dp)
        .height(50.dp)
        .padding(bottom = 8.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Keep the icon to 24.dp
            icon()
            Text(
                text= text.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = dgenTurqoise,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 1.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }
    }
}