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

    companion object {
        const val CHANNEL_ID = "trackify_reminders_channel"
        const val PAYMENT_NOTIFICATION_ID_PREFIX = 1000
        const val EXPIRATION_CHANNEL_ID = "trackify_expiration_channel"
        const val PAYMENT_CHANNEL_ID = "trackify_payment_channel"

        private const val TAG = "NotificationHelper"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            val paymentChannel = NotificationChannel(
                PAYMENT_CHANNEL_ID,
                context.getString(R.string.notification_channel_payment_due),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_payment_due_description)
            }

            val expirationChannel = NotificationChannel(
                EXPIRATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_expiration),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_expiration_description)
            }

            notificationManager.createNotificationChannels(
                listOf(reminderChannel, paymentChannel, expirationChannel)
            )

            Log.d(TAG, "Каналы уведомлений созданы")
        }
    }

    fun showUpcomingSubscriptionPaymentNotification(subscription: Subscription, daysLeft: Int) {
        if (!checkNotificationPermission()) {
            Log.e(TAG, "Отсутствует разрешение на уведомления")
            return
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscriptionId", subscription.subscriptionId)
            putExtra("action", "openSubscription")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (PAYMENT_NOTIFICATION_ID_PREFIX + subscription.subscriptionId).toInt(),
            intent,
            pendingIntentFlags
        )

        val notificationText = when {
            daysLeft == 0 -> context.getString(R.string.notification_payment_today, subscription.name)
            daysLeft == 1 -> context.getString(R.string.notification_payment_tomorrow, subscription.name)
            else -> context.getString(R.string.notification_payment_upcoming, subscription.name, daysLeft)
        }

        val formattedAmount = CurrencyFormatter.formatAmount(context, subscription.price)
        val notificationTextWithAmount = "$notificationText ($formattedAmount)"

        val builder = NotificationCompat.Builder(context, PAYMENT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_upcoming_payment_title))
            .setContentText(notificationTextWithAmount)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationTextWithAmount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify((PAYMENT_NOTIFICATION_ID_PREFIX + subscription.subscriptionId).toInt(), builder.build())
            }
            Log.d(TAG, "Уведомление о платеже отправлено для ${subscription.name}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка отправки уведомления: ${e.message}")
        }
    }

    fun showSubscriptionExpirationNotification(subscription: Subscription, daysLeft: Int) {
        if (!checkNotificationPermission()) return

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscriptionId", subscription.subscriptionId)
            putExtra("action", "openSubscription")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (subscription.subscriptionId + 3000).toInt(),
            intent,
            pendingIntentFlags
        )

        val notificationText = when {
            daysLeft == 0 -> context.getString(R.string.subscription_expires_today, subscription.name)
            daysLeft == 1 -> context.getString(R.string.subscription_expires_tomorrow, subscription.name)
            else -> context.getString(R.string.subscription_expires_days, subscription.name, daysLeft)
        }

        val builder = NotificationCompat.Builder(context, EXPIRATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_channel_expiration))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify((subscription.subscriptionId + 3000).toInt(), builder.build())
            }
            Log.d(TAG, "Уведомление об окончании подписки отправлено для ${subscription.name}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка отправки уведомления об окончании подписки: ${e.message}")
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

    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}