package com.onlive.trackify.utils

import android.content.Context
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.data.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.random.Random

class PerformanceTester(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val errorHandler = ErrorHandler.getInstance(context)

    suspend fun generateTestData(
        subscriptionsCount: Int = 100,
        paymentsPerSubscription: Int = 20
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val categories = generateTestCategories()

            val subscriptions = generateTestSubscriptions(subscriptionsCount, categories)

            generateTestPayments(subscriptions, paymentsPerSubscription)

            Result.Success(Unit)
        } catch (e: Exception) {
            errorHandler.handleError("Ошибка при генерации тестовых данных: ${e.message}")
            Result.Error("Ошибка при генерации тестовых данных", e)
        }
    }

    private suspend fun generateTestCategories(): List<Category> {
        val existingCategories = database.categoryDao().getAllCategoriesSync()

        if (existingCategories.size >= 10) {
            return existingCategories
        }

        val testCategories = listOf(
            Category(name = "Развлечения", colorCode = "#FF5252"),
            Category(name = "Стриминг", colorCode = "#7C4DFF"),
            Category(name = "Музыка", colorCode = "#448AFF"),
            Category(name = "Программы", colorCode = "#4CAF50"),
            Category(name = "Игры", colorCode = "#FF9800"),
            Category(name = "Облачные сервисы", colorCode = "#00BCD4"),
            Category(name = "Спорт", colorCode = "#795548"),
            Category(name = "Здоровье", colorCode = "#009688"),
            Category(name = "Образование", colorCode = "#F44336"),
            Category(name = "Информация", colorCode = "#2196F3")
        )

        val categoryIds = database.categoryDao().insertAll(testCategories)

        return testCategories.mapIndexed { index, category ->
            category.copy(categoryId = categoryIds[index])
        }
    }

    private suspend fun generateTestSubscriptions(
        count: Int,
        categories: List<Category>
    ): List<Subscription> {

        val subscriptionNames = listOf(
            "Netflix", "Spotify", "YouTube Premium", "Apple Music", "Яндекс.Плюс",
            "Microsoft 365", "Dropbox", "Google One", "MEGOGO", "Amediateka",
            "Apple TV+", "Adobe Creative Cloud", "PlayStation Plus", "Xbox Game Pass",
            "EA Play", "Storytel", "Bookmate", "Литрес", "Headspace", "Duolingo Plus",
            "Avast Premium", "Norton 360", "Kaspersky Total Security", "Tinkoff Premium",
            "Сбер Прайм", "Альфа-Премиум", "Okko", "Кинопоиск HD", "Skillbox",
            "Нетология", "Skyeng", "GeekBrains", "FitnessBlender", "Strava Premium"
        )

        val calendar = Calendar.getInstance()
        val subscriptions = mutableListOf<Subscription>()

        for (i in 1..count) {
            val nameIndex = abs(Random.nextInt()) % subscriptionNames.size
            val name = if (i <= subscriptionNames.size) {
                subscriptionNames[i - 1]
            } else {
                "${subscriptionNames[nameIndex]} ${i / subscriptionNames.size}"
            }

            val description = "Тестовая подписка на $name"

            val price = when {
                Random.nextDouble() < 0.7 -> Random.nextDouble(100.0, 1000.0)
                else -> Random.nextDouble(1000.0, 5000.0)
            }

            val billingFrequency = if (Random.nextDouble() < 0.7) {
                BillingFrequency.MONTHLY
            } else {
                BillingFrequency.YEARLY
            }

            calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR) - 2)
            calendar.add(Calendar.DAY_OF_YEAR, Random.nextInt(365 * 2))
            val startDate = calendar.time

            val endDate = if (Random.nextDouble() < 0.3) {
                calendar.time = startDate
                calendar.add(Calendar.YEAR, Random.nextInt(1, 3))
                calendar.time
            } else {
                null
            }

            val categoryId = if (Random.nextDouble() < 0.8) {
                categories[abs(Random.nextInt()) % categories.size].categoryId
            } else {
                null
            }

            val active = Random.nextDouble() < 0.9

            val subscription = Subscription(
                name = name,
                description = description,
                price = price.round(2),
                billingFrequency = billingFrequency,
                startDate = startDate,
                endDate = endDate,
                categoryId = categoryId,
                active = active
            )

            val id = database.subscriptionDao().insert(subscription)
            subscriptions.add(subscription.copy(subscriptionId = id))
        }

        return subscriptions
    }

    private suspend fun generateTestPayments(
        subscriptions: List<Subscription>,
        paymentsPerSubscription: Int
    ) {
        for (subscription in subscriptions) {
            val calendar = Calendar.getInstance()
            calendar.time = subscription.startDate

            for (i in 1..paymentsPerSubscription) {
                val paymentDate = calendar.time

                val status = when {
                    calendar.time.after(Date()) -> PaymentStatus.PENDING
                    Random.nextDouble() < 0.9 -> PaymentStatus.CONFIRMED
                    else -> PaymentStatus.MANUAL
                }

                val payment = Payment(
                    subscriptionId = subscription.subscriptionId,
                    amount = subscription.price,
                    date = paymentDate,
                    notes = if (Random.nextDouble() < 0.3) "Примечание к платежу ${UUID.randomUUID().toString().substring(0, 8)}" else null,
                    status = status,
                    autoGenerated = Random.nextDouble() < 0.7
                )

                database.paymentDao().insert(payment)

                when (subscription.billingFrequency) {
                    BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                    BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
                }
            }
        }
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    private fun abs(value: Int): Int {
        return value.absoluteValue
    }

    suspend fun clearTestData(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            database.paymentDao().deleteAllSync()
            database.subscriptionDao().deleteAllSync()
            Result.Success(Unit)
        } catch (e: Exception) {
            errorHandler.handleError("Ошибка при очистке тестовых данных: ${e.message}")
            Result.Error("Ошибка при очистке тестовых данных", e)
        }
    }

    suspend fun measureDatabasePerformance(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val results = mutableListOf<String>()

            val startTime1 = System.currentTimeMillis()
            val subscriptions = database.subscriptionDao().getAllSubscriptionsSync()
            val endTime1 = System.currentTimeMillis()
            results.add("Загрузка ${subscriptions.size} подписок: ${endTime1 - startTime1} мс")

            val startTime2 = System.currentTimeMillis()
            val payments = database.paymentDao().getAllPaymentsSync()
            val endTime2 = System.currentTimeMillis()
            results.add("Загрузка ${payments.size} платежей: ${endTime2 - startTime2} мс")

            val startTime3 = System.currentTimeMillis()
            val categories = database.categoryDao().getAllCategoriesSync()
            val endTime3 = System.currentTimeMillis()
            results.add("Загрузка ${categories.size} категорий: ${endTime3 - startTime3} мс")

            Result.Success(results.joinToString("\n"))
        } catch (e: Exception) {
            errorHandler.handleError("Ошибка при измерении производительности: ${e.message}")
            Result.Error("Ошибка при измерении производительности", e)
        }
    }
}