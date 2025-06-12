package com.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenTurqoise

@Composable
fun BottomBarButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    text: String,
    primaryColor: Color
) {
    Button(
        modifier = Modifier
            .clip(RoundedCornerShape(0.dp))
            .padding(bottom = 0.dp)
            .width(IntrinsicSize.Min),
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = primaryColor,
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(8.dp)

        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Keep the icon to 24.dp
            icon()
            Text(
                text = text.uppercase(),
                modifier = Modifier,
                style = TextStyle(
                    fontFamily = SpaceMono,
                    color = primaryColor,
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