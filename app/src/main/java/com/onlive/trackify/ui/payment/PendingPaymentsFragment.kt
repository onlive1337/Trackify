package com.onlive.trackify.ui.payment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentPendingPaymentsBinding
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

class PendingPaymentsFragment : Fragment() {

    private var _binding: FragmentPendingPaymentsBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var pendingPaymentAdapter: PendingPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textViewTitle.text = getString(R.string.pending_payments)
        binding.textViewDescription.text = getString(R.string.payments_pending_confirmation)

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        pendingPaymentAdapter = PendingPaymentAdapter(
            onConfirmClick = { paymentWithName ->
                paymentViewModel.confirmPayment(paymentWithName.payment)
                Toast.makeText(requireContext(), getString(R.string.payment_confirmed), Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { paymentWithName ->
                showDeleteConfirmationDialog(paymentWithName)
            }
        )

        binding.recyclerViewPendingPayments.apply {
            adapter = pendingPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            updatePaymentsList()
        }

        paymentViewModel.pendingPayments.observe(viewLifecycleOwner) { payments ->
            updatePaymentsList()

            if (payments.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewPendingPayments.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewPendingPayments.visibility = View.VISIBLE
            }
        }
    }

    private fun updatePaymentsList() {
        val payments = paymentViewModel.pendingPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return
        if (subscriptions.isEmpty()) return

        val paymentsWithNames = payments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: getString(R.string.unknown)
            )
        }

        pendingPaymentAdapter.submitList(paymentsWithNames)
    }

    private fun showDeleteConfirmationDialog(paymentWithName: PaymentWithSubscriptionName) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_payment_confirmation))
            .setMessage(getString(R.string.delete_payment_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                paymentViewModel.delete(paymentWithName.payment)
                Toast.makeText(requireContext(), getString(R.string.payment_deleted), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}