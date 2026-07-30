package com.agunganamiroh.ui.screen.agent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agunganamiroh.navigation.Screen
import com.agunganamiroh.ui.screen.account.AccountCenterScreen

private const val BAR_HIDE_THRESHOLD = 5f
private const val BAR_ANIM_DURATION = 250

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
            route = Screen.JamaahInput.route,
            label = "Tambah Jamaah",
            icon = Icons.Outlined.AddCircleOutline,
            selectedIcon = Icons.Outlined.AddCircleOutline
        ),
        TabItem(
            route = Screen.AccountCenter.route,
            label = "Profil",
            icon = Icons.Default.Person,
            selectedIcon = Icons.Default.Person
        )
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showBottomBar by remember { mutableStateOf(true) }

    val currentShowBottomBar by rememberUpdatedState(showBottomBar)

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    animationSpec = tween(BAR_ANIM_DURATION),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(BAR_ANIM_DURATION),
                    targetOffsetY = { it }
                )
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
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
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTabIndex) {
                0 -> AgentDashboardScreen(navController = navController)
                1 -> InputJamaahScreen(navController = navController)
                2 -> AccountCenterScreen(navController = navController)
                else -> AgentDashboardScreen(navController = navController)
            }
        }
    }
}

private data class TabItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
)
