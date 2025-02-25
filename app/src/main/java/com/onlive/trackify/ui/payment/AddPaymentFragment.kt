package com.onlive.trackify.ui.payment

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
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentAddPaymentBinding
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

        // Проверяем, был ли передан ID подписки (отличный от -1)
        if (args.subscriptionId != -1L) {
            selectedSubscriptionId = args.subscriptionId
        }
    }

    private fun setupSubscriptionSpinner() {
        // Инициализация адаптера с пустыми данными
        subscriptionAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mutableListOf<String>()
        )
        subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubscription.adapter = subscriptionAdapter

        // Наблюдаем за списком подписок
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subs ->
            subscriptions = subs

            // Обновляем адаптер с новыми данными
            val subscriptionNames = subs.map { it.name }

            subscriptionAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                subscriptionNames
            )
            subscriptionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubscription.adapter = subscriptionAdapter

            // Если был передан ID подписки, выберем её в спиннере
            selectedSubscriptionId?.let { id ->
                selectSubscriptionInSpinner(id)
            }
        }
    }

    private fun setupDatePicker() {
        // Формат для отображения даты
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

        // Установка текущей даты
        binding.buttonSelectDate.text = dateFormat.format(paymentDate)

        // Настройка выбора даты
        binding.buttonSelectDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                paymentDate = calendar.time
                binding.buttonSelectDate.text = dateFormat.format(paymentDate)
            }, year, month, day).show()
        }
    }

    private fun selectSubscriptionInSpinner(subscriptionId: Long) {
        // Ищем индекс подписки в списке
        val index = subscriptions.indexOfFirst { it.subscriptionId == subscriptionId }
        if (index != -1) {
            binding.spinnerSubscription.setSelection(index)
        }
    }

    private fun savePayment() {
        // Проверка выбора подписки
        if (subscriptions.isEmpty() || binding.spinnerSubscription.selectedItemPosition == -1) {
            Toast.makeText(requireContext(), "Выберите подписку", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка ввода суммы
        val amountText = binding.editTextAmount.text.toString().trim()
        if (amountText.isEmpty()) {
            binding.editTextAmount.error = "Введите сумму"
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.editTextAmount.error = "Введите корректную сумму"
            return
        }

        // Получение ID выбранной подписки
        val position = binding.spinnerSubscription.selectedItemPosition
        val subscriptionId = subscriptions[position].subscriptionId

        // Получение примечаний
        val notes = binding.editTextNotes.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        // Создание объекта платежа
        val payment = Payment(
            subscriptionId = subscriptionId,
            amount = amount,
            date = paymentDate,
            notes = notes
        )

        // Сохранение в базу данных
        paymentViewModel.insert(payment)

        // Сообщение об успешном сохранении
        Toast.makeText(requireContext(), "Платеж добавлен", Toast.LENGTH_SHORT).show()

        // Возвращаемся назад
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}