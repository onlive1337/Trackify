package com.onlive.trackify.ui.overview

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.databinding.ItemUpcomingPaymentBinding
import com.onlive.trackify.ui.overview.UpcomingPaymentsFragment.UpcomingPayment
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class UpcomingPaymentAdapter :
    ListAdapter<UpcomingPayment, UpcomingPaymentAdapter.UpcomingPaymentViewHolder>(UpcomingPaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingPaymentViewHolder {
        val binding = ItemUpcomingPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UpcomingPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UpcomingPaymentViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class UpcomingPaymentViewHolder(private val binding: ItemUpcomingPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(upcomingPayment: UpcomingPayment) {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
            currencyFormat.maximumFractionDigits = 0

            binding.textViewSubscriptionName.text = upcomingPayment.subscription.name
            binding.textViewPaymentDate.text = dateFormat.format(upcomingPayment.paymentDate)
            binding.textViewPaymentAmount.text = currencyFormat.format(upcomingPayment.subscription.price)

            val daysText = when(upcomingPayment.daysUntil) {
                0 -> "Сегодня"
                1 -> "Завтра"
                else -> "Через ${upcomingPayment.daysUntil} дней"
            }
            binding.textViewDaysUntil.text = daysText

            val urgencyColor = when {
                upcomingPayment.daysUntil == 0 -> Color.parseColor("#F44336") // Красный для сегодня
                upcomingPayment.daysUntil <= 3 -> Color.parseColor("#FF9800") // Оранжевый для 1-3 дней
                upcomingPayment.daysUntil <= 7 -> Color.parseColor("#FFC107") // Желтый для 4-7 дней
                else -> Color.parseColor("#4CAF50") // Зеленый для остальных
            }
            binding.viewUrgencyIndicator.setBackgroundColor(urgencyColor)
        }
    }

    class UpcomingPaymentDiffCallback : DiffUtil.ItemCallback<UpcomingPayment>() {
        override fun areItemsTheSame(oldItem: UpcomingPayment, newItem: UpcomingPayment): Boolean {
            return oldItem.subscription.subscriptionId == newItem.subscription.subscriptionId &&
                    oldItem.paymentDate.time == newItem.paymentDate.time
        }

        override fun areContentsTheSame(oldItem: UpcomingPayment, newItem: UpcomingPayment): Boolean {
            return oldItem == newItem
        }
    }
}