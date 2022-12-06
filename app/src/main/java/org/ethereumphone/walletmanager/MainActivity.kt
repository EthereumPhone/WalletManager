package org.ethereumphone.walletmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.ethereumphone.walletmanager.theme.WalletManagerTheme


class MainActivity: ComponentActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletManagerTheme {

            }

        }
    }
}