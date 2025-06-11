package com.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.core.ui.util.*

@Immutable
data class DgenColors(
    val dgenBlack: Color,
    val dgenWhite: Color,
    val dgenGray: Color,
    val dgenGunMetal: Color,
    val dgenRed: Color,
    val dgenDarkBlack: Color,
    val dgenGreen: Color,
    val dgenAqua: Color,
    val dgenOrche: Color,
    val dgenOcean: Color,
    val dgenTurqoise: Color,
    val dgenBurgendy: Color,
)

@Immutable
data class DgenTypography(
    val header0: TextStyle,
    val header1: TextStyle,
    val header2: TextStyle,
    val header3: TextStyle,
    val body2: TextStyle,
    val body1: TextStyle,
    val button: TextStyle,
    val label: TextStyle
)

@Immutable
data class DgenElevation(
    val default: Dp,
    val pressed: Dp
)


@Immutable
data class DgenDimension(
    val IconSize: Dp,
    val IconButtonSize: Dp,
    val LabelFontSize: TextUnit,
    val ButtonFontSize: TextUnit,
    val Body1FontSize: TextUnit,
    val Body2FontSize: TextUnit,
    val Header3FontSize: TextUnit,
    val Header2FontSize: TextUnit,
    val Header1FontSize: TextUnit,
    val Header0FontSize: TextUnit,
)


val LocalCustomColors = staticCompositionLocalOf {
    DgenColors(
        dgenBlack = Color.Unspecified,
        dgenWhite = Color.Unspecified,
        dgenGray = Color.Unspecified,
        dgenGunMetal = Color.Unspecified,
        dgenRed = Color.Unspecified,
        dgenDarkBlack = Color.Unspecified,
        dgenGreen = Color.Unspecified,
        dgenAqua = Color.Unspecified,
        dgenOrche = Color.Unspecified,
        dgenOcean = Color.Unspecified,
        dgenTurqoise = dgenTurqoise,
        dgenBurgendy = dgenBurgendy
    )
}
val LocalCustomTypography = staticCompositionLocalOf {
    DgenTypography(
        header0 = TextStyle.Default,
        header1 = TextStyle.Default,
        header2 = TextStyle.Default,
        header3 = TextStyle.Default,
        body2 = TextStyle.Default,
        body1 = TextStyle.Default,
        button = TextStyle.Default,
        label = TextStyle.Default,
    )
}
val LocalCustomElevation = staticCompositionLocalOf {
    DgenElevation(
        default = Dp.Unspecified,
        pressed = Dp.Unspecified
    )
}

val LocalCustomDimension = staticCompositionLocalOf {
    DgenDimension(
        IconSize = Dp.Unspecified,
        IconButtonSize = Dp.Unspecified,
        LabelFontSize = TextUnit.Unspecified,
        ButtonFontSize = TextUnit.Unspecified,
        Body1FontSize = TextUnit.Unspecified,
        Body2FontSize = TextUnit.Unspecified,
        Header3FontSize = TextUnit.Unspecified,
        Header2FontSize = TextUnit.Unspecified,
        Header1FontSize = TextUnit.Unspecified,
        Header0FontSize = TextUnit.Unspecified,
    )
}

@Composable
fun DgenTheme(
    /* ... */
    content: @Composable () -> Unit
) {

    val customColors = DgenColors(
        dgenBlack = dgenBlack,
        dgenWhite = dgenWhite,
        dgenGray = dgenGray,
        dgenGunMetal = dgenGunMetal,
        dgenRed = dgenRed,
        dgenDarkBlack = dgenDarkBlack,
        dgenGreen = dgenGreen,
        dgenAqua = dgenAqua,
        dgenOrche = dgenOrche,
        dgenOcean = dgenOcean,
        dgenTurqoise = dgenTurqoise,
        dgenBurgendy = dgenBurgendy
    )
    val customTypography = DgenTypography(
        header0 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = header0_fontSize,
            lineHeight = header0_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header1 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = header1_fontSize,
            lineHeight = header1_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header2 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = header2_fontSize,
            lineHeight = header2_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        header3 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = header3_fontSize,
            lineHeight = header3_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        body2 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = body2_fontSize,
            lineHeight = body2_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        body1 = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenWhite,
            fontWeight = FontWeight.SemiBold,
            fontSize = body1_fontSize,
            lineHeight = body1_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        button = TextStyle(
            fontFamily = PitagonsSans,
            color = dgenBlack,
            fontWeight = FontWeight.SemiBold,
            fontSize = button_fontSize,
            lineHeight = button_fontSize,
            letterSpacing = 0.sp,
            textDecoration = TextDecoration.None
        ),
        label = TextStyle(
            fontFamily = SpaceMono,
            color = dgenTurqoise,
            fontWeight = FontWeight.SemiBold,
            fontSize = label_fontSize,
            lineHeight = label_fontSize,
            letterSpacing = 1.sp,
            textDecoration = TextDecoration.None
        ),
    )
    val customElevation = DgenElevation(
        default = 4.dp,
        pressed = 8.dp
    )

    val customDimension = DgenDimension(
        IconSize = IconSize,
        IconButtonSize = IconButtonSize,
        LabelFontSize = label_fontSize,
        ButtonFontSize = button_fontSize,
        Body1FontSize = body1_fontSize,
        Body2FontSize = body2_fontSize,
        Header3FontSize = header3_fontSize,
        Header2FontSize = header2_fontSize,
        Header1FontSize = header1_fontSize,
        Header0FontSize = header0_fontSize,
    )

    CompositionLocalProvider(
        LocalCustomColors provides customColors,
        LocalCustomTypography provides customTypography,
        LocalCustomElevation provides customElevation,
        LocalCustomDimension provides customDimension,
        content = content
    )
}

// Use with eg. CustomTheme.elevation.small
object DgenTheme {
    val colors: DgenColors
        @Composable
        get() = LocalCustomColors.current
    val typography: DgenTypography
        @Composable
        get() = LocalCustomTypography.current
    val elevation: DgenElevation
        @Composable
        get() = LocalCustomElevation.current
    val dimensions: DgenDimension
        @Composable
        get() = LocalCustomDimension.current
}