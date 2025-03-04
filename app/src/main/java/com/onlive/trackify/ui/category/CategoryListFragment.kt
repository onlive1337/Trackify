package com.onlive.trackify.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
        setupBottomPadding()
        observeCategories()

        binding.fabAddGroup.setOnClickListener {
            val action = CategoryListFragmentDirections.actionCategoryListFragmentToCategoryDetailFragment(
                categoryId = -1L
            )
            findNavController().navigate(action)
        }
    }

    private fun setupBottomPadding() {
        binding.recyclerViewGroups.clipToPadding = false

        val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val extraPadding = resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
        val totalPadding = bottomNavHeight + extraPadding

        binding.recyclerViewGroups.setPadding(
            binding.recyclerViewGroups.paddingLeft,
            binding.recyclerViewGroups.paddingTop,
            binding.recyclerViewGroups.paddingRight,
            totalPadding
        )
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

        binding.recyclerViewGroups.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
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
                binding.recyclerViewGroups.visibility = View.GONE
            } else {
                binding.textViewEmptyList.visibility = View.GONE
                binding.recyclerViewGroups.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.category_sort_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.sort_name_asc -> {
                currentSortOrder = SortOrder.NAME_ASC
                menuItem.isChecked = true
                observeCategories()
                true
            }
            R.id.sort_name_desc -> {
                currentSortOrder = SortOrder.NAME_DESC
                menuItem.isChecked = true
                observeCategories()
                true
            }
            R.id.sort_color -> {
                currentSortOrder = SortOrder.COLOR
                menuItem.isChecked = true
                observeCategories()
                true
            }
            else -> false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SortOrder {
        NAME_ASC, NAME_DESC, COLOR
    }
}