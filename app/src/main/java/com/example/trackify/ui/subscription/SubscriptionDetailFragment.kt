package com.example.trackify.ui.subscription

import android.app.AlertDialog
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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackify.data.model.BillingFrequency
import com.example.trackify.data.model.Category
import com.example.trackify.data.model.Subscription
import com.example.trackify.databinding.FragmentSubscriptionDetailBinding
import com.example.trackify.ui.payment.PaymentAdapter
import com.example.trackify.ui.payment.PaymentWithSubscriptionName
import com.example.trackify.viewmodel.CategoryViewModel
import com.example.trackify.viewmodel.PaymentViewModel
import com.example.trackify.viewmodel.SubscriptionViewModel
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

        // Настройка RecyclerView для платежей
        setupPaymentsRecyclerView()

        // Настройка выбора даты
        setupDatePickers()

        // Настройка выбора категории
        setupCategorySpinner()

        // Загрузка подписки по ID из аргументов
        loadSubscription()

        // Обработчики кнопок
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
        // Формат для отображения даты
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

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

            // Если у нас уже есть подписка, выберем её категорию
            subscription?.let { sub ->
                selectCategoryInSpinner(sub.categoryId)
            }
        }
    }

    private fun loadSubscription() {
        val subscriptionId = args.subscriptionId

        // Загрузка данных подписки
        subscriptionViewModel.getSubscriptionById(subscriptionId).observe(viewLifecycleOwner) { sub ->
            if (sub != null) {
                subscription = sub
                updateUI(sub)
            }
        }

        // Загрузка платежей для этой подписки
        paymentViewModel.getPaymentsBySubscription(subscriptionId).observe(viewLifecycleOwner) { payments ->
            if (payments.isEmpty()) {
                binding.textViewNoPayments.visibility = View.VISIBLE
                binding.recyclerViewPayments.visibility = View.GONE
            } else {
                binding.textViewNoPayments.visibility = View.GONE
                binding.recyclerViewPayments.visibility = View.VISIBLE

                // Получаем название подписки
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
        // Заполнение полей формы данными подписки
        binding.editTextName.setText(subscription.name)
        binding.editTextDescription.setText(subscription.description)
        binding.editTextPrice.setText(subscription.price.toString())

        // Частота оплаты
        when (subscription.billingFrequency) {
            BillingFrequency.MONTHLY -> binding.radioButtonMonthly.isChecked = true
            BillingFrequency.YEARLY -> binding.radioButtonYearly.isChecked = true
        }

        // Даты
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

        startDate = subscription.startDate
        binding.buttonSelectStartDate.text = dateFormat.format(startDate)

        endDate = subscription.endDate
        if (endDate != null) {
            binding.buttonSelectEndDate.text = dateFormat.format(endDate!!)
        } else {
            binding.buttonSelectEndDate.text = "Бессрочно"
        }

        // Категория
        selectCategoryInSpinner(subscription.categoryId)

        // Статус активности
        binding.switchActive.isChecked = subscription.active
    }

    private fun selectCategoryInSpinner(categoryId: Long?) {
        if (categoryId == null || categories.isEmpty()) {
            binding.spinnerCategory.setSelection(0)
            return
        }

        // Ищем индекс категории в списке
        val index = categories.indexOfFirst { it.categoryId == categoryId }
        if (index != -1) {
            // +1 потому что первый элемент - "Без категории"
            binding.spinnerCategory.setSelection(index + 1)
        } else {
            binding.spinnerCategory.setSelection(0)
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

        // Активность подписки
        val isActive = binding.switchActive.isChecked

        // Создание обновленного объекта подписки
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

            // Сохранение в базу данных
            subscriptionViewModel.update(updatedSubscription)

            // Сообщение об успешном сохранении
            Toast.makeText(requireContext(), "Подписка обновлена", Toast.LENGTH_SHORT).show()

            // Возвращаемся назад
            findNavController().popBackStack()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление подписки")
            .setMessage("Вы уверены, что хотите удалить эту подписку? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                subscription?.let {
                    subscriptionViewModel.delete(it)
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}