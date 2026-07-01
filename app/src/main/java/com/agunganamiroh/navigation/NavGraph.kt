package com.agunganamiroh.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.agunganamiroh.ui.screen.auth.LoginScreen
import com.agunganamiroh.ui.screen.admin.AdminDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentDashboardScreen

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
    }
}