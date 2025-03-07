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
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.viewmodel.StatisticsViewModel

class CategoryLegendAdapter : ListAdapter<StatisticsViewModel.CategorySpending, CategoryLegendAdapter.ViewHolder>(
    CategoryDiffCallback()
) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: View = itemView.findViewById(R.id.viewCategoryColor)
        private val nameText: TextView = itemView.findViewById(R.id.textViewCategoryName)
        private val amountText: TextView = itemView.findViewById(R.id.textViewCategoryAmount)
        private val percentText: TextView = itemView.findViewById(R.id.textViewCategoryPercent)

        fun bind(item: StatisticsViewModel.CategorySpending, totalAmount: Double) {
            try {
                colorView.setBackgroundColor(Color.parseColor(item.colorCode))
            } catch (e: Exception) {
                colorView.setBackgroundColor(Color.GRAY)
            }

            nameText.text = item.categoryName
            amountText.text = CurrencyFormatter.formatAmount(itemView.context, item.amount)

            val percentage = if (totalAmount > 0) (item.amount / totalAmount) * 100 else 0.0
            percentText.text = String.format("%.1f%%", percentage)
        }
    }

    private var totalAmount: Double = 0.0

    fun submitData(items: List<StatisticsViewModel.CategorySpending>, total: Double) {
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

    class CategoryDiffCallback : DiffUtil.ItemCallback<StatisticsViewModel.CategorySpending>() {
        override fun areItemsTheSame(
            oldItem: StatisticsViewModel.CategorySpending,
            newItem: StatisticsViewModel.CategorySpending
        ): Boolean {
            return oldItem.categoryId == newItem.categoryId
        }

        override fun areContentsTheSame(
            oldItem: StatisticsViewModel.CategorySpending,
            newItem: StatisticsViewModel.CategorySpending
        ): Boolean {
            return oldItem == newItem
        }
    }
}