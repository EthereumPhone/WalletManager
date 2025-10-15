package com.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import com.core.ui.util.SpaceMono
import com.core.ui.util.mediumEnterDuration
import com.core.ui.util.mediumExitDuration
import com.core.ui.util.rememberDebouncedClickHandler

@Composable
fun HeaderBar(
    modifier: Modifier = Modifier,
    text: String = "",
    enableCancel: Boolean = true,
    content: @Composable () -> Unit = {},
    primaryColor: Color,
    onClick: () -> Unit = {}
){
    var enabled by remember { mutableStateOf(true) }


    Row (
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    )
    {
        if (text == ""){
            content()
        } else {
            Text(
                text = text.uppercase(),
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 24.sp,
                    letterSpacing = 0.sp,
                    textDecoration = TextDecoration.None
                )
            )
        }



        AnimatedVisibility(
            modifier = Modifier.size(56.dp),
            visible = enableCancel,
            enter = fadeIn(tween(mediumEnterDuration)),
            exit  = fadeOut(tween(mediumExitDuration))
        ) {
            IconButton(modifier = Modifier.size(56.dp),
                onClick = dropUnlessResumed {
                    if (!enabled) return@dropUnlessResumed
                    enabled = false                      // one-shot guard
                    onClick()
                }) {
                Box(modifier = Modifier.size(56.dp)) {
                    Icon(
                        modifier = Modifier.size(32.dp).align(Alignment.CenterEnd),
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = "Back",
                        tint = primaryColor
                    )
                }
            }
        }
    }
}