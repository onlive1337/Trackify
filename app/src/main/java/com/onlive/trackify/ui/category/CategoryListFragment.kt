package com.onlive.trackify.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentCategoryListBinding
import com.onlive.trackify.viewmodel.CategoryViewModel

class CategoryListFragment : Fragment(), MenuProvider {

    private var _binding: FragmentCategoryListBinding? = null
    private val binding get() = _binding!!

    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentSortOrder = SortOrder.NAME_ASC

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setupRecyclerView()
        observeCategories()

        binding.fabAddCategory.setOnClickListener {
            val action = CategoryListFragmentDirections.actionCategoryListFragmentToCategoryDetailFragment(
                categoryId = -1L
            )
            findNavController().navigate(action)
        }

        binding.buttonSortOptions.setOnClickListener { view ->
            showSortOptionsMenu(view)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                val action = CategoryListFragmentDirections.actionCategoryListFragmentToCategoryDetailFragment(
                    categoryId = category.categoryId
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { category ->
                categoryViewModel.delete(category)
            }
        )

        binding.recyclerViewCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())

            clipToPadding = false
            val bottomPadding = resources.getDimensionPixelSize(R.dimen.fab_bottom_padding)
            setPadding(paddingLeft, paddingTop, paddingRight, bottomPadding)
        }
    }

    private fun observeCategories() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val sortedCategories = when (currentSortOrder) {
                SortOrder.NAME_ASC -> categories.sortedBy { it.name }
                SortOrder.NAME_DESC -> categories.sortedByDescending { it.name }
                SortOrder.COLOR -> categories.sortedBy { it.colorCode }
            }

            categoryAdapter.submitList(sortedCategories)

            if (sortedCategories.isEmpty()) {
                binding.textViewEmptyList.visibility = View.VISIBLE
                binding.recyclerViewCategories.visibility = View.GONE
            } else {
                binding.textViewEmptyList.visibility = View.GONE
                binding.recyclerViewCategories.visibility = View.VISIBLE
            }
        }
    }

    private fun showSortOptionsMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.category_sort_menu, popup.menu)

        when (currentSortOrder) {
            SortOrder.NAME_ASC -> popup.menu.findItem(R.id.sort_name_asc).isChecked = true
            SortOrder.NAME_DESC -> popup.menu.findItem(R.id.sort_name_desc).isChecked = true
            SortOrder.COLOR -> popup.menu.findItem(R.id.sort_color).isChecked = true
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort_name_asc -> {
                    currentSortOrder = SortOrder.NAME_ASC
                    observeCategories()
                    true
                }
                R.id.sort_name_desc -> {
                    currentSortOrder = SortOrder.NAME_DESC
                    observeCategories()
                    true
                }
                R.id.sort_color -> {
                    currentSortOrder = SortOrder.COLOR
                    observeCategories()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SortOrder {
        NAME_ASC, NAME_DESC, COLOR
    }
}