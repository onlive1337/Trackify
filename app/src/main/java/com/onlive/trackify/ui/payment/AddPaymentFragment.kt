package com.onlive.trackify.ui.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentAddPaymentBinding
import com.onlive.trackify.utils.DatePickerHelper
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPaymentFragment : Fragment() {

    private var _binding: FragmentAddPaymentBinding? = null
    private val binding get() = _binding!!

    private val paymentViewModel: PaymentViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    private val args by navArgs<AddPaymentFragmentArgs>()

    private var selectedSubscriptionId: Long? = null
    private var subscriptions = listOf<Subscription>()
    private lateinit var subscriptionAdapter: ArrayAdapter<String>

    private val calendar = Calendar.getInstance()
    private var paymentDate: Date = calendar.time

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSubscriptionSpinner()
        setupDatePicker()

        binding.buttonSave.setOnClickListener {
            savePayment()
        }

        if (args.subscriptionId != -1L) {
            selectedSubscriptionId = args.subscriptionId
        }
    }

    private fun setupSubscriptionSpinner() {
        subscriptionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubscription.adapter = subscriptionAdapter

        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subs ->
            subscriptions = subs

            val subscriptionNames = subs.map { it.name }

            subscriptionAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                subscriptionNames
            )
            subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubscription.adapter = subscriptionAdapter

            selectedSubscriptionId?.let { id ->
                selectSubscriptionInSpinner(id)
            }
        }
    }

    private fun setupDatePicker() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val datePickerHelper = DatePickerHelper(requireContext())

        binding.buttonSelectDate.text = dateFormat.format(paymentDate)
        binding.buttonSelectDate.setOnClickListener {
            datePickerHelper.showAdvancedDatePickerForSubscription(
                initialDate = paymentDate,
                allowNullDate = false
            ) { selectedDate ->
                selectedDate?.let {
                    paymentDate = it
                    binding.buttonSelectDate.text = dateFormat.format(paymentDate)
                }
            }
        }
    }

    private fun selectSubscriptionInSpinner(subscriptionId: Long) {
        val index = subscriptions.indexOfFirst { it.subscriptionId == subscriptionId }
        if (index != -1) {
            binding.spinnerSubscription.setSelection(index)
        }
    }

    private fun savePayment() {
        if (subscriptions.isEmpty() || binding.spinnerSubscription.selectedItemPosition == -1) {
            Toast.makeText(requireContext(), getString(R.string.select_subscription), Toast.LENGTH_SHORT).show()
            return
        }

        val amountText = binding.editTextAmount.text.toString().trim()
        if (amountText.isEmpty()) {
            binding.editTextAmount.error = getString(R.string.enter_amount)
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.editTextAmount.error = getString(R.string.enter_correct_amount)
            return
        }

        val position = binding.spinnerSubscription.selectedItemPosition
        val subscriptionId = subscriptions[position].subscriptionId

        val notes = binding.editTextNotes.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        val payment = Payment(
            subscriptionId = subscriptionId,
            amount = amount,
            date = paymentDate,
            notes = notes
        )

        paymentViewModel.insert(payment)

        Toast.makeText(requireContext(), getString(R.string.payment_added), Toast.LENGTH_SHORT).show()

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}