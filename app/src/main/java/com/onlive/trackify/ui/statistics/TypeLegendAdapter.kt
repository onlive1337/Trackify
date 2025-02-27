package com.onlive.trackify.ui.statistics

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.R
import com.onlive.trackify.viewmodel.StatisticsViewModel
import java.text.NumberFormat
import java.util.Locale

class TypeLegendAdapter : ListAdapter<StatisticsViewModel.SubscriptionTypeSpending, TypeLegendAdapter.ViewHolder>(
    TypeDiffCallback()
) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.viewCategoryColor)
        private val nameText: TextView = itemView.findViewById(R.id.textViewCategoryName)
        private val amountText: TextView = itemView.findViewById(R.id.textViewCategoryAmount)
        private val percentText: TextView = itemView.findViewById(R.id.textViewCategoryPercent)

        fun bind(item: StatisticsViewModel.SubscriptionTypeSpending, totalAmount: Double) {
            try {
                colorView.setBackgroundColor(Color.parseColor(item.colorCode))
            } catch (e: Exception) {
                colorView.setBackgroundColor(Color.GRAY)
            }

            nameText.text = item.type

            val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
            format.maximumFractionDigits = 0
            amountText.text = format.format(item.amount)

            val percentage = if (totalAmount > 0) (item.amount / totalAmount) * 100 else 0.0
            percentText.text = String.format("%.1f%%", percentage)
        }
    }

    private var totalAmount: Double = 0.0

    fun submitData(items: List<StatisticsViewModel.SubscriptionTypeSpending>, total: Double) {
        totalAmount = total
        submitList(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_legend, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), totalAmount)
    }

    class TypeDiffCallback : DiffUtil.ItemCallback<StatisticsViewModel.SubscriptionTypeSpending>() {
        override fun areItemsTheSame(
            oldItem: StatisticsViewModel.SubscriptionTypeSpending,
            newItem: StatisticsViewModel.SubscriptionTypeSpending
        ): Boolean {
            return oldItem.type == newItem.type
        }

        override fun areContentsTheSame(
            oldItem: StatisticsViewModel.SubscriptionTypeSpending,
            newItem: StatisticsViewModel.SubscriptionTypeSpending
        ): Boolean {
            return oldItem == newItem
        }
    }
}