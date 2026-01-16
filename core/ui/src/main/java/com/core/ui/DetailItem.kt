package com.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.body1_fontSize
import com.core.ui.util.dgenWhite
import com.core.ui.util.label_fontSize

@Composable
fun DetailItem(
    label: String,
    value: String,
    primaryColor: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(end=24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = SpaceMono,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = label_fontSize,
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = PitagonsSans,
                color = dgenWhite,
                fontWeight = FontWeight.Medium,
                fontSize = body1_fontSize,
            )
        )
    }
}