package com.onlive.trackify.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.onlive.trackify.R
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.utils.stringResource

private data class NavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val route: Screen
)

@Composable
fun TrackifyBottomBar(
    navController: NavController,
    currentRoute: Screen,
    modifier: Modifier = Modifier
) {
    val windowWidth = with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val useHorizontalItems = windowWidth >= 600.dp

    val navItems = listOf(
        NavItem(
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            label = stringResource(R.string.title_subscriptions),
            route = Screen.Home
        ),
        NavItem(
            selectedIcon = Icons.Rounded.Payments,
            unselectedIcon = Icons.Outlined.Payments,
            label = stringResource(R.string.title_payments),
            route = Screen.Payments
        ),
        NavItem(
            selectedIcon = Icons.AutoMirrored.Rounded.ShowChart,
            unselectedIcon = Icons.AutoMirrored.Outlined.ShowChart,
            label = stringResource(R.string.title_statistics),
            route = Screen.Statistics
        ),
        NavItem(
            selectedIcon = Icons.Rounded.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            label = stringResource(R.string.title_settings),
            route = Screen.Settings
        )
    )

    ShortNavigationBar(
        modifier = modifier.layout { measurable, constraints ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, 0)
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        arrangement = if (useHorizontalItems) {
            ShortNavigationBarArrangement.Centered
        } else {
            ShortNavigationBarArrangement.EqualWeight
        }
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route
            ShortNavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo<Screen.Home> {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                iconPosition = if (useHorizontalItems) {
                    NavigationItemIconPosition.Start
                } else {
                    NavigationItemIconPosition.Top
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            )
        }
    }
}