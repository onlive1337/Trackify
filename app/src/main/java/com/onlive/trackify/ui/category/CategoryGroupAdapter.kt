package com.onlive.trackify.ui.category

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.databinding.ItemCategoryGroupBinding

class CategoryGroupAdapter(
    private val onGroupClick: (CategoryGroup) -> Unit,
    private val onDeleteClick: (CategoryGroup) -> Unit
) : ListAdapter<CategoryGroup, CategoryGroupAdapter.GroupViewHolder>(GroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemCategoryGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = getItem(position)
        holder.bind(group)
    }

    inner class GroupViewHolder(private val binding: ItemCategoryGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: CategoryGroup) {
            binding.textViewGroupName.text = group.name
            binding.textViewGroupDescription.text = group.description ?: ""

            try {
                val color = Color.parseColor(group.colorCode)
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(color)
                binding.viewGroupColor.background = shape
            } catch (e: IllegalArgumentException) {
                val shape = GradientDrawable()
                shape.shape = GradientDrawable.OVAL
                shape.setColor(Color.GRAY)
                binding.viewGroupColor.background = shape
            }

            binding.root.setOnClickListener {
                onGroupClick(group)
            }

            binding.buttonDeleteGroup.setOnClickListener {
                onDeleteClick(group)
            }
        }
    }

    class GroupDiffCallback : DiffUtil.ItemCallback<CategoryGroup>() {
        override fun areItemsTheSame(oldItem: CategoryGroup, newItem: CategoryGroup): Boolean {
            return oldItem.groupId == newItem.groupId
        }

        override fun areContentsTheSame(oldItem: CategoryGroup, newItem: CategoryGroup): Boolean {
            return oldItem == newItem
        }
    }
}