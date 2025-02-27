package com.onlive.trackify.utils

import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavGraph

object NavigationUtils {

    private fun NavGraph.containsDestination(@IdRes destinationId: Int): Boolean {
        return findNode(destinationId) != null
    }

    fun safeNavigate(navController: NavController, @IdRes destinationId: Int): Boolean {
        return if (navController.graph.containsDestination(destinationId)) {
            navController.navigate(destinationId)
            true
        } else {
            false
        }
    }

    fun safePopBackStack(navController: NavController, @IdRes destinationId: Int, inclusive: Boolean): Boolean {
        return try {
            navController.getBackStackEntry(destinationId)
            navController.popBackStack(destinationId, inclusive)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun handleBottomNavItemSelected(navController: NavController, currentTabId: Int, selectedTabId: Int): Boolean {
        if (selectedTabId == currentTabId) {
            try {
                navController.getBackStackEntry(selectedTabId)
                if (!navController.popBackStack(selectedTabId, false)) {
                    safeNavigate(navController, selectedTabId)
                }
            } catch (e: Exception) {
                safeNavigate(navController, selectedTabId)
            }
            return true
        }

        return safeNavigate(navController, selectedTabId)
    }
}