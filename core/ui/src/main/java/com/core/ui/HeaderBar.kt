package com.core.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dgenlibrary.ui.theme.SpaceMono
import com.example.dgenlibrary.ui.theme.dgenTurqoise

@Composable
fun HeaderBar(
    text: String,
    onClick: () -> Unit
){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = SpaceMono,
                color = dgenTurqoise,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                letterSpacing = 0.sp,
                textDecoration = TextDecoration.None
            )
        )

        Icon(
            modifier = Modifier
                .size(32.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        onClick()
                    }
                },
            painter = painterResource(R.drawable.baseline_close_24),
            contentDescription = "Back",
            tint = dgenTurqoise
        )

    }
}