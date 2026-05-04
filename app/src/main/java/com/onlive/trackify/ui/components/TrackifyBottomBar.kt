package com.onlive.trackify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.R
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.theme.TrackifyTheme
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.stringResource

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

@Composable
fun TrackifyBottomBar(
    navController: NavController,
    currentRoute: String,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Spacer(modifier = Modifier.width(12.dp))

        val navItems = listOf(
            NavItem(Icons.Filled.Home, stringResource(R.string.title_subscriptions), Screen.Home.route),
            NavItem(Icons.Filled.AttachMoney, stringResource(R.string.title_payments), Screen.Payments.route),
            NavItem(Icons.AutoMirrored.Filled.ShowChart, stringResource(R.string.title_statistics), Screen.Statistics.route),
            NavItem(Icons.Filled.Settings, stringResource(R.string.title_settings), Screen.Settings.route)
        )

        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon, 
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                },
                alwaysShowLabel = false
            )
        }

        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun TrackifyBottomBarPreview() {
    val context = LocalContext.current
    CompositionLocalProvider(LocalLocalizedContext provides context) {
        TrackifyTheme {
            Box(modifier = Modifier.padding(16.dp)) {
                TrackifyBottomBar(
                    navController = rememberNavController(),
                    currentRoute = Screen.Home.route
                )
            }
        }
    }
}
