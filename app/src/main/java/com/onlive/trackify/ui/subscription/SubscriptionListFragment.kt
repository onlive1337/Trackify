package com.onlive.trackify.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.databinding.FragmentSubscriptionListBinding
import com.onlive.trackify.utils.AnimationUtils
import com.onlive.trackify.utils.ViewModePreference
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubscriptionListFragment : Fragment(), MenuProvider {

    private var _binding: FragmentSubscriptionListBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var subscriptionAdapter: SubscriptionAdapter
    private lateinit var viewModePreference: ViewModePreference

    private var originalSubscriptionsList = listOf<Subscription>()
    private var currentQuery = ""
    private var isGridMode = false
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubscriptionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        viewModePreference = ViewModePreference(requireContext())
        isGridMode = viewModePreference.isGridModeEnabled()

        binding.fabAddSubscription.setOnClickListener {
            AnimationUtils.pulseAnimation(it)
            findNavController().navigate(R.id.action_navigation_subscriptions_to_addSubscriptionFragment)
        }

        setupRecyclerView()
        setupViewModeToggle()
        observeSubscriptions()
        setupSearchView()
    }

    private fun setupSearchView() {
        binding.searchViewSubscriptions.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()

                currentQuery = newText ?: ""

                searchJob = lifecycleScope.launch {
                    delay(300)
                    filterSubscriptions(currentQuery)
                }
                return true
            }
        })

        binding.searchViewSubscriptions.setOnCloseListener {
            currentQuery = ""
            filterSubscriptions(currentQuery)
            false
        }
    }

    private fun setupViewModeToggle() {
        updateViewModeIcon()

        binding.buttonToggleViewMode.setOnClickListener {
            AnimationUtils.pulseAnimation(it)
            isGridMode = !isGridMode
            viewModePreference.setGridModeEnabled(isGridMode)
            updateViewModeIcon()
            updateLayoutManager()
            subscriptionAdapter.setLayoutMode(isGridMode)
        }
    }

    private fun updateViewModeIcon() {
        binding.buttonToggleViewMode.setImageResource(
            if (isGridMode) R.drawable.ic_view_list else R.drawable.ic_view_grid
        )
    }

    private fun updateLayoutManager() {
        binding.recyclerViewSubscriptions.layoutManager = if (isGridMode) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }
    }

    private fun filterSubscriptions(query: String) {
        if (query.isEmpty()) {
            subscriptionAdapter.submitList(originalSubscriptionsList)
            updateEmptyView(originalSubscriptionsList.isEmpty())
            return
        }

        val filteredList = originalSubscriptionsList.filter { subscription ->
            subscription.name.contains(query, ignoreCase = true) ||
                    (subscription.description?.contains(query, ignoreCase = true) ?: false)
        }

        subscriptionAdapter.submitList(filteredList)
        updateEmptyView(filteredList.isEmpty(), isFiltered = true)
    }

    private fun updateEmptyView(isEmpty: Boolean, isFiltered: Boolean = false) {
        if (isEmpty) {
            binding.textViewEmpty.visibility = View.VISIBLE
            binding.recyclerViewSubscriptions.visibility = View.GONE

            binding.textViewEmpty.text = if (isFiltered) {
                "Подписки не найдены"
            } else {
                "У вас пока нет подписок"
            }
        } else {
            binding.textViewEmpty.visibility = View.GONE
            binding.recyclerViewSubscriptions.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter(
            onSubscriptionClick = { subscription ->
                val action = SubscriptionListFragmentDirections.actionNavigationSubscriptionsToSubscriptionDetailFragment(
                    subscription.subscriptionId
                )
                findNavController().navigate(action)
            },
            isGridMode = isGridMode
        )

        binding.recyclerViewSubscriptions.apply {
            adapter = subscriptionAdapter
            layoutManager = if (isGridMode) {
                GridLayoutManager(requireContext(), 2)
            } else {
                LinearLayoutManager(requireContext())
            }

            clipToPadding = false
            val bottomPadding = getNavigationBarHeight()
            setPadding(paddingLeft, paddingTop, paddingRight, bottomPadding)

            setHasFixedSize(true)
            setItemViewCacheSize(20)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0 && binding.fabAddSubscription.isShown) {
                        binding.fabAddSubscription.hide()
                    } else if (dy < 0 && !binding.fabAddSubscription.isShown) {
                        binding.fabAddSubscription.show()
                    }
                }
            })
        }
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    private fun observeSubscriptions() {
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            binding.progressBar.visibility = if (subscriptions == null) View.VISIBLE else View.GONE

            if (subscriptions != null) {
                originalSubscriptionsList = subscriptions

                if (currentQuery.isNotEmpty()) {
                    filterSubscriptions(currentQuery)
                } else {
                    subscriptionAdapter.submitList(subscriptions)
                    updateEmptyView(subscriptions.isEmpty())
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    override fun onDestroyView() {
        searchJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}