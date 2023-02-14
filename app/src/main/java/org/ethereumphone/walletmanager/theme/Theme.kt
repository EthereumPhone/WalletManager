package org.ethereumphone.walletmanager.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import org.ethereumphone.walletmanager.ui.theme.ethOSTypography

@SuppressLint("ConflictingOnColor")
private val darkColors = darkColors(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = black,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)


@Composable
fun WalletManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = darkColors

    MaterialTheme(
        colors = colors,
        content = content,
        typography = ethOSTypography
    )

}