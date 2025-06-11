package com.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenTurqoise

@Composable
fun HeaderBar(
    modifier: Modifier = Modifier,
    text: String = "",
    content: @Composable () -> Unit = {},
    onClick: () -> Unit
){
    // Debouncing-State für das Verhindern von mehrfachen schnellen Klicks
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val debounceDelay = 500L // 500ms Verzögerung zwischen Klicks
    
    Row (
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        if (text == ""){
            content()
        } else {
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
        }


        Icon(
            modifier = Modifier
                .size(32.dp)
                .clickable {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime >= debounceDelay) {
                        lastClickTime = currentTime
                        onClick()
                    }
                }
            ,
            painter = painterResource(R.drawable.baseline_close_24),
            contentDescription = "Back",
            tint = dgenTurqoise
        )

    }
}