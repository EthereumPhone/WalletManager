package org.ethereumphone.walletmanager.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import org.ethereumphone.walletmanager.theme.black
import org.ethereumphone.walletmanager.theme.dark_primary
import org.ethereumphone.walletmanager.theme.dark_secondary
import org.ethereumphone.walletmanager.theme.dark_tretiary

/*import com.example.collectiongallery.ui.Elevation
import com.example.collectiongallery.ui.LocalElevation
import com.example.collectiongallery.ui.LocalSpacing
import com.example.collectiongallery.ui.Spacing*/

/*private val DarkColorPalette = darkColors(
    primary = Purple200,
    primaryVariant = Purple700,
    secondary = Teal200
)

/private val LightColorPalette = lightColors(
    primary = Purple500,
    primaryVariant = Purple700,
    secondary = Teal200

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

private val ethOSColorPaletteLight = lightColors(
    primary = PrimaryLight,
    primaryVariant = SecondaryLight,
    secondary = AccentLight,
    background = white,
    surface = white,
    onPrimary = black,
    onSecondary = black,
    onBackground = black,
    onSurface = black,

    )
*/
private val ethOSColorPaletteDark = darkColors(
    primary = dark_primary,
    primaryVariant = dark_secondary,
    secondary = dark_tretiary,
    background = black,
    /*surface = darkgray2,
    onPrimary = white,
    onSecondary = white,
    onBackground = white,
    onSurface = white,
    error = derror,
    onError = white*/

)



//ethOS Pallette
@Composable
fun ethOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    /*val colors = if (darkTheme) {
        ethOSColorPaletteDark
    } else {
        ethOSColorPaletteDark
    }*/

    val colors = ethOSColorPaletteDark
    MaterialTheme(
        colors = colors,
        typography = ethOSTypography,
        //shapes = Shapes,
        content = content
    )
    //val colors = ethOSColorPaletteDark
    /*CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalElevation provides Elevation()
    ) {

    }*/



}