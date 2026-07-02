package com.karigar.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.karigar.app.navigation.Routes

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainScreen(onOpenCategory: (String) -> Unit, onLogout: () -> Unit) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab(Routes.TAB_HOME, "Home", Icons.Filled.Home),
        Tab(Routes.TAB_ORDERS, "Orders", Icons.AutoMirrored.Filled.ReceiptLong),
        Tab(Routes.TAB_HISTORY, "History", Icons.Filled.History),
        Tab(Routes.TAB_PROFILE, "Profile", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val current by nav.currentBackStackEntryAsState()
                val currentRoute = current?.destination?.route
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(Routes.TAB_HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = Routes.TAB_HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.TAB_HOME) { DashboardScreen(onSelectCategory = onOpenCategory) }
            composable(Routes.TAB_ORDERS) { OrdersScreen() }
            composable(Routes.TAB_HISTORY) { HistoryScreen() }
            composable(Routes.TAB_PROFILE) { ProfileScreen(onLogout = onLogout) }
        }
    }
}
