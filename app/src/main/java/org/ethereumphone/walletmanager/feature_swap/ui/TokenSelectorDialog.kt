package org.ethereumphone.walletmanager.feature_swap.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import org.ethereumphone.walletmanager.R

@Composable
fun TokenSelectorDialog(


) {
    Dialog(
        onDismissRequest = {}
    ) {
        Surface() {

            Column() {
                Text(text = stringResource(id = R.string.select_swap_token))
            }   
        }

    }
}