package com.onlive.trackify

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.color.DynamicColors
import com.onlive.trackify.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    private var navGraphIds = listOf(
        R.id.navigation_subscriptions,
        R.id.navigation_payments,
        R.id.navigation_statistics,
        R.id.navigation_settings
    )

    private var currentTabId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(true)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val navView: BottomNavigationView = binding.navView

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_subscriptions,
                R.id.navigation_payments,
                R.id.navigation_statistics,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        setupBottomNavigation(navView)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = destination.label
        }
    }

    private fun setupBottomNavigation(navView: BottomNavigationView) {
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            if (item.itemId == navView.selectedItemId) {
                val backStackEntryCount = navController.currentBackStack.value.size

                if (backStackEntryCount > 2) {
                    navigateToTabStart(item.itemId)
                    return@setOnItemSelectedListener true
                }
            }

            when (item.itemId) {
                R.id.navigation_subscriptions -> {
                    currentTabId = 0
                    if (isNotRootDestination(R.id.navigation_subscriptions)) {
                        navigateToTabStart(R.id.navigation_subscriptions)
                    }
                }
                R.id.navigation_payments -> {
                    currentTabId = 1
                    if (isNotRootDestination(R.id.navigation_payments)) {
                        navigateToTabStart(R.id.navigation_payments)
                    }
                }
                R.id.navigation_statistics -> {
                    currentTabId = 2
                    if (isNotRootDestination(R.id.navigation_statistics)) {
                        navigateToTabStart(R.id.navigation_statistics)
                    }
                }
                R.id.navigation_settings -> {
                    currentTabId = 3
                    if (isNotRootDestination(R.id.navigation_settings)) {
                        navigateToTabStart(R.id.navigation_settings)
                    }
                }
            }
            true
        }
    }

    private fun isNotRootDestination(destinationId: Int): Boolean {
        val currentDestination = navController.currentDestination?.id ?: return false

        return when (destinationId) {
            R.id.navigation_subscriptions -> currentDestination != R.id.navigation_subscriptions
            R.id.navigation_payments -> currentDestination != R.id.navigation_payments
            R.id.navigation_statistics -> currentDestination != R.id.navigation_statistics
            R.id.navigation_settings -> currentDestination != R.id.navigation_settings
            else -> false
        }
    }

    private fun navigateToTabStart(destinationId: Int) {
        navController.popBackStack(destinationId, false)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}