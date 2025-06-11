package com.core.ui.util

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Verwaltet zwei dynamische Farben (Primary & Secondary),
 * die beim Start der App aus den System-Akzentfarben gelesen werden können.
 * Die Farben werden als `mutableStateOf` gehalten, so dass jede Änderung
 * automatisch Recomposition in @Composable-Aufrufern auslöst.
 */
object SystemColorManager {

    private val DEFAULT_PRIMARY = Color(0xFF050505)
    private val DEFAULT_SECONDARY = Color(0xFFB5B3B3)

    /** Aktuelle Primärfarbe */
    var primaryColor by mutableStateOf(DEFAULT_PRIMARY)
        private set

    /** Aktuelle Sekundärfarbe */
    var secondaryColor by mutableStateOf(DEFAULT_SECONDARY)
        private set

    /**
     * Liest die aktuelle System-Akzentfarbe aus und aktualisiert die Felder.
     * Sollte z. B. im `LaunchedEffect` eines Screens ausgeführt werden.
     */
    fun refresh(context: Context) {
        // Android speichert die Akzentfarbe ab Android 12 in den Secure Settings.
        // Fallback ist ein intensives Rot, falls kein Eintrag vorhanden ist.
        val accentInt = Settings.Secure.getInt(
            context.contentResolver,
            "systemui_accent_color",
            DEFAULT_PRIMARY.toArgb()
        )
        val accentColor = Color(accentInt)
        primaryColor = accentColor
        // Eine sehr einfache Ableitung der Sekundärfarbe (leicht gedimmt).
        secondaryColor = accentColor.copy(alpha = 0.8f)
    }

    /**
     * Ermöglicht es, die Farben manuell zu überschreiben (z. B. für Tests).
     */
    fun setColors(primary: Color, secondary: Color) {
        primaryColor = primary
        secondaryColor = secondary
    }
} 