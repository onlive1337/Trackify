package com.onlive.trackify.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentSubscriptionListBinding
import com.onlive.trackify.viewmodel.SubscriptionViewModel

class SubscriptionListFragment : Fragment() {

    private var _binding: FragmentSubscriptionListBinding? = null
    private val binding get() = _binding!!

    private val subscriptionViewModel: SubscriptionViewModel by viewModels()
    private lateinit var subscriptionAdapter: SubscriptionAdapter

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

        binding.fabAddSubscription.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_subscriptions_to_addSubscriptionFragment)
        }

        setupRecyclerView()
        observeSubscriptions()
    }

    private fun setupRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter { subscription ->
            val action = SubscriptionListFragmentDirections.actionNavigationSubscriptionsToSubscriptionDetailFragment(
                subscription.subscriptionId
            )
            findNavController().navigate(action)
        }

        binding.recyclerViewSubscriptions.apply {
            adapter = subscriptionAdapter
            layoutManager = LinearLayoutManager(requireContext())

            clipToPadding = false
            val bottomPadding = getNavigationBarHeight()
            setPadding(paddingLeft, paddingTop, paddingRight, bottomPadding)
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
            subscriptionAdapter.submitList(subscriptions)

            if (subscriptions.isEmpty()) {
                binding.textViewEmpty.visibility = View.VISIBLE
                binding.recyclerViewSubscriptions.visibility = View.GONE
            } else {
                binding.textViewEmpty.visibility = View.GONE
                binding.recyclerViewSubscriptions.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}