package com.feature.send.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core.data.model.dto.Contact
import com.core.ui.util.PitagonsSans
import com.core.ui.util.SpaceMono
import com.core.ui.util.dgenGray
import com.core.ui.util.dgenWhite

@Composable
fun SelectedContact(
    contact: Contact,
    primaryColor: Color,
    secondaryColor: Color,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(primaryColor)
            .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {

                Text(
                    modifier = Modifier.widthIn(max=350.dp),
                    text = contact.name,
                    style = TextStyle(
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        color = secondaryColor
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )



        IconButton(
            onClick = onClear,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear contact",
                tint = secondaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}