package com.onlive.trackify.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.databinding.FragmentBulkPaymentActionsBinding
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

class BulkPaymentActionsFragment : Fragment() {

    private var _binding: FragmentBulkPaymentActionsBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var bulkPaymentAdapter: BulkPaymentAdapter

    private val selectedPayments = mutableSetOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBulkPaymentActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupFilterChips()
        setupActionButtons()
    }

    private fun setupRecyclerView() {
        bulkPaymentAdapter = BulkPaymentAdapter(
            onPaymentChecked = { payment, isChecked ->
                if (isChecked) {
                    selectedPayments.add(payment.paymentId)
                } else {
                    selectedPayments.remove(payment.paymentId)
                }
                updateActionButtonsState()
            }
        )

        binding.recyclerViewPayments.apply {
            adapter = bulkPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            updatePaymentsList()
        }

        paymentViewModel.allPayments.observe(viewLifecycleOwner) { payments ->
            updatePaymentsList()

            if (payments.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewPayments.visibility = View.GONE
                binding.layoutActions.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewPayments.visibility = View.VISIBLE
                binding.layoutActions.visibility = View.VISIBLE
            }
        }
    }

    private fun setupFilterChips() {
        binding.chipAllPayments.setOnClickListener {
            updatePaymentsList()
        }

        binding.chipPendingPayments.setOnClickListener {
            filterPendingPayments()
        }

        binding.chipManualPayments.setOnClickListener {
            filterManualPayments()
        }

        binding.chipConfirmedPayments.setOnClickListener {
            filterConfirmedPayments()
        }

        binding.chipAllPayments.isChecked = true
    }

    private fun setupActionButtons() {
        binding.buttonConfirmSelected.setOnClickListener {
            confirmSelectedPayments()
        }

        binding.buttonDeleteSelected.setOnClickListener {
            deleteSelectedPayments()
        }

        updateActionButtonsState()
    }

    private fun updatePaymentsList() {
        val payments = paymentViewModel.allPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return

        val paymentsWithNames = payments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: "Неизвестно"
            )
        }

        bulkPaymentAdapter.submitList(paymentsWithNames)

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun filterPendingPayments() {
        val payments = paymentViewModel.allPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return

        val pendingPayments = payments.filter { it.status == PaymentStatus.PENDING }

        val paymentsWithNames = pendingPayments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: "Неизвестно"
            )
        }

        bulkPaymentAdapter.submitList(paymentsWithNames)

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun filterManualPayments() {
        val payments = paymentViewModel.allPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return

        val manualPayments = payments.filter { it.status == PaymentStatus.MANUAL }

        val paymentsWithNames = manualPayments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: "Неизвестно"
            )
        }

        bulkPaymentAdapter.submitList(paymentsWithNames)

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun filterConfirmedPayments() {
        val payments = paymentViewModel.allPayments.value ?: return
        val subscriptions = subscriptionViewModel.allActiveSubscriptions.value ?: return

        val confirmedPayments = payments.filter { it.status == PaymentStatus.CONFIRMED }

        val paymentsWithNames = confirmedPayments.map { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentWithSubscriptionName(
                payment = payment,
                subscriptionName = subscription?.name ?: "Неизвестно"
            )
        }

        bulkPaymentAdapter.submitList(paymentsWithNames)

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun updateActionButtonsState() {
        val hasSelected = selectedPayments.isNotEmpty()

        binding.buttonConfirmSelected.isEnabled = hasSelected
        binding.buttonDeleteSelected.isEnabled = hasSelected

        binding.textViewSelectedCount.text = "Выбрано: ${selectedPayments.size}"
    }

    private fun confirmSelectedPayments() {
        if (selectedPayments.isEmpty()) {
            Toast.makeText(requireContext(), "Не выбрано ни одного платежа", Toast.LENGTH_SHORT).show()
            return
        }

        val payments = paymentViewModel.allPayments.value ?: return

        val paymentsToConfirm = payments.filter { payment ->
            selectedPayments.contains(payment.paymentId) && payment.status != PaymentStatus.CONFIRMED
        }

        for (payment in paymentsToConfirm) {
            paymentViewModel.confirmPayment(payment)
        }

        Toast.makeText(
            requireContext(),
            "Подтверждено платежей: ${paymentsToConfirm.size}",
            Toast.LENGTH_SHORT
        ).show()

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun deleteSelectedPayments() {
        if (selectedPayments.isEmpty()) {
            Toast.makeText(requireContext(), "Не выбрано ни одного платежа", Toast.LENGTH_SHORT).show()
            return
        }

        val payments = paymentViewModel.allPayments.value ?: return

        val paymentsToDelete = payments.filter { payment ->
            selectedPayments.contains(payment.paymentId)
        }

        for (payment in paymentsToDelete) {
            paymentViewModel.delete(payment)
        }

        Toast.makeText(
            requireContext(),
            "Удалено платежей: ${paymentsToDelete.size}",
            Toast.LENGTH_SHORT
        ).show()

        selectedPayments.clear()
        updateActionButtonsState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}