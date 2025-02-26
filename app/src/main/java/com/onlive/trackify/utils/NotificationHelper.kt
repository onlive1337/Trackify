package com.onlive.trackify.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onlive.trackify.MainActivity
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "trackify_reminders_channel"
        const val UPCOMING_NOTIFICATION_ID = 1001
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUpcomingPaymentNotification(subscription: Subscription, daysLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationText = when {
            daysLeft == 0 -> context.getString(R.string.notification_payment_today, subscription.name)
            daysLeft == 1 -> context.getString(R.string.notification_payment_tomorrow, subscription.name)
            else -> context.getString(R.string.notification_payment_upcoming, subscription.name, daysLeft)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subscriptions)
            .setContentTitle(context.getString(R.string.notification_upcoming_payment_title))
            .setContentText(notificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(subscription.subscriptionId.toInt(), builder.build())
            }
        } catch (e: SecurityException) {
        }
    }
}