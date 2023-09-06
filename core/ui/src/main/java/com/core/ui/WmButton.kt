package com.core.ui

import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ethOSButton(
    modifier: Modifier = Modifier,
    text: String, enabled: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource =
        remember { MutableInteractionSource() }
) {

    //val interactionSource = remember { MutableInteractionSource() }
    //val isPressed by interactionSource.collectIsPressedAsState() //if pressed
    //val isHovering by interactionSource.collectIsFocusedAsState() //if hovered

    Button(
        interactionSource = interactionSource,
        onClick = {

                onClick()

        },
        shape = RoundedCornerShape(50.dp),
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            //.hoverable(interactionSource = interactionSource, enabled = true)
            .height(54.dp)
            .indication(interactionSource, rememberRipple(bounded = true, color = Color.Black))

        ,
        contentPadding= PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Color(0xFF9FA2A5),
            disabledContentColor = Color.White ,//if(primary) (if(isPressed) positive else warning ) else blue,
            contentColor = Color(0xFF24303D),
            containerColor = Color.White
        )
    ) {

        Row(
            horizontalArrangement =  Arrangement.Center,
            verticalAlignment =  Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ){

            Text(text=text, color= Color(0xFF24303D), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        }

    }

}

