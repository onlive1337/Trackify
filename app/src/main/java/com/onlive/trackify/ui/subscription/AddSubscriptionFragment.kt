package com.onlive.trackify.ui.subscription

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentAddSubscriptionBinding
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
        // Формат для отображения даты
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

        // Установка текущей даты для начальной даты
        binding.buttonSelectStartDate.text = dateFormat.format(startDate)

        // Кнопка выбора начальной даты
        binding.buttonSelectStartDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                startDate = calendar.time
                binding.buttonSelectStartDate.text = dateFormat.format(startDate)
            }, year, month, day).show()
        }

        // Кнопка выбора конечной даты (или бессрочно)
        binding.buttonSelectEndDate.text = "Бессрочно"

        binding.buttonSelectEndDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                endDate = calendar.time
                binding.buttonSelectEndDate.text = dateFormat.format(endDate!!)
            }, year, month, day)

            // Добавляем кнопку "Бессрочно"
            dialog.setButton(DatePickerDialog.BUTTON_NEUTRAL, "Бессрочно") { _, _ ->
                endDate = null
                binding.buttonSelectEndDate.text = "Бессрочно"
            }

            dialog.show()
        }
    }

    private fun setupCategorySpinner() {
        // Создаем простой адаптер с пустым списком (пока)
        categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf("Без категории")
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Наблюдаем за списком категорий
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categoriesList ->
            categories = categoriesList

            // Обновляем адаптер с новыми данными
            val categoryNames = mutableListOf("Без категории")
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
        // Проверка обязательных полей
        val name = binding.editTextName.text.toString().trim()
        if (name.isEmpty()) {
            binding.editTextName.error = "Введите название"
            return
        }

        val priceText = binding.editTextPrice.text.toString().trim()
        if (priceText.isEmpty()) {
            binding.editTextPrice.error = "Введите цену"
            return
        }

        // Получение остальных данных
        val description = binding.editTextDescription.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.editTextPrice.error = "Введите корректную цену"
            return
        }

        // Определение частоты оплаты
        val billingFrequency = if (binding.radioButtonMonthly.isChecked) {
            BillingFrequency.MONTHLY
        } else {
            BillingFrequency.YEARLY
        }

        // Определение категории
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categoryPosition > 0 && categories.isNotEmpty()) {
            categories[categoryPosition - 1].categoryId
        } else {
            null
        }

        // Создание объекта подписки
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

        // Сохранение в базу данных
        subscriptionViewModel.insert(subscription)

        // Сообщение об успешном сохранении
        Toast.makeText(requireContext(), "Подписка добавлена", Toast.LENGTH_SHORT).show()

        // Возвращаемся назад
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}