package org.ethereumphone.walletmanager.core.designsystem


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable


val DarkDefaultColorScheme = darkColorScheme(

)


@Composable
fun WmTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content,
        typography = WmTypography
    )

}