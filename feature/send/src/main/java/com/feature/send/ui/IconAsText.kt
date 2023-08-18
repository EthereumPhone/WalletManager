package com.example.ethoscomponents.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ethoscomponents.components.NumPad
import com.example.ethoscomponents.components.NumPadDefaults
import com.feature.send.R

@Composable
fun IconAsText(
    //icon: ImageVector,
    modifier: Modifier = Modifier,
    //contentColor: Color = NumPadDefaults.contentColor,
) {
    val myId = "inlineContent"
    val text = buildAnnotatedString {
        append("")
        appendInlineContent(myId, "[icon]")
    }
    val inlineContent = mapOf(
        Pair(
            myId,
            InlineTextContent(
                Placeholder(
                    width = NumPadDefaults.fontSize,
                    height = NumPadDefaults.fontSize,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.AboveBaseline
                )
            ) {
//                Icon(
//                    imageVector = icon,
//                    contentDescription = null,
//                    tint = contentColor,
//                )
//                Image(
//                    painterResource(R.drawable.baseline_backspace_24),
//                    contentDescription = "",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.fillMaxSize().background(Color.Red)
//                )
                Icon(
                    painter = painterResource(id = R.drawable.baseline_backspace_24),
                    contentDescription = null // decorative element
                )
            }
        )
    )
    Text(text = text,
        inlineContent = inlineContent,
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewNumPad() {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            tint= Color.White,
            painter = painterResource(id = R.drawable.baseline_backspace_24),
            contentDescription = null // decorative element
        )
    }
}

