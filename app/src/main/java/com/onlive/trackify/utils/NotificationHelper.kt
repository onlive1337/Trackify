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
import com.onlive.trackify.utils.CurrencyFormatter

/**
 * Класс для работы с уведомлениями в приложении
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "trackify_reminders_channel"
        const val UPCOMING_NOTIFICATION_ID = 1001
        const val PAYMENT_DUE_CHANNEL_ID = "trackify_payment_due_channel"
        const val EXPIRATION_CHANNEL_ID = "trackify_expiration_channel"

        private const val TAG = "NotificationHelper"
    }

    /**
     * Создает каналы уведомлений для Android 8.0+
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Канал для напоминаний о платежах
            val reminderChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }

            // Канал для уведомлений о просроченных платежах
            val paymentDueChannel = NotificationChannel(
                PAYMENT_DUE_CHANNEL_ID,
                "Просроченные платежи",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о просроченных платежах по подпискам"
            }

            // Канал для уведомлений об истекающих подписках
            val expirationChannel = NotificationChannel(
                EXPIRATION_CHANNEL_ID,
                "Истекающие подписки",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления об истекающих подписках"
            }

            // Регистрируем все каналы
            notificationManager.createNotificationChannels(
                listOf(reminderChannel, paymentDueChannel, expirationChannel)
            )
        }
    }

    /**
     * Показывает уведомление о предстоящем платеже
     */
    fun showUpcomingPaymentNotification(subscription: Subscription, daysLeft: Int) {
        if (!checkNotificationPermission()) {
            Log.e(TAG, "Отсутствует разрешение на отправку уведомлений")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscriptionId", subscription.subscriptionId)
            putExtra("action", "openSubscription")
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

        // Форматируем сумму платежа с валютой
        val formattedAmount = CurrencyFormatter.formatAmount(context, subscription.price)
        val notificationTextWithAmount = "$notificationText ($formattedAmount)"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subscriptions)
            .setContentTitle(context.getString(R.string.notification_upcoming_payment_title))
            .setContentText(notificationTextWithAmount)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationTextWithAmount))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(subscription.subscriptionId.toInt(), builder.build())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка при отправке уведомления: ${e.message}")
        }
    }

    /**
     * Показывает уведомление об истекающей подписке
     */
    fun showExpirationNotification(subscription: Subscription, daysLeft: Int) {
        if (!checkNotificationPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("subscriptionId", subscription.subscriptionId)
            putExtra("action", "openSubscription")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subscription.subscriptionId.toInt() + 1000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationText = when {
            daysLeft == 0 -> "Подписка ${subscription.name} истекает сегодня"
            daysLeft == 1 -> "Подписка ${subscription.name} истекает завтра"
            else -> "Подписка ${subscription.name} истекает через $daysLeft дней"
        }

        val builder = NotificationCompat.Builder(context, EXPIRATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subscriptions)
            .setContentTitle("Истекающая подписка")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                // Используем другой ID, чтобы не конфликтовать с уведомлениями о платежах
                notify(subscription.subscriptionId.toInt() + 2000, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Ошибка при отправке уведомления об истечении: ${e.message}")
        }
    }

    /**
     * Проверяет наличие разрешения на отправку уведомлений
     */
    private fun checkNotificationPermission(): Boolean {
        // Для Android 13+ нужно явное разрешение
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    /**
     * Отменяет уведомление
     */
    fun cancelNotification(notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
}