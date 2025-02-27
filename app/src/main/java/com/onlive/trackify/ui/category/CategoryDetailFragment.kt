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
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.databinding.FragmentCategoryDetailBinding
import com.onlive.trackify.viewmodel.CategoryViewModel

class CategoryDetailFragment : Fragment() {

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private val args: CategoryDetailFragmentArgs by navArgs()

    private var currentColorCode = "#FF5252"
    private lateinit var colorAdapter: ColorPickerAdapter

    private val predefinedColors = listOf(
        "#FF5252", // Красный
        "#FF4081", // Розовый
        "#E040FB", // Пурпурный
        "#7C4DFF", // Фиолетовый
        "#536DFE", // Индиго
        "#448AFF", // Синий
        "#40C4FF", // Голубой
        "#18FFFF", // Бирюзовый
        "#64FFDA", // Мятный
        "#69F0AE", // Зеленый
        "#B2FF59", // Лаймовый
        "#EEFF41", // Желтый
        "#FFFF00", // Ярко-желтый
        "#FFD740", // Янтарный
        "#FFAB40", // Оранжевый
        "#FF6E40", // Глубокий оранжевый
        "#8D6E63", // Коричневый
        "#BDBDBD", // Серый
        "#212121"  // Черный
    )

    private var category: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupColorPicker()

        val categoryId = args.categoryId
        if (categoryId != -1L) {
            loadCategory(categoryId)
        } else {
            updateColorPreview(currentColorCode)
        }

        binding.buttonSave.setOnClickListener {
            saveCategory()
        }

        binding.buttonDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        if (categoryId == -1L) {
            binding.buttonDelete.visibility = View.GONE
        }
    }

    private fun setupColorPicker() {
        colorAdapter = ColorPickerAdapter(predefinedColors) { colorCode ->
            currentColorCode = colorCode
            updateColorPreview(colorCode)
        }

        binding.recyclerViewColors.apply {
            adapter = colorAdapter
            layoutManager = GridLayoutManager(requireContext(), 5) // 5 цветов в ряду
        }
    }

    private fun loadCategory(categoryId: Long) {
        categoryViewModel.getCategoryById(categoryId).observe(viewLifecycleOwner) { cat ->
            if (cat != null) {
                category = cat
                binding.categoryNameEditText.setText(cat.name)
                currentColorCode = cat.colorCode
                updateColorPreview(cat.colorCode)

                colorAdapter.setSelectedColor(cat.colorCode)
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

    private fun saveCategory() {
        val name = binding.categoryNameEditText.text.toString().trim()

        if (name.isEmpty()) {
            binding.categoryNameEditText.error = "Введите название категории"
            return
        }

        if (category != null) {
            val updatedCategory = category!!.copy(
                name = name,
                colorCode = currentColorCode
            )
            categoryViewModel.update(updatedCategory)
            Toast.makeText(requireContext(), "Категория обновлена", Toast.LENGTH_SHORT).show()
        } else {
            val newCategory = Category(
                name = name,
                colorCode = currentColorCode
            )
            categoryViewModel.insert(newCategory)
            Toast.makeText(requireContext(), "Категория создана", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Удаление категории")
            .setMessage("Вы уверены, что хотите удалить эту категорию? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { _, _ ->
                category?.let {
                    categoryViewModel.delete(it)
                    Toast.makeText(requireContext(), "Категория удалена", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}