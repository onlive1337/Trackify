package com.onlive.trackify.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
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

        binding.textViewTitle.text = getString(R.string.bulk_payment_title)
        binding.textViewInstruction.text = getString(R.string.bulk_payment_instruction)

        setupRecyclerView()
        setupBottomPadding()
        observeData()
        setupFilterChips()
        setupActionButtons()
    }

    private fun setupBottomPadding() {
        binding.recyclerViewPayments.clipToPadding = false

        val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val extraPadding = resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
        val totalPadding = bottomNavHeight + extraPadding

        binding.recyclerViewPayments.setPadding(
            binding.recyclerViewPayments.paddingLeft,
            binding.recyclerViewPayments.paddingTop,
            binding.recyclerViewPayments.paddingRight,
            totalPadding
        )
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
        binding.chipAllPayments.text = getString(R.string.all_payments)
        binding.chipPendingPayments.text = getString(R.string.payments_pending_confirmation)
        binding.chipManualPayments.text = getString(R.string.manual_payments)
        binding.chipConfirmedPayments.text = getString(R.string.confirmed_payments)

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
        binding.buttonConfirmSelected.text = getString(R.string.confirm_selected)
        binding.buttonDeleteSelected.text = getString(R.string.delete_selected)

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
                subscriptionName = subscription?.name ?: getString(R.string.unknown)
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
                subscriptionName = subscription?.name ?: getString(R.string.unknown)
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
                subscriptionName = subscription?.name ?: getString(R.string.unknown)
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
                subscriptionName = subscription?.name ?: getString(R.string.unknown)
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

        binding.textViewSelectedCount.text = getString(R.string.selected_count, selectedPayments.size)
    }

    private fun confirmSelectedPayments() {
        if (selectedPayments.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_payments_selected), Toast.LENGTH_SHORT).show()
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
            getString(R.string.payment_confirmed) + ": ${paymentsToConfirm.size}",
            Toast.LENGTH_SHORT
        ).show()

        selectedPayments.clear()
        updateActionButtonsState()
    }

    private fun deleteSelectedPayments() {
        if (selectedPayments.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.no_payments_selected), Toast.LENGTH_SHORT).show()
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
            getString(R.string.payment_deleted) + ": ${paymentsToDelete.size}",
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