package com.core.ui.util

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.core.ui.R

/**
 * Manages two dynamic colors (Primary & Secondary),
 * which can be read from the system accent colors when the app starts.
 * The colors are held as `mutableStateOf`, so that any change
 * automatically triggers recomposition in @Composable callers.
 */
object SystemColorManager {

    private val DEFAULT_PRIMARY = Color(0xFF050505)
    private val DEFAULT_SECONDARY = Color(0xFFB5B3B3)

    /** Current primary color */
    var primaryColor by mutableStateOf(DEFAULT_PRIMARY)
        private set

    /** Current secondary color */
    var secondaryColor by mutableStateOf(DEFAULT_SECONDARY)
        private set

    /**
     * Reads the current system accent color and updates the fields.
     * Should be executed, for example, in the `LaunchedEffect` of a screen.
     */
    fun refresh(context: Context) {
        // Android stores the accent color in Secure Settings from Android 12 onwards.
        // The fallback is an intense red if no entry is present.
        val accentInt = Settings.Secure.getInt(
            context.contentResolver,
            "systemui_accent_color",
            Color.Red.toArgb()
        )

        val accentColor = if(accentInt == -65536) {
            Color.Red
        } else {
            Color(accentInt)
        }

        Log.d("SystemColorManager","accentInt: $accentInt, accentColor: $accentColor")

        //Decide the colorway
        when(accentInt){
            // If the accent color is not set, we use the default colors.
            -65536 -> {
                primaryColor = lazerCore
                secondaryColor = lazerBurn
            }
            //TERMINAL
            -13510400 -> {
                primaryColor = terminalCore
                secondaryColor = terminalHack
            }
            //LAZER
            -131072 -> {
                primaryColor = lazerCore
                secondaryColor = lazerBurn
            }
            //OCEAN
            -16718593 -> {
                primaryColor = oceanCore
                secondaryColor = oceanAbyss
            }
            //ORCHE
            -1012183 -> {
                primaryColor = orcheCore
                secondaryColor = orcheAsh
            }

            //GUNMETAL
            -3618616 -> {
                primaryColor = gunMetalCore
                secondaryColor = gunMetalForge
            }
        }

        //-13510400 - Green
        // -131072 - Red
        // -16718593 - blue
        // -1012183 - orange
        // -3618616 - Gray
    }

    /**
     * Allows to manually override the colors (e.g. for testing).
     */
    fun setColors(primary: Color, secondary: Color) {
        primaryColor = primary
        secondaryColor = secondary
    }

    /**
     * Returns the appropriate placeholder token drawable resource based on the current primary color.
     * Each theme has its own styled placeholder icon.
     */
    fun getPlaceholderTokenDrawable(): Int {
        return getPlaceholderTokenDrawableForColor(primaryColor)
    }

    /**
     * Returns the appropriate placeholder token drawable resource based on the given color.
     * - Lazer (red): placeholder_token_lazer
     * - Ocean (cyan): placeholder_token_ocean
     * - Orche (orange): placeholder_token_orche
     * - Gunmetal (gray): placeholder_token_gunmetal
     * - Terminal (green): placeholder_token_terminal
     */
    fun getPlaceholderTokenDrawableForColor(color: Color): Int {
        return when (color) {
            lazerCore -> R.drawable.placeholder_token_lazer
            oceanCore -> R.drawable.placeholder_token_ocean
            orcheCore -> R.drawable.placeholder_token_orche
            gunMetalCore -> R.drawable.placeholder_token_gunmetal
            terminalCore -> R.drawable.placeholder_token_terminal
            else -> R.drawable.placeholder_token_lazer // Default to lazer if unknown
        }
    }
} 