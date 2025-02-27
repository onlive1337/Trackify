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

            when (destination.id) {
                R.id.navigation_subscriptions -> currentTabId = 0
                R.id.navigation_payments -> currentTabId = 1
                R.id.navigation_statistics -> currentTabId = 2
                R.id.navigation_settings -> currentTabId = 3
            }
        }
    }

    private fun setupBottomNavigation(navView: BottomNavigationView) {
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            val targetId = when (item.itemId) {
                R.id.navigation_subscriptions -> R.id.navigation_subscriptions
                R.id.navigation_payments -> R.id.navigation_payments
                R.id.navigation_statistics -> R.id.navigation_statistics
                R.id.navigation_settings -> R.id.navigation_settings
                else -> return@setOnItemSelectedListener false
            }

            val currentDestinationId = navController.currentDestination?.id ?: -1

            if (currentDestinationId != targetId && targetId == navGraphIds[currentTabId]) {
                navController.popBackStack(targetId, false)
                return@setOnItemSelectedListener true
            }

            try {
                navController.navigate(targetId)
                currentTabId = navGraphIds.indexOf(targetId)
                return@setOnItemSelectedListener true
            } catch (e: Exception) {
                e.printStackTrace()
                return@setOnItemSelectedListener false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}