package com.agunganamiroh.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.agunganamiroh.ui.screen.auth.LoginScreen
import com.agunganamiroh.ui.screen.admin.AdminDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentProfileScreen
import com.agunganamiroh.ui.screen.agent.InputJamaahScreen
import com.agunganamiroh.ui.screen.agent.DataJamaahScreen
import com.agunganamiroh.ui.screen.agent.DetailJamaahScreen
import com.agunganamiroh.ui.screen.agent.AgentMainScaffold
import com.agunganamiroh.ui.screen.agent.AgentHistoryScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

private const val DURATION = 300

@Composable
fun NavGraph() {

    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = {
            fadeIn(tween(DURATION)) + slideInHorizontally(
                tween(DURATION),
                initialOffsetX = { it / 5 }
            )
        },
        exitTransition = {
            fadeOut(tween(DURATION))
        },
        popEnterTransition = {
            fadeIn(tween(DURATION))
        },
        popExitTransition = {
            fadeOut(tween(DURATION)) + slideOutHorizontally(
                tween(DURATION),
                targetOffsetX = { it / 5 }
            )
        }
    ) {

        composable("login") {

            LoginScreen(
                onLoginSuccess = { role ->

                    when(role) {

                        "admin" -> {
                            navController.navigate(
                                "admin_dashboard"
                            ) {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }

                        "agent" -> {
                            navController.navigate(
                                Screen.AgentMain.route
                            ) {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboardScreen()
        }

        composable(route = Screen.AgentMain.route) {
            AgentMainScaffold(navController = navController)
        }

        composable(route = "data_jamaah") {
            DataJamaahScreen(
                navController = navController
            )
        }

        composable(
            route = "input_jamaah?id={id}",
            arguments = listOf(navArgument("id") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) {
            InputJamaahScreen(
                navController = navController
            )
        }

        composable(
            route = "detail_jamaah/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailJamaahScreen(
                navController = navController,
                jamaahId = id
            )
        }

        composable(route = "pembayaran") {
            // Placeholder
        }

        composable(route = "invoice") {
            // Placeholder
        }

        composable(route = "riwayat") {
            // Placeholder
        }
    }
}
