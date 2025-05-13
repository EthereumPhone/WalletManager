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
import androidx.compose.ui.tooling.preview.Preview
import android.graphics.Color as AndroidColor


// Custom Toast implementation that doesn't use ComposeView directly
fun Color.toAndroidColor(): Int {
    return AndroidColor.argb(
        (alpha * 255).toInt(),
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )
}

// Custom Toast implementation without using ComposeView
fun Context.showCustomToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
    backgroundColor: Color = Color(0xFF333333),
    textColor: Color = Color.White
) {
    val toast = Toast(this)

    // Create a layout programmatically
    val layout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(24, 16, 24, 16)
    }

    // Create a text view for the message
    val textView = TextView(this).apply {
        text = message
        setTextColor(textColor.toAndroidColor())
        textSize = 16f
        gravity = Gravity.CENTER
        font
    }

    // Add text view to layout
    layout.addView(textView)

    // Create a background drawable for the toast
    val shape = GradientDrawable().apply {
        cornerRadius = 60f
        setColor(backgroundColor.toAndroidColor())
    }

    // Set the background
    layout.background = shape

    // Set up and show the toast
    toast.apply {
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
        this.duration = duration
        view = layout
        show()
    }
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
                    context.showCustomToast("This is a custom toast message!",duration = Toast.LENGTH_LONG)
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Show Custom Toast")
            }

            Button(
                onClick = {
                    // Use Snackbar (Compose-friendly alternative)
                    scope.launch {
                        snackbarHostState.showSnackbar("This is a Snackbar message!")
                    }
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text("Show Snackbar")
            }

        }
    }
}


// @Preview(device = "spec:width=720px,height=720px,dpi=240", name = "DDevice")