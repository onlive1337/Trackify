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
        val backStack = navController.currentBackStack.value

        val destinationExists = backStack.any { it.destination.id == destinationId }

        if (destinationExists) {
            navController.popBackStack(destinationId, inclusive)
            return true
        }

        return false
    }

    fun handleBottomNavItemSelected(navController: NavController, currentTabId: Int, selectedTabId: Int): Boolean {
        if (selectedTabId == currentTabId) {
            val backStackSize = navController.currentBackStack.value.size

            if (backStackSize > 2) {
                if (!safePopBackStack(navController, selectedTabId, false)) {
                    safeNavigate(navController, selectedTabId)
                }
                return true
            }
            return false
        }

        return safeNavigate(navController, selectedTabId)
    }
}