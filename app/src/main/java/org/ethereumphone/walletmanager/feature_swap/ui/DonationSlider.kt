package org.ethereumphone.walletmanager.feature_swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.ethereumphone.walletmanager.R
import org.ethereumphone.walletmanager.core.ui.WmSlider


@Composable
fun DonationSlider(

) {
    Column() {
        Text(
            text = stringResource(id = R.string.donate_prompt),
            color = Color.LightGray
        )
        WmSlider()
    }
}

@Preview
@Composable
fun previewDonationSlider() {
    Column(
    ) {
        DonationSlider()
    }
}