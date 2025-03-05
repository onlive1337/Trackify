package com.onlive.trackify.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.databinding.ItemPaymentBinding
import com.onlive.trackify.utils.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentAdapter : ListAdapter<PaymentWithSubscriptionName, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class PaymentViewHolder(private val binding: ItemPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentWithName: PaymentWithSubscriptionName) {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

            binding.textViewPaymentDate.text = dateFormat.format(paymentWithName.payment.date)
            binding.textViewPaymentAmount.text = CurrencyFormatter.formatAmount(
                binding.root.context,
                paymentWithName.payment.amount
            )
            binding.textViewPaymentSubscription.text = paymentWithName.subscriptionName

            val chipText = when (paymentWithName.payment.status) {
                PaymentStatus.PENDING -> binding.root.context.getString(R.string.payment_status_pending)
                PaymentStatus.CONFIRMED -> binding.root.context.getString(R.string.payment_status_confirmed)
                PaymentStatus.MANUAL -> binding.root.context.getString(R.string.payment_status_manual)
            }

            binding.chipPaymentStatus.text = chipText
            binding.chipPaymentStatus.visibility = View.VISIBLE

            if (paymentWithName.payment.notes.isNullOrEmpty()) {
                binding.textViewPaymentNotes.visibility = View.GONE
            } else {
                binding.textViewPaymentNotes.text = paymentWithName.payment.notes
                binding.textViewPaymentNotes.visibility = View.VISIBLE
            }
        }
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

data class PaymentWithSubscriptionName(
    val payment: Payment,
    val subscriptionName: String
)