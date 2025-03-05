package com.onlive.trackify.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentAddSubscriptionBinding
import com.onlive.trackify.utils.DatePickerHelper
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddSubscriptionFragment : Fragment() {

    private var _binding: FragmentAddSubscriptionBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var startDate: Date = calendar.time
    private var endDate: Date? = null

    private var categories = listOf<Category>()
    private lateinit var categoryAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddSubscriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePickers()
        setupCategorySpinner()

        binding.buttonSave.setOnClickListener {
            saveSubscription()
        }
    }

    private fun setupDatePickers() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val datePickerHelper = DatePickerHelper(requireContext())

        binding.buttonSelectStartDate.text = dateFormat.format(startDate)
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
        binding.buttonSelectEndDate.text = endDate?.let { dateFormat.format(it) }
            ?: context?.getString(R.string.indefinitely)
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
        val noCategoryString = getString(R.string.without_category)

        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf(noCategoryString)
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            categories = categoriesList

            val categoryNames = mutableListOf(noCategoryString)
            categoryNames.addAll(categoriesList.map { it.name })

            categoryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = categoryAdapter
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

        val subscription = Subscription(
            name = name,
            description = description,
            price = price,
            billingFrequency = billingFrequency,
            startDate = startDate,
            endDate = endDate,
            categoryId = categoryId,
            active = true
        )

        subscriptionViewModel.insert(subscription)

        Toast.makeText(requireContext(), getString(R.string.subscription_added), Toast.LENGTH_SHORT).show()

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}