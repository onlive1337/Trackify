package com.onlive.trackify.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onlive.trackify.MainActivity
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription

class NotificationHelper(private val context: Context) {

    private val channelId = "trackify_notifications"
    private val tag = "NotificationHelper"

    fun createNotificationChannel() {
        val name = context.getString(R.string.notification_channel_name)
        val descriptionText = context.getString(R.string.notification_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
            enableVibration(true)
            enableLights(true)
        }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        Log.d(tag, "Notification channel created")
    }

    fun showPaymentReminderNotification(subscription: Subscription, daysUntil: Int) {
        if (!checkNotificationPermission()) {
            Log.w(tag, "No notification permission")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscription_id", subscription.subscriptionId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, subscription.subscriptionId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_payment_reminder_title)
        val text = when (daysUntil) {
            0 -> context.getString(R.string.notification_payment_today, subscription.name)
            1 -> context.getString(R.string.notification_payment_tomorrow, subscription.name)
            else -> context.getString(R.string.notification_payment_in_days, subscription.name, daysUntil)
        }

        val formattedAmount = CurrencyFormatter.formatAmount(context, subscription.price)
        val fullText = "$text ($formattedAmount)"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_payments)
            .setContentTitle(title)
            .setContentText(fullText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(subscription.subscriptionId.toInt(), builder.build())
            }
            Log.d(tag, "Payment reminder notification sent for ${subscription.name}")
        } catch (e: SecurityException) {
            Log.e(tag, "Failed to send notification: ${e.message}")
        }
    }

    fun showExpirationReminderNotification(subscription: Subscription, daysUntil: Int) {
        if (!checkNotificationPermission()) {
            Log.w(tag, "No notification permission")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscription_id", subscription.subscriptionId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, (subscription.subscriptionId + 10000).toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_expiration_reminder_title)
        val text = when (daysUntil) {
            0 -> context.getString(R.string.notification_expires_today, subscription.name)
            1 -> context.getString(R.string.notification_expires_tomorrow, subscription.name)
            else -> context.getString(R.string.notification_expires_in_days, subscription.name, daysUntil)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify((subscription.subscriptionId + 10000).toInt(), builder.build())
            }
            Log.d(tag, "Expiration reminder notification sent for ${subscription.name}")
        } catch (e: SecurityException) {
            Log.e(tag, "Failed to send notification: ${e.message}")
        }
    }

    private fun checkNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}