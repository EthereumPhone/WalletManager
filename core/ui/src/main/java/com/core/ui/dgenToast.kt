package com.core.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.view.Gravity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.remember
import androidx.core.app.NotificationCompat
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.TextView
import android.widget.LinearLayout
import android.graphics.drawable.GradientDrawable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.res.ResourcesCompat
import com.example.dgenlibrary.ui.theme.PitagonsSans
import com.example.dgenlibrary.ui.theme.SpaceMono
import android.graphics.Color as AndroidColor


object FontResourceMap {
    private val map = mutableMapOf<Pair<FontFamily, FontWeight>, Int>()

    // Register a font resource
    fun register(fontFamily: FontFamily, weight: FontWeight, resourceId: Int) {
        map[Pair(fontFamily, weight)] = resourceId
    }

    // Get a font resource
    fun getResourceId(fontFamily: FontFamily, weight: FontWeight): Int? {
        return map[Pair(fontFamily, weight)]
    }
}

// Initialize the map with your fonts (call this once at app startup)
fun initializeFontMap(spaceMono: FontFamily, pitagonsSans: FontFamily) {
    // Register SpaceMono fonts
    FontResourceMap.register(spaceMono, FontWeight.Bold, R.font.spacemono_bold)

    // Register PitagonsSans fonts
    FontResourceMap.register(pitagonsSans, FontWeight.Bold, R.font.pitagonsanstext_bold)
    FontResourceMap.register(pitagonsSans, FontWeight.Medium, R.font.pitagonsanstext_medium)
    FontResourceMap.register(pitagonsSans, FontWeight.Normal, R.font.pitagonsanstext_regular)
    FontResourceMap.register(pitagonsSans, FontWeight.SemiBold, R.font.pitagonsanstext_semibold)
    FontResourceMap.register(pitagonsSans, FontWeight.Light, R.font.pitagonsanstext_light)
}

// Custom Toast implementation that works with Compose FontFamily
fun Context.showCustomToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
    backgroundColor: Color = Color(0xFF333333),
    textColor: Color = Color.White,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: Float = 16f,
    lineSpacingMultiplier: Float = 1.0f,
    lineSpacingExtra: Float = 0f,
    paddingHorizontal: Int = 24,
    paddingVertical: Int = 16,
    cornerRadius: Float = 16f,
    maxWidth: Int? = null,
    toastGravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
    xOffset: Int = 0,
    yOffset: Int = 150
) {
    val toast = Toast(this)

    // Create a layout programmatically
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        if (maxWidth != null) {
            this.minimumWidth = maxWidth
        }
    }

    // Get typeface from FontFamily if provided
    var typeface: Typeface? = null
    if (fontFamily != null) {
        val fontResourceId = FontResourceMap.getResourceId(fontFamily, fontWeight)
        if (fontResourceId != null) {
            typeface = ResourcesCompat.getFont(this, fontResourceId)
        }
    }

    // Create a text view for the message
    val textView = TextView(this).apply {
        text = message
        setTextColor(textColor.toAndroidColor())
        textSize = fontSize
        gravity = Gravity.CENTER

        // Set font family if provided
        if (typeface != null) {
            this.typeface = typeface
        } else {
            // Apply standard Android font weight if no custom typeface
            val androidWeight = when (fontWeight) {
                FontWeight.Bold -> Typeface.BOLD
                FontWeight.Normal -> Typeface.NORMAL
                else -> Typeface.NORMAL
            }
            this.typeface = Typeface.create(Typeface.DEFAULT, androidWeight)
        }

        // Set line spacing
        setLineSpacing(lineSpacingExtra, lineSpacingMultiplier)
    }

    // Add text view to layout
    layout.addView(textView)

    // Create a background drawable for the toast
    val shape = GradientDrawable().apply {
        this.cornerRadius = cornerRadius
        setColor(backgroundColor.toAndroidColor())
    }

    // Set the background
    layout.background = shape

    // Set up and show the toast
    toast.apply {
        setGravity(gravity, xOffset, yOffset)
        this.duration = duration
        view = layout
        show()
    }
}

// Extension function to convert Compose Color to Android Color
fun Color.toAndroidColor(): Int {
    return android.graphics.Color.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

// For system notifications (appears outside the app)
/*class NotificationHelper(private val context: Context) {
    private val channelId = "custom_toast_channel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Custom Toast Channel"
            val descriptionText = "Channel for custom toast notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, notificationId: Int = 1) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}*/

// Compose UI example
@Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")
@Composable
fun ToastDemoScreen() {
    val context = LocalContext.current
    //val notificationHelper = remember { NotificationHelper(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    initializeFontMap(SpaceMono, PitagonsSans)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    // Use custom toast
                    context.showCustomToast(
                        message = "This uses Pitagons Sans Medium!",
                        fontFamily = PitagonsSans,
                        fontWeight = FontWeight.Medium,
                        backgroundColor = Color(0xFF2C3E50),
                        textColor = Color.White
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Show Custom Toast")
            }

            Button(
                onClick = {
                    // Use Snackbar (Compose-friendly alternative)
                    context.showCustomToast(
                        message = "This uses Space Mono Bold!",
                        fontFamily = SpaceMono,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Show Snackbar")
            }

        }
    }
}


// @Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")