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

class NotificationHelper(rawContext: Context) {

    private val context: Context = LocaleHelper.getLocalizedContext(
        rawContext.applicationContext ?: rawContext,
        PreferenceManager(rawContext)
    )
    private val channelId = "trackify_notifications"
    private val tag = "NotificationHelper"

    companion object {
        private const val TAG_PAYMENT_REMINDER = "payment_reminder"
        private const val TAG_EXPIRATION_REMINDER = "expiration_reminder"
        const val EXTRA_SUBSCRIPTION_ID = "subscription_id"
    }

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

    fun showPaymentReminderNotification(subscription: Subscription, daysUntil: Int): Boolean {
        if (!checkNotificationPermission()) {
            Log.w(tag, "No notification permission")
            return false
        }

        val notificationId = subscription.subscriptionId.hashCode()
        val requestCode = notificationId * 31 + 1

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_SUBSCRIPTION_ID, subscription.subscriptionId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_payment_reminder_title)
        val text = when (daysUntil) {
            0 -> context.getString(R.string.notification_payment_today, subscription.name)
            1 -> context.getString(R.string.notification_payment_tomorrow, subscription.name)
            else -> context.resources.getQuantityString(R.plurals.notification_payment_in_days, daysUntil, subscription.name, daysUntil)
        }

        val formattedAmount = CurrencyFormatter.formatAmount(context, subscription.price)
        val fullText = "$text ($formattedAmount)"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_payments)
            .setContentTitle(title)
            .setContentText(fullText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        return try {
            with(NotificationManagerCompat.from(context)) {
                notify(TAG_PAYMENT_REMINDER, notificationId, builder.build())
            }
            Log.d(tag, "Payment reminder notification sent for ${subscription.name}")
            true
        } catch (e: SecurityException) {
            Log.e(tag, "Failed to send notification: ${e.message}")
            false
        }
    }

    fun showExpirationReminderNotification(subscription: Subscription, daysUntil: Int): Boolean {
        if (!checkNotificationPermission()) {
            Log.w(tag, "No notification permission")
            return false
        }

        val notificationId = subscription.subscriptionId.hashCode()
        val requestCode = notificationId * 31 + 2

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_SUBSCRIPTION_ID, subscription.subscriptionId)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notification_expiration_reminder_title)
        val text = when (daysUntil) {
            0 -> context.getString(R.string.notification_expires_today, subscription.name)
            1 -> context.getString(R.string.notification_expires_tomorrow, subscription.name)
            else -> context.resources.getQuantityString(R.plurals.notification_expires_in_days, daysUntil, subscription.name, daysUntil)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        return try {
            with(NotificationManagerCompat.from(context)) {
                notify(TAG_EXPIRATION_REMINDER, notificationId, builder.build())
            }
            Log.d(tag, "Expiration reminder notification sent for ${subscription.name}")
            true
        } catch (e: SecurityException) {
            Log.e(tag, "Failed to send notification: ${e.message}")
            false
        }
    }

    fun cancelNotificationsForSubscription(subscriptionId: Long) {
        runCatching {
            val manager = NotificationManagerCompat.from(context)
            val notificationId = subscriptionId.hashCode()
            manager.cancel(TAG_PAYMENT_REMINDER, notificationId)
            manager.cancel(TAG_EXPIRATION_REMINDER, notificationId)
            manager.cancel(subscriptionId.toInt())
            manager.cancel((subscriptionId + 10000).toInt())
        }
    }

    fun cancelAllNotifications() {
        runCatching {
            NotificationManagerCompat.from(context).cancelAll()
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
