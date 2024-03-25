package com.core.ui

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun ethOSCenterTextFieldInline(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = false,//true,
    onTextChanged: (String) -> Unit,
    color: Color = Color.White,
) {

    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = FocusRequester()
    var fontSize by remember { mutableStateOf(size.sp) }



    BasicTextField(
        value = text,
        onValueChange = {
            onTextChanged(it)
            fontSize = size.sp
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                // Clear focus when the user presses the "Done" button on the keyboard
                focusRequester.requestFocus()
            }
        ),
        singleLine = singleLine,
        minLines = 1,
        maxLines = 2,
        textStyle = LocalTextStyle.current.copy(
            color = color,
            textAlign = TextAlign.Center,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }

            .onKeyEvent {
                Log.d("KEYEVENT12", "${it.key.keyCode} == ${Key.NumPadEnter.keyCode}")

                if ((it.nativeKeyEvent.keyCode == Key.Enter.nativeKeyCode) || (it.nativeKeyEvent.keyCode == Key.NumPadEnter.nativeKeyCode)) {
                    focusRequester.requestFocus()
                    true
                }
                false
            }
                ,
        cursorBrush = SolidColor(Color.White),


    ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (!isFocused && text.isEmpty()) {
                Text(
                    text = label,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = size.sp,
                    color = Color(0xFF9FA2A5),
                    modifier = Modifier.fillMaxWidth()



                )
            }
            innerTextField()
        }
    }
}







@SuppressLint("ComposableNaming")
@Composable
fun ethOSTextField(
    text: String,
    size: Int,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = false,//true,
    maxChar: Int = 42,
    sizeCut: Int = 2,
    numberInput: Boolean = false,
    onTextChanged: (String) -> Unit,
    color: Color = Color.White,
    center: Boolean = false,
) {

    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = FocusRequester()
    var fontSize by remember { mutableStateOf(size.sp) }
    //val keyboardController = LocalSoftwareKeyboardController.current




    BasicTextField(
        value = text,
        readOnly = false,
        onValueChange = {
            if(it.length < maxChar){
                onTextChanged(it)
            }
            fontSize = size.sp
        },
        keyboardActions = KeyboardActions(
            onDone = {
                // Clear focus when the user presses the "Done" button on the keyboard
                focusRequester.requestFocus()
            }
        ),
        keyboardOptions = if(numberInput) KeyboardOptions(keyboardType = KeyboardType.Number,imeAction = ImeAction.Done) else KeyboardOptions(keyboardType = KeyboardType.Text,imeAction = ImeAction.Done),
        singleLine = singleLine,
        minLines = 1,
        maxLines = 2,
        textStyle = LocalTextStyle.current.copy(
            color = color,
            fontSize = calculateFontSize(text.length,size,sizeCut),
            fontWeight = FontWeight.SemiBold
        ),

        modifier = modifier
            
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }

            .onKeyEvent {
                if ((it.nativeKeyEvent.keyCode == Key.Enter.nativeKeyCode) || (it.nativeKeyEvent.keyCode == Key.NumPadEnter.nativeKeyCode)) {
                    focusRequester.requestFocus()
                    true
                }
                false
            }
            .width(IntrinsicSize.Min)
//            .onPreviewKeyEvent { keyEvent: KeyEvent ->
//                if (keyEvent.key.keyCode == Key.NumPadEnter.keyCode) {
//                    // Handle the enter press here
//                    Log.d("KEYEVENT TRUE", "${keyEvent.key.keyCode} == ${Key.NumPadEnter.keyCode}")
//                    true // Indicate that the event has been handled
//                } else {
//                    Log.d("KEYEVENT FASLE", "${keyEvent.key.keyCode} == ${Key.NumPadEnter.keyCode}")
//
//                    false // Indicate that the event has not been handled
//                }
//            }
,
        cursorBrush = SolidColor(Color.White),
        //cursorBrush = SolidColor(if (isFocused) Color.White else Color.White),
    ) { innerTextField ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxWidth()
        ) {
            //if (!isFocused && text.isEmpty()) {
            if (text.isEmpty()) {
                Text(
                    text = label,
                    textAlign = if(center) TextAlign.Center else TextAlign.Start ,
                    fontWeight = FontWeight.Medium,
                    fontSize = size.sp,
                    color = Color(0xFF9FA2A5),
                    modifier = modifier.fillMaxWidth()
                )
            }
            innerTextField()
        }
    }





}



fun calculateFontSize(length: Int, defaultSize: Int, maxChar: Int ): TextUnit {
    val defaultFontSize = defaultSize.sp
    val maxChars = maxChar
    val scaleFactor = 0.8

    var adjustedSize = if (length > maxChars) {
        val scaledSize = (defaultFontSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        result.sp
    } else {
        defaultFontSize
    }

    if (length > maxChars*2) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        Log.e("Length: ", "${ adjustedSize }")
    }

    if (length > maxChars*3) {
        val scaledSize = (adjustedSize * scaleFactor).value
        val minValue = 12f // Minimum font size
        val result = max(scaledSize, minValue)
        adjustedSize = result.sp
        Log.e("Length: ", "${ adjustedSize }")
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
//        ethOSTextField(
//            text = test,
//            size = 64,
//            label = "0",
//            onTextChanged = { value -> test = value }
//
////                modifier = Modifier.fillMaxWidth(),
//
//
//
//        )
        //TextField(value = test, onValueChange = {test = it})
    }

}
