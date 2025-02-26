package com.onlive.trackify.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentPaymentListBinding
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

class PaymentListFragment : Fragment() {

    private var _binding: FragmentPaymentListBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddPayment.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_payments_to_addPaymentFragment)
        }

        binding.cardPendingPayments.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_payments_to_pendingPaymentsFragment)
        }

        binding.buttonBulkActions.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_payments_to_bulkPaymentActionsFragment)
        }

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter()

        binding.recyclerViewPayments.apply {
            adapter = paymentAdapter
            layoutManager = LinearLayoutManager(requireContext())

            clipToPadding = false
            val bottomPadding = getNavigationBarHeight()
            setPadding(paddingLeft, paddingTop, paddingRight, bottomPadding)
        }
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun observeData() {
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subs ->
            updatePaymentsList()
        }

        paymentViewModel.allPayments.observe(viewLifecycleOwner) { payments ->
            updatePaymentsList()

            if (payments.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewPayments.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewPayments.visibility = View.VISIBLE
            }
        }

        paymentViewModel.pendingPaymentsCount.observe(viewLifecycleOwner) { count ->
            if (count > 0) {
                binding.cardPendingPayments.visibility = View.VISIBLE
                binding.textViewPendingCount.text = count.toString()
            } else {
                binding.cardPendingPayments.visibility = View.GONE
            }
        }
    }

    private fun updatePaymentsList() {
        val payments = paymentViewModel.allPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return
        if (subscriptions.isEmpty()) return

        val paymentsWithNames = payments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: "Неизвестно"
            )
        }

        paymentAdapter.submitList(paymentsWithNames)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}