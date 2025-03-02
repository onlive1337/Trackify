package com.onlive.trackify.ui.category

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.databinding.FragmentCategoryGroupDetailBinding
import com.onlive.trackify.viewmodel.CategoryGroupViewModel
import com.onlive.trackify.viewmodel.CategoryViewModel

class CategoryGroupDetailFragment : Fragment() {

    private var _binding: FragmentCategoryGroupDetailBinding? = null
    private val binding get() = _binding!!

    private val categoryGroupViewModel: CategoryGroupViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val args: CategoryGroupDetailFragmentArgs by navArgs()

    private var currentColorCode = "#FF5252"
    private lateinit var colorAdapter: ColorPickerAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private val predefinedColors = listOf(
        "#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
        "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41", "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40",
        "#8D6E63", "#BDBDBD", "#212121"
    )

    private var group: CategoryGroup? = null
    private var categories = listOf<Category>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryGroupDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupColorPicker()
        setupCategoriesList()

        val groupId = args.groupId
        if (groupId != -1L) {
            loadGroup(groupId)
            loadGroupCategories(groupId)
        } else {
            updateColorPreview(currentColorCode)
            binding.buttonDelete.visibility = View.GONE
        }

        binding.buttonSave.setOnClickListener {
            saveGroup()
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun setupColorPicker() {
        colorAdapter = ColorPickerAdapter(predefinedColors) { colorCode ->
            currentColorCode = colorCode
            updateColorPreview(colorCode)
        }

        binding.recyclerViewColors.apply {
            adapter = colorAdapter
            layoutManager = GridLayoutManager(requireContext(), 5)
        }
    }

    private fun setupCategoriesList() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { /* Не требуется действие при клике */ },
            onDeleteClick = { /* Не требуется удаление отсюда */ }
        )

        binding.recyclerViewCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { allCategories ->
            categories = allCategories

            group?.let { loadedGroup ->
                val groupCategories = categories.filter { it.groupId == loadedGroup.groupId }
                categoryAdapter.submitList(groupCategories)

                if (groupCategories.isEmpty()) {
                    binding.textViewNoCategories.visibility = View.VISIBLE
                    binding.recyclerViewCategories.visibility = View.GONE
                } else {
                    binding.textViewNoCategories.visibility = View.GONE
                    binding.recyclerViewCategories.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadGroup(groupId: Long) {
        categoryGroupViewModel.getGroupById(groupId).observe(viewLifecycleOwner) { loadedGroup ->
            if (loadedGroup != null) {
                group = loadedGroup
                binding.editTextGroupName.setText(loadedGroup.name)
                binding.editTextGroupDescription.setText(loadedGroup.description ?: "")
                currentColorCode = loadedGroup.colorCode
                updateColorPreview(loadedGroup.colorCode)
                colorAdapter.setSelectedColor(loadedGroup.colorCode)
            }
        }
    }

    private fun loadGroupCategories(groupId: Long) {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { allCategories ->
            val groupCategories = allCategories.filter { it.groupId == groupId }
            categoryAdapter.submitList(groupCategories)

            if (groupCategories.isEmpty()) {
                binding.textViewNoCategories.visibility = View.VISIBLE
                binding.recyclerViewCategories.visibility = View.GONE
            } else {
                binding.textViewNoCategories.visibility = View.GONE
                binding.recyclerViewCategories.visibility = View.VISIBLE
            }
        }
    }

    private fun updateColorPreview(colorCode: String) {
        try {
            val color = Color.parseColor(colorCode)
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(color)
            binding.viewColorPreview.background = shape
        } catch (e: IllegalArgumentException) {
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            shape.setColor(Color.GRAY)
            binding.viewColorPreview.background = shape
        }
    }

    private fun saveGroup() {
        val name = binding.editTextGroupName.text.toString().trim()

        if (name.isEmpty()) {
            binding.editTextGroupName.error = getString(R.string.enter_category_name)
            return
        }

        val description = binding.editTextGroupDescription.text.toString().trim().let {
            if (it.isEmpty()) null else it
        }

        if (group != null) {
            val updatedGroup = group!!.copy(
                name = name,
                description = description,
                colorCode = currentColorCode
            )
            categoryGroupViewModel.update(updatedGroup)
            Toast.makeText(requireContext(), getString(R.string.group_updated), Toast.LENGTH_SHORT).show()
        } else {
            val newGroup = CategoryGroup(
                name = name,
                description = description,
                colorCode = currentColorCode
            )
            categoryGroupViewModel.insert(newGroup)
            Toast.makeText(requireContext(), getString(R.string.group_added), Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_group_confirmation))
            .setMessage(getString(R.string.delete_group_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                group?.let {
                    categoryGroupViewModel.delete(it)
                    Toast.makeText(requireContext(), getString(R.string.group_deleted), Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}