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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ethosmobile.components.library.theme.Colors
import org.ethosmobile.components.library.theme.Fonts


//

@Composable
fun ethOSTextButton(
    modifier: Modifier = Modifier,
    text: String, enabled: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource =
        remember { MutableInteractionSource() }
) {
    TextButton(
        interactionSource = interactionSource,
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .indication(interactionSource, rememberRipple(bounded = true, color = Colors.BLACK)),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = Colors.TRANSPARENT,
            disabledContentColor = Colors.GRAY ,//if(primary) (if(isPressed) positive else warning ) else blue,
            contentColor = Colors.WHITE,
            containerColor = Colors.TRANSPARENT
        )
    ) {

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material.Text(
                text=text,
                color= Colors.WHITE,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                fontFamily = Fonts.INTER
            )

        }

    }

}


@Preview
@Composable
private fun PreviewButton() {
    ethOSTextButton(
        onClick = { /*TODO*/ },
        enabled = true,
        text = "Send"
    )
}