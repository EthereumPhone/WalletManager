package com.feature.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.ethOSTextField

@Composable
fun SendRoute() {

}
@Composable
fun SendScreen(
    modifier: Modifier = Modifier
) {

    var test by remember { mutableStateOf("") }

    Column {
        Text(text = "Elie")
        ethOSTextField(
            text = test,
            label = "ENS or Address",
            trailingIcon = {
                Icon(imageVector = Icons.Rounded.Lock,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                    contentDescription = "")
            },
            modifier = Modifier.fillMaxWidth()
        ) {test = it}
        NumPad(
            value = "",
            modifier = Modifier
        ) {}


    }

}

@Preview
@Composable
fun PreviewSendScreen() {
    SendScreen()
}