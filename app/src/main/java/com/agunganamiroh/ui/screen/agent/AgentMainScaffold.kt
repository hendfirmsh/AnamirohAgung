package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agunganamiroh.navigation.Screen

@Composable
fun AgentMainScaffold(
    navController: NavController
) {
    val tabs = listOf(
        TabItem(
            route = Screen.AgentDashboard.route,
            label = "Beranda",
            icon = Icons.Default.Home,
            selectedIcon = Icons.Default.Home
        ),
        TabItem(
            route = Screen.AgentHistory.route,
            label = "Riwayat",
            icon = Icons.Default.History,
            selectedIcon = Icons.Default.History
        ),
        TabItem(
            route = Screen.AgentProfile.route,
            label = "Profil",
            icon = Icons.Default.Person,
            selectedIcon = Icons.Default.Person
        )
    )

    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedTabIndex == index) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label,
                                tint = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> AgentDashboardScreen(navController = navController)
            1 -> AgentHistoryScreen(navController = navController)
            2 -> AgentProfileScreen(navController = navController)
            else -> AgentDashboardScreen(navController = navController)
        }
    }
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
)