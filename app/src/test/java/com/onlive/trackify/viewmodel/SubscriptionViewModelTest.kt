package com.onlive.trackify.viewmodel

import com.onlive.trackify.R
import com.onlive.trackify.data.database.SubscriptionDao
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.data.repository.SubscriptionRepository
import com.onlive.trackify.utils.ErrorReporter
import com.onlive.trackify.utils.Result
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insert succeeds, updates allSubscriptions, triggers mutation hook and onSuccess`() = runTest(testDispatcher) {
        val fakeDao = FakeSubscriptionDao()
        val repository = SubscriptionRepository(fakeDao, testDispatcher)
        val errorReporter = FakeErrorReporter()
        var mutationCount = 0
        var onSuccessCalled = false

        val viewModel = SubscriptionViewModel(
            repository = repository,
            errorReporter = errorReporter,
            onSubscriptionMutated = { mutationCount++ }
        )

        val collectorJob = backgroundScope.launch(testDispatcher) {
            viewModel.allSubscriptions.collect {}
        }

        val sample = Subscription(
            name = "YouTube Premium",
            description = "Family plan",
            price = 13.99,
            billingFrequency = BillingFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 11, 1),
            endDate = null,
            categoryId = null
        )

        viewModel.insert(
            subscription = sample,
            onSuccess = { onSuccessCalled = true }
        )

        assertTrue(onSuccessCalled)
        assertEquals(1, mutationCount)
        assertTrue(errorReporter.reportedErrors.isEmpty())
        assertEquals(1, viewModel.allSubscriptions.value?.size)
        assertEquals("YouTube Premium", viewModel.allSubscriptions.value?.single()?.subscription?.name)

        collectorJob.cancel()
    }

    @Test
    fun `insert failure reports localized error resource and invokes onError without onSuccess`() = runTest(testDispatcher) {
        val fakeDao = FakeSubscriptionDao(shouldThrowOnWrite = true)
        val repository = SubscriptionRepository(fakeDao, testDispatcher)
        val errorReporter = FakeErrorReporter()
        var mutationCount = 0
        var onSuccessCalled = false
        var onErrorCalled = false

        val viewModel = SubscriptionViewModel(
            repository = repository,
            errorReporter = errorReporter,
            onSubscriptionMutated = { mutationCount++ }
        )

        val sample = Subscription(
            name = "Spotify",
            description = null,
            price = 9.99,
            billingFrequency = BillingFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 11, 1),
            endDate = null,
            categoryId = null
        )

        viewModel.insert(
            subscription = sample,
            onSuccess = { onSuccessCalled = true },
            onError = { onErrorCalled = true }
        )

        assertFalse(onSuccessCalled)
        assertTrue(onErrorCalled)
        assertEquals(0, mutationCount)
        assertEquals(1, errorReporter.reportedErrors.size)
        assertEquals(
            R.string.error_creating_subscription,
            errorReporter.reportedErrors.single().messageRes
        )
    }

    @Test
    fun `update on deleted subscription reports error and invokes onError`() = runTest(testDispatcher) {
        val fakeDao = FakeSubscriptionDao()
        val repository = SubscriptionRepository(fakeDao, testDispatcher)
        val errorReporter = FakeErrorReporter()
        var onSuccessCalled = false
        var onErrorCalled = false

        val viewModel = SubscriptionViewModel(
            repository = repository,
            errorReporter = errorReporter
        )

        val ghostSub = Subscription(
            subscriptionId = 999L,
            name = "Deleted Plan",
            description = null,
            price = 9.99,
            billingFrequency = BillingFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 11, 1),
            endDate = null,
            categoryId = null
        )

        viewModel.update(
            subscription = ghostSub,
            onSuccess = { onSuccessCalled = true },
            onError = { onErrorCalled = true }
        )

        assertFalse(onSuccessCalled)
        assertTrue(onErrorCalled)
        assertEquals(R.string.error_updating_subscription, errorReporter.reportedErrors.single().messageRes)
    }

    @Test
    fun `delete invokes onSubscriptionDeleted with deleted id`() = runTest(testDispatcher) {
        val fakeDao = FakeSubscriptionDao()
        val repository = SubscriptionRepository(fakeDao, testDispatcher)
        val errorReporter = FakeErrorReporter()
        val deletedIds = mutableListOf<Long>()

        val viewModel = SubscriptionViewModel(
            repository = repository,
            errorReporter = errorReporter,
            onSubscriptionDeleted = { deletedIds += it }
        )

        val sub = Subscription(
            subscriptionId = 42L,
            name = "Cloud Storage",
            description = null,
            price = 2.99,
            billingFrequency = BillingFrequency.MONTHLY,
            startDate = LocalDate.of(2026, 11, 1),
            endDate = null,
            categoryId = null
        )
        fakeDao.insert(sub)

        var deleted = false
        viewModel.delete(subscription = sub, onSuccess = { deleted = true })

        assertTrue(deleted)
        assertEquals(listOf(42L), deletedIds)
    }

    private class FakeErrorReporter : ErrorReporter {
        val reportedErrors = mutableListOf<Result.Error>()
        val reportedExceptions = mutableListOf<Throwable>()

        override fun reportError(error: Result.Error, showMessage: Boolean) {
            reportedErrors += error
        }

        override fun reportException(throwable: Throwable, showMessage: Boolean) {
            reportedExceptions += throwable
        }
    }

    private class FakeSubscriptionDao(
        private val shouldThrowOnWrite: Boolean = false
    ) : SubscriptionDao {
        private val items = MutableStateFlow<List<Subscription>>(emptyList())
        private var nextId = 1L

        override suspend fun insert(subscription: Subscription): Long {
            if (shouldThrowOnWrite) throw IllegalStateException("Simulated SQLite error")
            val id = if (subscription.subscriptionId == 0L) nextId++ else subscription.subscriptionId
            items.value += subscription.copy(subscriptionId = id)
            return id
        }

        override suspend fun insertAll(subscriptions: List<Subscription>) {
            if (shouldThrowOnWrite) throw IllegalStateException("Simulated SQLite error")
            items.value += subscriptions
        }

        override suspend fun update(subscription: Subscription): Int {
            if (shouldThrowOnWrite) throw IllegalStateException("Simulated SQLite error")
            var updated = 0
            items.value = items.value.map {
                if (it.subscriptionId == subscription.subscriptionId) {
                    updated++
                    subscription
                } else {
                    it
                }
            }
            return updated
        }

        override suspend fun delete(subscription: Subscription) {
            if (shouldThrowOnWrite) throw IllegalStateException("Simulated SQLite error")
            items.value = items.value.filterNot { it.subscriptionId == subscription.subscriptionId }
        }

        override fun getAllSubscriptionsWithCategory(): Flow<List<SubscriptionWithCategory>> {
            return items.map { list ->
                list.map { SubscriptionWithCategory(subscription = it, category = null) }
            }
        }

        override fun getSubscriptionWithCategoryById(id: Long): Flow<SubscriptionWithCategory?> {
            return items.map { list ->
                list.find { it.subscriptionId == id }?.let {
                    SubscriptionWithCategory(subscription = it, category = null)
                }
            }
        }

        override suspend fun getAllSnapshot(): List<Subscription> = items.value

        override suspend fun getAllSubscriptionsForWorker(): List<Subscription> = items.value

        override suspend fun deleteAll() {
            items.value = emptyList()
        }
    }
}
