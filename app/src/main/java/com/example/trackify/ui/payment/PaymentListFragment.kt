package com.example.trackify.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackify.R
import com.example.trackify.data.model.Payment
import com.example.trackify.data.model.Subscription
import com.example.trackify.databinding.FragmentPaymentListBinding
import com.example.trackify.viewmodel.PaymentViewModel
import com.example.trackify.viewmodel.SubscriptionViewModel

class PaymentListFragment : Fragment() {

    private var _binding: FragmentPaymentListBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var paymentAdapter: PaymentAdapter

    // Для хранения списка подписок
    private var subscriptions: List<Subscription> = emptyList()

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

        setupRecyclerView()
        observeData()
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter()

        binding.recyclerViewPayments.apply {
            adapter = paymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeData() {
        // Сначала получаем список подписок
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subs ->
            subscriptions = subs
            updatePaymentsList()
        }

        // Затем наблюдаем за изменениями в платежах
        paymentViewModel.allPayments.observe(viewLifecycleOwner) { payments ->
            updatePaymentsList()

            // Показываем сообщение, если список пуст
            if (payments.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewPayments.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewPayments.visibility = View.VISIBLE
            }
        }
    }

    private fun updatePaymentsList() {
        val payments = paymentViewModel.allPayments.value ?: return
        if (subscriptions.isEmpty()) return

        // Создаем список объектов PaymentWithSubscriptionName
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