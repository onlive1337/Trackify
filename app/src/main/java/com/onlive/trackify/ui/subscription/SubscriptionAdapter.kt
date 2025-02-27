package com.onlive.trackify.ui.subscription

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.ItemSubscriptionBinding
import com.onlive.trackify.databinding.ItemSubscriptionGridBinding
import com.onlive.trackify.utils.AnimationUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SubscriptionAdapter(
    private val onSubscriptionClick: (Subscription) -> Unit,
    private var isGridMode: Boolean = false
) : ListAdapter<Subscription, RecyclerView.ViewHolder>(SubscriptionDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_LIST = 1
        private const val VIEW_TYPE_GRID = 2
        private val DATE_FORMAT = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridMode) VIEW_TYPE_GRID else VIEW_TYPE_LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GRID -> {
                val binding = ItemSubscriptionGridBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                GridSubscriptionViewHolder(binding)
            }
            else -> {
                val binding = ItemSubscriptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ListSubscriptionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val subscription = getItem(position)
        when (holder) {
            is ListSubscriptionViewHolder -> holder.bind(subscription)
            is GridSubscriptionViewHolder -> holder.bind(subscription)
        }
    }

    fun setLayoutMode(isGrid: Boolean) {
        if (this.isGridMode != isGrid) {
            this.isGridMode = isGrid
            notifyItemRangeChanged(0, itemCount)
        }
    }

    inner class ListSubscriptionViewHolder(private val binding: ItemSubscriptionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                AnimationUtils.pulseAnimation(it)
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubscriptionClick(getItem(position))
                }
            }
        }

        fun bind(subscription: Subscription) {
            binding.textViewSubscriptionName.text = subscription.name

            val priceString = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> "₽${subscription.price}/мес"
                BillingFrequency.YEARLY -> "₽${subscription.price}/год"
            }
            binding.textViewSubscriptionPrice.text = priceString

            binding.textViewSubscriptionCategory.text = subscription.categoryName ?: "Без категории"

            val nextPayment = calculateNextPaymentDate(
                subscription.startDate,
                subscription.billingFrequency
            )
            binding.textViewNextPayment.text =
                "Следующий платеж: ${DATE_FORMAT.format(nextPayment)}"

            try {
                subscription.categoryColor?.let { colorCode ->
                    binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.parseColor(colorCode))
                } ?: run {
                    binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.LTGRAY)
                }
            } catch (e: Exception) {
                binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.LTGRAY)
            }
        }
    }

    inner class GridSubscriptionViewHolder(private val binding: ItemSubscriptionGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                AnimationUtils.pulseAnimation(it)
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubscriptionClick(getItem(position))
                }
            }
        }

        fun bind(subscription: Subscription) {
            binding.textViewSubscriptionName.text = subscription.name

            val priceString = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> "₽${subscription.price}/мес"
                BillingFrequency.YEARLY -> "₽${subscription.price}/год"
            }
            binding.textViewSubscriptionPrice.text = priceString

            binding.textViewSubscriptionCategory.text = subscription.categoryName ?: "Без категории"

            val nextPayment = calculateNextPaymentDate(
                subscription.startDate,
                subscription.billingFrequency
            )
            binding.textViewNextPayment.text =
                "Следующий платеж: ${DATE_FORMAT.format(nextPayment)}"

            try {
                subscription.categoryColor?.let { colorCode ->
                    binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.parseColor(colorCode))
                } ?: run {
                    binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.LTGRAY)
                }
            } catch (e: Exception) {
                binding.viewCategoryColor.setBackgroundColor(android.graphics.Color.LTGRAY)
            }
        }
    }

    private fun calculateNextPaymentDate(startDate: Date, frequency: BillingFrequency): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val today = Calendar.getInstance()

        while (calendar.before(today)) {
            when (frequency) {
                BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
        }

        return calendar.time
    }

    class SubscriptionDiffCallback : DiffUtil.ItemCallback<Subscription>() {
        override fun areItemsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem.subscriptionId == newItem.subscriptionId
        }

        override fun areContentsTheSame(oldItem: Subscription, newItem: Subscription): Boolean {
            return oldItem == newItem
        }
    }
}