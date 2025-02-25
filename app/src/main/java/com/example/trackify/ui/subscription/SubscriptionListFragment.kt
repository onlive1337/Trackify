package com.example.trackify.ui.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackify.R
import com.example.trackify.databinding.FragmentSubscriptionListBinding
import com.example.trackify.viewmodel.SubscriptionViewModel

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

        // Настройка кнопки добавления подписки
        binding.fabAddSubscription.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_subscriptions_to_addSubscriptionFragment)
        }

        setupRecyclerView()
        observeSubscriptions()
    }

    private fun setupRecyclerView() {
        subscriptionAdapter = SubscriptionAdapter { subscription ->
            // Обработка нажатия на подписку - переход к деталям
            val action = SubscriptionListFragmentDirections.actionNavigationSubscriptionsToSubscriptionDetailFragment(
                subscription.subscriptionId
            )
            findNavController().navigate(action)
        }

        binding.recyclerViewSubscriptions.apply {
            adapter = subscriptionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeSubscriptions() {
        // Подписка на изменения данных
        subscriptionViewModel.allActiveSubscriptions.observe(viewLifecycleOwner) { subscriptions ->
            // Обновление UI при изменении данных
            subscriptionAdapter.submitList(subscriptions)

            // Показываем сообщение, если список пуст
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