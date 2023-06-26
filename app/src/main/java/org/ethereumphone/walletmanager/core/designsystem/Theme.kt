package org.ethereumphone.walletmanager.core.designsystem


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController


val DarkDefaultColorScheme = darkColorScheme(

)


@Composable
fun WmTheme(
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = background
    )

    MaterialTheme(
        content = content,
        typography = WmTypography
    )

}