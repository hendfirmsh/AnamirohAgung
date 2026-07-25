package com.agunganamiroh.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.agunganamiroh.motion.NavigationMotion
import com.agunganamiroh.ui.screen.auth.LoginScreen
import com.agunganamiroh.ui.screen.admin.AdminDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentDashboardScreen
import com.agunganamiroh.ui.screen.agent.AgentHistoryScreen
import com.agunganamiroh.ui.screen.agent.AgentMainScaffold
import com.agunganamiroh.ui.screen.agent.AgentProfileScreen
import com.agunganamiroh.ui.screen.account.AccountCenterScreen
import com.agunganamiroh.ui.screen.notification.NotificationCenterScreen
import com.agunganamiroh.ui.screen.agent.InputJamaahScreen
import com.agunganamiroh.ui.screen.agent.DataJamaahScreen
import com.agunganamiroh.ui.screen.agent.DetailJamaahScreen
import com.agunganamiroh.ui.screen.payment.PaymentScreen
import com.agunganamiroh.ui.screen.payment.PaymentDetailScreen
import com.agunganamiroh.ui.screen.invoice.AgentInvoiceListScreen
import com.agunganamiroh.ui.screen.invoice.AgentInvoiceDetailScreen
import com.agunganamiroh.ui.screen.invoice.AdminInvoiceListScreen
import com.agunganamiroh.ui.screen.invoice.AdminCreateInvoiceScreen
import com.agunganamiroh.ui.screen.keberangkatan.KeberangkatanScreen
import com.agunganamiroh.ui.screen.keberangkatan.detail.DepartureDetailScreen
import com.agunganamiroh.ui.screen.laporan.LaporanScreen
import com.agunganamiroh.ui.screen.paket.PackageCatalogScreen
import com.agunganamiroh.ui.screen.paket.PackageDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph() {

    val navController =
        rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        NavHost(
            navController = navController,
            startDestination = "login"
        ) {

        composable(
            route = "login",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {

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

        composable(
            route = "admin_dashboard",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AdminDashboardScreen(navController = navController)
        }

        composable(
            route = "agent_dashboard",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AgentDashboardScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.AgentMain.route,
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AgentMainScaffold(navController = navController)
        }

        composable(
            route = Screen.AgentHistory.route,
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AgentHistoryScreen(navController = navController)
        }

        composable(
            route = Screen.AgentProfile.route,
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AgentProfileScreen(navController = navController)
        }

        composable(
            route = "account_center",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AccountCenterScreen(
                navController = navController
            )
        }

        composable(
            route = "input_jamaah?id={id}&paketId={paketId}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("paketId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            InputJamaahScreen(
                navController = navController
            )
        }

        composable(
            route = "data_jamaah",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            DataJamaahScreen(
                navController = navController
            )
        }

        composable(
            route = "detail_jamaah/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailJamaahScreen(
                navController = navController,
                jamaahId = id
            )
        }

        composable(
            route = "pembayaran",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            PaymentScreen(navController = navController)
        }

        composable(
            route = "payment_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PaymentDetailScreen(navController = navController, jamaahId = id)
        }

        composable(
            route = "agent_invoice_list",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AgentInvoiceListScreen(navController = navController)
        }

        composable(
            route = "agent_invoice_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AgentInvoiceDetailScreen(navController = navController, invoiceId = id)
        }

        composable(
            route = "admin_invoice_list",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            AdminInvoiceListScreen(navController = navController)
        }

        composable(
            route = "admin_create_invoice/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AdminCreateInvoiceScreen(navController = navController, jamaahId = id)
        }

        composable(
            route = "package_catalog",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            PackageCatalogScreen(navController = navController)
        }

        composable(
            route = "package_detail/{paketId}",
            arguments = listOf(navArgument("paketId") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            PackageDetailScreen(navController = navController)
        }

        composable(
            route = "keberangkatan",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            KeberangkatanScreen(navController = navController)
        }

        composable(
            route = "departure_detail/{paketId}",
            arguments = listOf(navArgument("paketId") { type = NavType.StringType }),
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) { backStackEntry ->
            val paketId = backStackEntry.arguments?.getString("paketId") ?: ""
            DepartureDetailScreen(
                navController = navController,
                paketId = paketId
            )
        }

        composable(
            route = "laporan",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            val authViewModel: com.agunganamiroh.viewmodel.AuthViewModel = viewModel()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()
            LaporanScreen(
                navController = navController,
                agentEmail = authState.user?.email ?: "",
                agentName = authState.user?.companyName ?: ""
            )
        }

        composable(
            route = "notification_center",
            enterTransition = NavigationMotion.enterTransition,
            exitTransition = NavigationMotion.exitTransition,
            popEnterTransition = NavigationMotion.popEnterTransition,
            popExitTransition = NavigationMotion.popExitTransition
        ) {
            NotificationCenterScreen(navController = navController)
        }
    }
    } // Box
} // NavGraph
