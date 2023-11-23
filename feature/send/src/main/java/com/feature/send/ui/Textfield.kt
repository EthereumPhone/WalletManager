package com.feature.send.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun ethOSCenterTextField(
    text: String,
    title: String,
    //focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier,
    label: String = "",
    center: Boolean = false,
    singleLine: Boolean = false,//true,
    onTextChanged: (String) -> Unit
) {

    var isFocused by remember { mutableStateOf(false) }
    //val focusRequester = FocusRequester()
    var fontSize by remember { mutableStateOf(16.sp) }

    Column(
        horizontalAlignment = if(center) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9FA2A5),
            fontSize = 20.sp
        )
        BasicTextField(
            value = text,
            onValueChange = {
                onTextChanged(it)
                fontSize = calculateFontSize(it.length)
            },
            singleLine = singleLine,
            minLines = 1,
            maxLines = 2,
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            ),

//            textStyle = TextStyle(
////                textAlign = TextAlign.Center,
////                fontSize =  24.sp,
////                fontWeight = FontWeight.SemiBold,
////            ),
            cursorBrush = SolidColor(Color.White),
//            modifier = modifier
//                .clip(RoundedCornerShape(10))
//                .background(Color.Magenta)
//                .height(64.dp)


        ) { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = modifier.widthIn(0.dp, 300.dp)

            ) {
                if (!isFocused && text.isEmpty()) {
                    Text(
                        text = label,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        color = Color(0xFF9FA2A5),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

//                    if(text == "") {
//                        Text(
//                            label,
//                            color = Color.White,
//                            fontSize = 32.sp,
//                            fontWeight= FontWeight.SemiBold,
//                            modifier = Modifier.align(Alignment.CenterStart)
//                        )
//                    }
                    innerTextField()

            }
        }
    }

}



fun calculateFontSize(length: Int): TextUnit {
    val defaultFontSize = 18.sp
    val maxChars = 25
    val scaleFactor = 0.8

    var adjustedSize = if (length > maxChars) {
        val scaledSize = (defaultFontSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        result.sp
    } else {
        defaultFontSize
    }

//    if (length > (maxChars*2) - 15) {
//        val scaledSize = (adjustedSize * scaleFactor).value
//        val minValue = 12f // Minimum font size
//        val result = max(scaledSize, minValue)
//        adjustedSize = result.sp
//    }




    return adjustedSize
}


@Preview
@Composable
fun PreviewTextField() {

    var test by remember { mutableStateOf("") }//0x2ade3187F051796542aF4f320c430fF9Bdd9507F
    val focusRequester = remember { FocusRequester() }
    //val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ethOSCenterTextField(
                text = test,
                title = "To",
                label = "(Enter address or ENS)",

                modifier = Modifier.fillMaxWidth(),
                center = true


            ) { value -> test = value }
            //TextField(value = test, onValueChange = {test = it})
        }

}