package com.onlive.trackify.ui.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentUpcomingPaymentsBinding
import com.onlive.trackify.viewmodel.OverviewViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.Calendar
import java.util.Date

class UpcomingPaymentsFragment : Fragment() {

    private var _binding: FragmentUpcomingPaymentsBinding? = null
    private val binding get() = _binding!!

    private val overviewViewModel: OverviewViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var upcomingPaymentsAdapter: UpcomingPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpcomingPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBottomPadding()
        observeData()

        binding.chipToday.setOnClickListener { filterByTimeRange(0) }
        binding.chipWeek.setOnClickListener { filterByTimeRange(7) }
        binding.chipMonth.setOnClickListener { filterByTimeRange(30) }
        binding.chipAll.setOnClickListener { loadAllUpcomingPayments() }

        binding.chipWeek.isChecked = true
        filterByTimeRange(7)
    }

    private fun setupBottomPadding() {
        binding.recyclerViewUpcomingPayments.clipToPadding = false

        val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val extraPadding = resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
        val totalPadding = bottomNavHeight + extraPadding

        binding.recyclerViewUpcomingPayments.setPadding(
            binding.recyclerViewUpcomingPayments.paddingLeft,
            binding.recyclerViewUpcomingPayments.paddingTop,
            binding.recyclerViewUpcomingPayments.paddingRight,
            totalPadding
        )
    }

    private fun setupRecyclerView() {
        upcomingPaymentsAdapter = UpcomingPaymentAdapter()

        binding.recyclerViewUpcomingPayments.apply {
            adapter = upcomingPaymentsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            val upcomingPayments = calculateUpcomingPayments(subscriptions)
            updateUpcomingPaymentsList(upcomingPayments)
        }
    }

    private fun filterByTimeRange(days: Int) {
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return

        val endDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, days)
        }.time

        val upcomingPayments = calculateUpcomingPayments(subscriptions, endDate)
        updateUpcomingPaymentsList(upcomingPayments)
    }

    private fun loadAllUpcomingPayments() {
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return
        val upcomingPayments = calculateUpcomingPayments(subscriptions)
        updateUpcomingPaymentsList(upcomingPayments)
    }

    private fun calculateUpcomingPayments(
        subscriptions: List<Subscription>,
        endDate: Date? = null
    ): List<UpcomingPayment> {
        val today = Calendar.getInstance().time
        val upcomingPayments = mutableListOf<UpcomingPayment>()

        for (subscription in subscriptions) {
            if (!subscription.active) continue

            val nextPaymentDate = calculateNextPaymentDate(subscription)

            if (endDate != null && nextPaymentDate.after(endDate)) {
                continue
            }

            val daysUntilPayment = getDaysDifference(today, nextPaymentDate)

            upcomingPayments.add(
                UpcomingPayment(
                    subscription = subscription,
                    paymentDate = nextPaymentDate,
                    daysUntil = daysUntilPayment
                )
            )
        }

        return upcomingPayments.sortedBy { it.paymentDate }
    }

    private fun calculateNextPaymentDate(subscription: Subscription): Date {
        val calendar = Calendar.getInstance()
        calendar.time = subscription.startDate

        val today = Calendar.getInstance()

        while (calendar.before(today)) {
            when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
        }

        return calendar.time
    }

    private fun getDaysDifference(date1: Date, date2: Date): Int {
        val diff = date2.time - date1.time
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun updateUpcomingPaymentsList(upcomingPayments: List<UpcomingPayment>) {
        if (upcomingPayments.isEmpty()) {
            binding.textViewEmpty.visibility = View.VISIBLE
            binding.recyclerViewUpcomingPayments.visibility = View.GONE
        } else {
            binding.textViewEmpty.visibility = View.GONE
            binding.recyclerViewUpcomingPayments.visibility = View.VISIBLE
            upcomingPaymentsAdapter.submitList(upcomingPayments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class UpcomingPayment(
        val subscription: Subscription,
        val paymentDate: Date,
        val daysUntil: Int
    )
}