package com.onlive.trackify.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentCategoryGroupListBinding
import com.onlive.trackify.viewmodel.CategoryGroupViewModel

class CategoryGroupListFragment : Fragment() {

    private var _binding: FragmentCategoryGroupListBinding? = null
    private val binding get() = _binding!!

    private val categoryGroupViewModel: CategoryGroupViewModel by viewModels()
    private lateinit var categoryGroupAdapter: CategoryGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryGroupListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBottomPadding()
        observeGroups()

        binding.fabAddGroup.setOnClickListener {
            val action = CategoryGroupListFragmentDirections.actionCategoryGroupListFragmentToCategoryGroupDetailFragment(
                groupId = -1L
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
        categoryGroupAdapter = CategoryGroupAdapter(
            onGroupClick = { group ->
                val action = CategoryGroupListFragmentDirections.actionCategoryGroupListFragmentToCategoryGroupDetailFragment(
                    groupId = group.groupId
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { group ->
                categoryGroupViewModel.delete(group)
            }
        )

        binding.recyclerViewGroups.apply {
            adapter = categoryGroupAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeGroups() {
        categoryGroupViewModel.allGroups.observe(viewLifecycleOwner) { groups ->
            categoryGroupAdapter.submitList(groups)

            if (groups.isEmpty()) {
                binding.textViewEmptyList.visibility = View.VISIBLE
                binding.recyclerViewGroups.visibility = View.GONE
            } else {
                binding.textViewEmptyList.visibility = View.GONE
                binding.recyclerViewGroups.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}