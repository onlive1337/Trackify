package com.onlive.trackify

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.color.DynamicColors
import com.onlive.trackify.databinding.ActivityMainBinding
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils

class MainActivity : AppCompatActivity(), PreferenceManager.OnPreferenceChangedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var preferenceManager: PreferenceManager
    private var currentNavItemId: Int = R.id.navigation_subscriptions

    override fun attachBaseContext(newBase: Context) {
        preferenceManager = PreferenceManager(newBase)
        val context = LocaleHelper.getLocalizedContext(newBase, preferenceManager)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivityIfAvailable(this)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_subscriptions,
                R.id.navigation_payments,
                R.id.navigation_statistics,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val itemAnimation = AnimationUtils.loadAnimation(this, R.anim.bottom_nav_item_animation)

        binding.navView.setOnItemSelectedListener { item ->
            val itemId = item.itemId

            val menuView = binding.navView.findViewById<View>(itemId)
            menuView?.startAnimation(itemAnimation)

            val navContainer = binding.bottomNavContainer

            val scaleDown = AnimatorSet().apply {
                val scaleDownX = ObjectAnimator.ofFloat(navContainer, View.SCALE_X, 1f, 0.97f)
                val scaleDownY = ObjectAnimator.ofFloat(navContainer, View.SCALE_Y, 1f, 0.97f)
                playTogether(scaleDownX, scaleDownY)
                duration = 100
            }

            val scaleUp = AnimatorSet().apply {
                val scaleUpX = ObjectAnimator.ofFloat(navContainer, View.SCALE_X, 0.97f, 1.015f)
                val scaleUpY = ObjectAnimator.ofFloat(navContainer, View.SCALE_Y, 0.97f, 1.015f)
                playTogether(scaleUpX, scaleUpY)
                duration = 100
            }

            val scaleNormal = AnimatorSet().apply {
                val scaleNormalX = ObjectAnimator.ofFloat(navContainer, View.SCALE_X, 1.015f, 1f)
                val scaleNormalY = ObjectAnimator.ofFloat(navContainer, View.SCALE_Y, 1.015f, 1f)
                playTogether(scaleNormalX, scaleNormalY)
                duration = 100
            }

            AnimatorSet().apply {
                playSequentially(scaleDown, scaleUp, scaleNormal)
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }

            if (itemId == currentNavItemId) {
                when (itemId) {
                    R.id.navigation_subscriptions -> navController.popBackStack(R.id.navigation_subscriptions, false)
                    R.id.navigation_payments -> navController.popBackStack(R.id.navigation_payments, false)
                    R.id.navigation_statistics -> navController.popBackStack(R.id.navigation_statistics, false)
                    R.id.navigation_settings -> navController.popBackStack(R.id.navigation_settings, false)
                }
            } else {
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .build()
                navController.navigate(itemId, null, navOptions)
                currentNavItemId = itemId
            }

            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = destination.label
            if (appBarConfiguration.topLevelDestinations.contains(destination.id)) {
                currentNavItemId = destination.id
                binding.navView.menu.findItem(currentNavItemId)?.isChecked = true
            }
        }

        preferenceManager.addOnPreferenceChangedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onPreferenceChanged(key: String, value: Any?) {
        if (key == "language_code") {
            recreate()
        }
    }

    override fun onDestroy() {
        preferenceManager.removeOnPreferenceChangedListener(this)
        super.onDestroy()
    }

    fun setupRecyclerViewBottomPadding(recyclerView: RecyclerView) {
        recyclerView.clipToPadding = false

        val bottomNavHeight = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val extraPadding = resources.getDimensionPixelSize(R.dimen.floating_nav_extra_padding)
        val totalPadding = bottomNavHeight + extraPadding

        recyclerView.setPadding(
            recyclerView.paddingLeft,
            recyclerView.paddingTop,
            recyclerView.paddingRight,
            totalPadding
        )
    }
}