package com.karigar.worker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.karigar.worker.data.TokenStore

private data class Tab(val label: String, val icon: ImageVector)

@Composable
fun WorkerMainScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val bearer = remember { "Bearer " + (TokenStore(context).getToken() ?: "") }
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        Tab("Home", Icons.Filled.Home),
        Tab("Stats", Icons.Filled.BarChart),
        Tab("History", Icons.Filled.History),
        Tab("Profile", Icons.Filled.Person)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEachIndexed { i, tab ->
                    NavigationBarItem(
                        selected = selected == i,
                        onClick = { selected = i },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selected) {
                0 -> WorkerHomeScreen(onLogout = onLogout)
                1 -> WorkerStatsScreen(bearer)
                2 -> WorkerHistoryScreen(bearer)
                else -> WorkerProfileScreen(bearer, onLogout)
            }
        }
    }
}
