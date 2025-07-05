package com.feature.send.ui

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.feature.send.R
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CustomCaptureActivity : CaptureActivity() {
    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullscreen()

        setContentView(R.layout.custom_qr_scanner)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)

        val systemColor = getColorForRender()


        val infoText = findViewById<TextView>(R.id.info_text)
        infoText.setTextColor(systemColor)
        infoText.alpha = 0.5f
        
        // Back button handling
        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setColorFilter(systemColor)
        backButton.setOnClickListener {
            finish()
            // Fade-Out-Animation beim Verlassen
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
        
        // Fade-In-Animation beim Öffnen
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    private fun getColorForRender(): Int {
        val accentColor = Settings.Secure.getInt(
            contentResolver,
            "systemui_accent_color",
            -0x20000 // Default red
        )

        when (accentColor) {
            -131072 -> return -0x10000
        }

        return accentColor
    }
    
    private fun setupFullscreen() {
        // Draw edge-to-edge while keeping system bars visible
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)
            ?.show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
        // Stelle sicher, dass Vollbild-Modus aktiv bleibt
        setupFullscreen()
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Reaktiviere Vollbild-Modus wenn das Fenster wieder Fokus erhält
            setupFullscreen()
        }
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
    
    override fun finish() {
        super.finish()
        // Fade-Animation beim Schließen
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
} 