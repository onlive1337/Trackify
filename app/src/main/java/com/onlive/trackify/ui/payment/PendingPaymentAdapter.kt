package com.onlive.trackify.ui.payment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.databinding.ItemPendingPaymentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PendingPaymentAdapter(
    private val onConfirmClick: (PaymentWithSubscriptionName) -> Unit,
    private val onDeleteClick: (PaymentWithSubscriptionName) -> Unit
) : ListAdapter<PaymentWithSubscriptionName, PendingPaymentAdapter.PendingPaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingPaymentViewHolder {
        val binding = ItemPendingPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PendingPaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingPaymentViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class PendingPaymentViewHolder(private val binding: ItemPendingPaymentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentWithName: PaymentWithSubscriptionName) {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            val payment = paymentWithName.payment

            binding.textViewPaymentDate.text = dateFormat.format(payment.date)
            binding.textViewPaymentAmount.text = "₽${payment.amount}"
            binding.textViewPaymentSubscription.text = paymentWithName.subscriptionName

            if (payment.notes.isNullOrEmpty()) {
                binding.textViewPaymentNotes.visibility = View.GONE
            } else {
                binding.textViewPaymentNotes.text = payment.notes
                binding.textViewPaymentNotes.visibility = View.VISIBLE
            }

            binding.buttonConfirmPayment.setOnClickListener {
                onConfirmClick(paymentWithName)
            }

            binding.buttonDeletePayment.setOnClickListener {
                onDeleteClick(paymentWithName)
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