package com.onlive.trackify.ui.subscription

import android.app.AlertDialog
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentSubscriptionDetailBinding
import com.onlive.trackify.ui.payment.PaymentAdapter
import com.onlive.trackify.ui.payment.PaymentWithSubscriptionName
import com.onlive.trackify.utils.DatePickerHelper
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SubscriptionDetailFragment : Fragment() {

    private var _binding: FragmentSubscriptionDetailBinding? = null
    private val binding get() = _binding!!

    private val args: SubscriptionDetailFragmentArgs by navArgs()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()

    private lateinit var paymentAdapter: PaymentAdapter

    private val calendar = Calendar.getInstance()
    private var startDate: Date = calendar.time
    private var endDate: Date? = null

    private var categories = listOf<Category>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    private var subscription: Subscription? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPaymentsRecyclerView()

        setupDatePickers()

        setupCategorySpinner()

        loadSubscription()

        binding.buttonSave.setOnClickListener {
            saveSubscription()
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.buttonAddPayment.setOnClickListener {
            subscription?.let { sub ->
                val action = SubscriptionDetailFragmentDirections.actionSubscriptionDetailFragmentToAddPaymentFragment(sub.subscriptionId)
                findNavController().navigate(action)
            }
        }
    }

    private fun setupPaymentsRecyclerView() {
        paymentAdapter = PaymentAdapter()
        binding.recyclerViewPayments.apply {
            adapter = paymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val datePickerHelper = DatePickerHelper(requireContext())

        binding.buttonSelectStartDate.setOnClickListener {
            datePickerHelper.showAdvancedDatePickerForSubscription(
                initialDate = startDate,
                allowNullDate = false
            ) { selectedDate ->
                selectedDate?.let {
                    startDate = it
                    binding.buttonSelectStartDate.text = dateFormat.format(startDate)
                }
            }
        }
        binding.buttonSelectEndDate.setOnClickListener {
            datePickerHelper.showAdvancedDatePickerForSubscription(
                initialDate = endDate ?: Calendar.getInstance().apply {
                    time = startDate
                    add(Calendar.YEAR, 1)
                }.time,
                allowNullDate = true
            ) { selectedDate ->
                endDate = selectedDate
                binding.buttonSelectEndDate.text = selectedDate?.let { dateFormat.format(it) } ?: context?.getString(R.string.indefinitely)
            }
        }
    }

    private fun setupCategorySpinner() {
        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf("Без категории")
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            categories = categoriesList

            val categoryNames = mutableListOf("Без категории")
            categoryNames.addAll(categoriesList.map { it.name })

            categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = categoryAdapter

            subscription?.let { sub ->
                selectCategoryInSpinner(sub.categoryId)
            }
        }
    }

    private fun loadSubscription() {
        val subscriptionId = args.subscriptionId

        subscriptionViewModel.getSubscriptionById(subscriptionId).observe(viewLifecycleOwner) { sub ->
            if (sub != null) {
                subscription = sub
                updateUI(sub)
            }
        }

        paymentViewModel.getPaymentsBySubscription(subscriptionId).observe(viewLifecycleOwner) { payments ->
            if (payments.isEmpty()) {
                binding.textViewNoPayments.visibility = View.VISIBLE
                binding.recyclerViewPayments.visibility = View.GONE
            } else {
                binding.textViewNoPayments.visibility = View.GONE
                binding.recyclerViewPayments.visibility = View.VISIBLE

                subscription?.let { sub ->
                    val paymentsWithName = payments.map { payment ->
                        PaymentWithSubscriptionName(payment, sub.name)
                    }
                    paymentAdapter.submitList(paymentsWithName)
                }
            }
        }
    }

    private fun updateUI(subscription: Subscription) {
        binding.editTextName.setText(subscription.name)
        binding.editTextDescription.setText(subscription.description)
        binding.editTextPrice.setText(subscription.price.toString())

        when (subscription.billingFrequency) {
            BillingFrequency.MONTHLY -> binding.radioButtonMonthly.isChecked = true
            BillingFrequency.YEARLY -> binding.radioButtonYearly.isChecked = true
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

        startDate = subscription.startDate
        binding.buttonSelectStartDate.text = dateFormat.format(startDate)

        endDate = subscription.endDate
        if (endDate != null) {
            binding.buttonSelectEndDate.text = dateFormat.format(endDate!!)
        } else {
            binding.buttonSelectEndDate.text = getString(R.string.indefinitely)
        }

        selectCategoryInSpinner(subscription.categoryId)

        binding.switchActive.isChecked = subscription.active
    }

    private fun selectCategoryInSpinner(categoryId: Long?) {
        if (categoryId == null || categories.isEmpty()) {
            binding.spinnerCategory.setSelection(0)
            return
        }

        val index = categories.indexOfFirst { it.categoryId == categoryId }
        if (index != -1) {
            binding.spinnerCategory.setSelection(index + 1)
        } else {
            binding.spinnerCategory.setSelection(0)
        }
    }

    private fun saveSubscription() {
        val name = binding.editTextName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextName.error = getString(R.string.enter_name)
            return
        }

        val priceText = binding.editTextPrice.text.toString().trim()
        if (priceText.isEmpty()) {
            binding.editTextPrice.error = getString(R.string.enter_price)
            return
        }

        val description = binding.editTextDescription.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.editTextPrice.error = getString(R.string.enter_correct_price)
            return
        }

        val billingFrequency = if (binding.radioButtonMonthly.isChecked) {
            BillingFrequency.MONTHLY
        } else {
            BillingFrequency.YEARLY
        }

        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categoryPosition > 0 && categories.isNotEmpty()) {
            categories[categoryPosition - 1].categoryId
        } else {
            null
        }

        val isActive = binding.switchActive.isChecked

        subscription?.let { sub ->
            val updatedSubscription = sub.copy(
                name = name,
                description = description,
                price = price,
                billingFrequency = billingFrequency,
                startDate = startDate,
                endDate = endDate,
                categoryId = categoryId,
                active = isActive
            )

            subscriptionViewModel.update(updatedSubscription)

            Toast.makeText(requireContext(), getString(R.string.subscription_updated), Toast.LENGTH_SHORT).show()

            findNavController().popBackStack()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_subscription_confirmation)
            .setMessage(R.string.delete_subscription_message)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                subscription?.let {
                    subscriptionViewModel.delete(it)
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}