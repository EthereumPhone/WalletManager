package org.ethereumphone.walletmanager.feature_swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ethereumphone.walletmanager.core.ui.WmTextField
import org.ethereumphone.walletmanager.core.ui.WmTextFieldDefaults


@Composable
fun TokenSelector(

    modifier: Modifier = Modifier
) {
    var inputTokenState by remember { mutableStateOf("") }
    var outputTokenState by remember { mutableStateOf("") }

    Box(
        Modifier.requiredHeightIn(200.dp, 200.dp)
    ) {
        TokenTextField(
            text = inputTokenState,
            trailingIcon =
            {

            },
            onTextChange = {inputTokenState = it},
            fiatValue = 0f,
            balance = 0f,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(5.dp))
        )
        TokenTextField(
            text = outputTokenState,
            trailingIcon = { /*TODO*/ },
            onTextChange = {outputTokenState = it},
            fiatValue = 0f,
            balance = 0f,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        Icon(
            imageVector = Icons.Rounded.SwapVert,
            contentDescription = "switch tokens",
            tint = WmTextFieldDefaults.WmDarkTextField().containerColor,
            modifier = Modifier
                .align(Alignment.Center)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.White)
                .size(32.dp)
        )
    }
}

@Composable
@Preview
fun TokenSelectorPreview() {
    TokenSelector()
}