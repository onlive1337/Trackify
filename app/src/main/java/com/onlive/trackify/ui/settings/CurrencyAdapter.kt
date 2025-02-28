package com.onlive.trackify.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.databinding.ItemCurrencyBinding

class CurrencyAdapter(
    private val currencies: List<Currency>,
    private val selectedCurrencyCode: String,
    private val onCurrencySelected: (Currency) -> Unit
) : RecyclerView.Adapter<CurrencyAdapter.CurrencyViewHolder>() {

    private var selectedPosition = currencies.indexOfFirst { it.code == selectedCurrencyCode }
        .takeIf { it >= 0 } ?: 0

    inner class CurrencyViewHolder(private val binding: ItemCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currency: Currency, isSelected: Boolean) {
            binding.radioButtonCurrency.apply {
                text = "${currency.symbol} - ${currency.name} (${currency.code})"
                isChecked = isSelected
                setOnClickListener {
                    val previousSelected = selectedPosition
                    selectedPosition = adapterPosition
                    notifyItemChanged(previousSelected)
                    notifyItemChanged(selectedPosition)
                    onCurrencySelected(currency)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) {
        holder.bind(currencies[position], position == selectedPosition)
    }

    override fun getItemCount() = currencies.size
}