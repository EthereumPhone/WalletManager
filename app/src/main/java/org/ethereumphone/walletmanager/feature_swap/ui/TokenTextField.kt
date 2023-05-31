package org.ethereumphone.walletmanager.feature_swap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumphone.walletmanager.core.ui.WmTextField
import org.ethereumphone.walletmanager.core.ui.WmTextFieldDefaults

@Composable
fun TokenTextField(
    text: String,
    trailingIcon: @Composable (() -> Unit)?,
    onTextChange: (String) -> Unit,
    fiatValue: Float,
    balance: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier= modifier.background(WmTextFieldDefaults.WmDarkTextField().containerColor)
    ) {
        WmTextField(
            text = text,
            trailingIcon = trailingIcon,
            onTextChanged = onTextChange
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = WmTextFieldDefaults.horizontalPaddingValues)
                .fillMaxWidth()
        ) {
            Text(
                text = fiatValue.toString(),
                color = Color.White
            )
            Text(
                text = balance.toString(),
                color = Color.White
            )
        }
    }
}

@Composable
@Preview
fun PreviewTokenField() {
    var text by remember { mutableStateOf("o") }
    TokenTextField(
        text = text,
        trailingIcon = { /*TODO*/ },
        onTextChange = {text = it},
        fiatValue = 54.2f,
        balance = 233.2f
    )
}