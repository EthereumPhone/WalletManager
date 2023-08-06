package com.feature.send

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ethoscomponents.components.NumPad
import com.feature.send.ui.SwipeButton
import com.feature.send.ui.ethOSTextField

@Composable
fun SendRoute() {

}
@Composable
fun SendScreen(
    modifier: Modifier = Modifier
) {

    var test by remember { mutableStateOf("") }

    Column (
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF1E2730)).padding(horizontal = 24.dp, vertical = 32.dp)
    ){
        Text(
            text = "Send",
            style = TextStyle(
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
        ethOSTextField(
            text = test,
            label = "ENS or Address",
            trailingIcon = {
//                Icon(imageVector = Icons.Rounded.Lock,
//                    tint = Color.White,
//                    modifier = Modifier.size(32.dp),
//                    contentDescription = "")
                Icon(
                    tint= Color.White,
                    painter = painterResource(id = R.drawable.baseline_qr_code_24),
                    modifier = Modifier.size(32.dp),
                    contentDescription = ""
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {test = it}

        Text(
            text = "0.00 ETH",//"${walletAmount.ethAmount} ETH",
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.White
        )


        NumPad(
            value = "",
            modifier = Modifier
        ) {}
        SwipeButton(text = "Swipe to send", icon = Icons.Rounded.ArrowForward, completeIcon = Icons.Rounded.Check) {

        }


    }

}

@Preview
@Composable
fun PreviewSendScreen() {
    SendScreen()
}