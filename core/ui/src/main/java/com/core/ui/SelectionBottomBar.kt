package com.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.core.ui.util.dgenBlack

@Composable
fun SelectionBottomBar(
    onHide: () -> Unit,
    primaryColor: Color,
    hideLabel: String = "Hide"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(dgenBlack)
            .padding(start = 8.dp, end = 8.dp, top = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        BottomBarButton(
            primaryColor = primaryColor,
            onClick = onHide,
            icon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.hidden_assets),
                    contentDescription = hideLabel,
                    tint = primaryColor
                )
            },
            text = hideLabel
        )
    }
}
