package com.agunganamiroh.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.agunganamiroh.ui.screen.auth.LoginScreen
import com.agunganamiroh.ui.screen.admin.AdminDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentProfileScreen
import com.agunganamiroh.ui.screen.agent.InputJamaahScreen
import com.agunganamiroh.ui.screen.agent.DataJamaahScreen
import com.agunganamiroh.ui.screen.agent.DetailJamaahScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph() {

    val navController =
        rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
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
                                "agent_dashboard"
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

        composable(route = "agent_dashboard") {
            AgentDashboardScreen(
                navController = navController
            )
        }

        composable(route = "profil_agent") {
            AgentProfileScreen(
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

        composable(route = "data_jamaah") {
            DataJamaahScreen(
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