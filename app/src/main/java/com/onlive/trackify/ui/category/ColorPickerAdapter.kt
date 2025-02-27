package com.onlive.trackify.ui.category

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.R
import com.onlive.trackify.databinding.ItemColorBinding

class ColorPickerAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemColorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        holder.bind(color, position == selectedPosition)
    }

    override fun getItemCount(): Int = colors.size

    fun setSelectedColor(colorCode: String) {
        val position = colors.indexOf(colorCode)
        if (position != -1 && position != selectedPosition) {
            val oldPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
        } else if (position == -1 && colors.isNotEmpty()) {
            selectedPosition = 0
            notifyItemChanged(selectedPosition)
        }
    }

    inner class ColorViewHolder(private val binding: ItemColorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onColorSelected(colors[selectedPosition])
            }
        }

        fun bind(colorCode: String, isSelected: Boolean) {
            try {
                val color = Color.parseColor(colorCode)
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(color)
                binding.viewColor.background = shape
            } catch (e: IllegalArgumentException) {
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(Color.GRAY)
                binding.viewColor.background = shape
            }

            val borderColor = if (isSelected) {
                ContextCompat.getColor(itemView.context, R.color.md_theme_light_primary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.md_theme_light_outline)
            }

            val borderWidth = if (isSelected) {
                itemView.context.resources.getDimensionPixelSize(R.dimen.color_picker_selected_border_width)
            } else {
                itemView.context.resources.getDimensionPixelSize(R.dimen.color_picker_border_width)
            }

            binding.colorBorder.setStrokeColor(borderColor)
            binding.colorBorder.setStrokeWidth(borderWidth)
        }
    }
}