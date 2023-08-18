package com.feature.send.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TextToggleButton(
    text: String,
    selected: MutableState<Boolean>,
    onClickChange: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Button(
        onClick = onClickChange,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected.value) Color.White else Color(0xFF24303D),
            contentColor = Color.White
        ),
        shape = CircleShape,
        //elevation = ButtonDefaults.elevation(0.dp, 0.dp),
        modifier = modifier.padding(14.dp, 0.dp),
    ) {
        Row(
            modifier = modifier.padding(0.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                color = if (selected.value) Color(0xFF24303D) else Color.White,
                modifier = modifier.padding(0.dp),
            )
        }
    }
}