package com.onlive.trackify.ui.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.ItemSubscriptionBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SubscriptionAdapter(private val onSubscriptionClick: (Subscription) -> Unit) :
    ListAdapter<Subscription, SubscriptionAdapter.SubscriptionViewHolder>(SubscriptionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val binding = ItemSubscriptionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubscriptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SubscriptionViewHolder(private val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubscriptionClick(getItem(position))
                }
            }
        }

        fun bind(subscription: Subscription) {
            binding.textViewSubscriptionName.text = subscription.name

            // Форматирование цены в зависимости от частоты оплаты
            val priceString = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> "₽${subscription.price}/мес"
                BillingFrequency.YEARLY -> "₽${subscription.price}/год"
            }
            binding.textViewSubscriptionPrice.text = priceString

            // Отображение категории (будет заполнено позже из базы данных)
            binding.textViewSubscriptionCategory.text = "Категория" // Временный текст

            // Отображение даты следующего платежа
            val nextPayment = calculateNextPaymentDate(
                subscription.startDate,
                subscription.billingFrequency
            )
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
            binding.textViewNextPayment.text = "Следующий платеж: ${dateFormat.format(nextPayment)}"
        }

        private fun calculateNextPaymentDate(startDate: Date, frequency: BillingFrequency): Date {
            val calendar = Calendar.getInstance()
            calendar.time = startDate

            val today = Calendar.getInstance()

            // Вычисляем следующую дату платежа
            while (calendar.before(today)) {
                when (frequency) {
                    BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                    BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
                }
            }

            return calendar.time
        }
    }

    // DiffUtil для эффективного обновления RecyclerView
    class SubscriptionDiffCallback : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem.subscriptionId == newItem.subscriptionId
        }

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem == newItem
        }
    }
}