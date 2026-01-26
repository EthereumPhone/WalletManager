package com.core.data.broadcast

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver that handles low paymaster balance notifications from the system.
 *
 * The PaymasterService sends this broadcast when the paymaster balance falls below 0.2 (20 cents).
 *
 * Expected intent:
 * - Action: org.ethereumphone.walletmanager.ACTION_SHOW_LOW_BALANCE
 * - Extra "balance": String - Current balance as a plain decimal string (e.g., "0.15")
 *
 * The system service already throttles notifications to once per 24 hours.
 */
class LowBalanceReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SHOW_LOW_BALANCE = "org.ethereumphone.walletmanager.ACTION_SHOW_LOW_BALANCE"
        const val EXTRA_BALANCE = "balance"

        private const val TAG = "LowBalanceReceiver"
        private const val CHANNEL_ID = "paymaster_low_balance"
        private const val NOTIFICATION_ID = 2001

        private const val ACTION_OPEN_GAS = "org.ethereumphone.walletmanager.ACTION_OPEN_GAS"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive called with action: ${intent?.action}")

        if (context == null || intent == null) {
            Log.w(TAG, "Context or intent is null")
            return
        }

        if (intent.action != ACTION_SHOW_LOW_BALANCE) {
            Log.w(TAG, "Received unexpected action: ${intent.action}")
            return
        }

        val balance = intent.getStringExtra(EXTRA_BALANCE) ?: "0"
        Log.d(TAG, "Received low balance notification: balance=$balance")

        showLowBalanceNotification(context, balance)
    }

    private fun showLowBalanceNotification(context: Context, balance: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Low Balance Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when Paymaster balance is low"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open the gas/top-up screen
        val openIntent = Intent().apply {
            action = ACTION_OPEN_GAS
            setPackage(context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Paymaster Balance Low")
            .setContentText("Your balance is \$$balance. Tap to top up.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.i(TAG, "Low balance notification displayed for balance: $balance")
    }
}
