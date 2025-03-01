package com.onlive.trackify.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.databinding.ItemBulkPaymentBinding
import com.onlive.trackify.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class BulkPaymentAdapter(
    private val onPaymentChecked: (Payment, Boolean) -> Unit
) : ListAdapter<PaymentWithSubscriptionName, BulkPaymentAdapter.BulkPaymentViewHolder>(PaymentDiffCallback()) {

    private val checkedItems = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BulkPaymentViewHolder {
        val binding = ItemBulkPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BulkPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BulkPaymentViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class BulkPaymentViewHolder(private val binding: ItemBulkPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentWithName: PaymentWithSubscriptionName) {
            val payment = paymentWithName.payment
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

            binding.textViewPaymentDate.text = dateFormat.format(payment.date)
            binding.textViewPaymentAmount.text = CurrencyFormatter.formatAmount(
                binding.root.context,
                payment.amount
            )
            binding.textViewPaymentSubscription.text = paymentWithName.subscriptionName

            when (payment.status) {
                PaymentStatus.PENDING -> {
                    binding.chipStatus.text = "Ожидает"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                }
                PaymentStatus.CONFIRMED -> {
                    binding.chipStatus.text = "Подтвержден"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                }
                PaymentStatus.MANUAL -> {
                    binding.chipStatus.text = "Ручной"
                    binding.chipStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                }
            }

            if (payment.notes.isNullOrEmpty()) {
                binding.textViewPaymentNotes.visibility = View.GONE
            } else {
                binding.textViewPaymentNotes.text = payment.notes
                binding.textViewPaymentNotes.visibility = View.VISIBLE
            }

            binding.checkboxPayment.isChecked = checkedItems.contains(payment.paymentId)

            binding.checkboxPayment.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkedItems.add(payment.paymentId)
                } else {
                    checkedItems.remove(payment.paymentId)
                }
                onPaymentChecked(payment, isChecked)
            }

            binding.root.setOnClickListener {
                binding.checkboxPayment.isChecked = !binding.checkboxPayment.isChecked
            }
        }
    }

    override fun submitList(list: List<PaymentWithSubscriptionName>?) {
        checkedItems.clear()
        super.submitList(list)
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<PaymentWithSubscriptionName>() {
        override fun areItemsTheSame(oldItem: PaymentWithSubscriptionName, newItem: PaymentWithSubscriptionName): Boolean {
            return oldItem.payment.paymentId == newItem.payment.paymentId
        }

        override fun areContentsTheSame(oldItem: PaymentWithSubscriptionName, newItem: PaymentWithSubscriptionName): Boolean {
            return oldItem.payment == newItem.payment &&
                    oldItem.subscriptionName == newItem.subscriptionName
        }
    }
}