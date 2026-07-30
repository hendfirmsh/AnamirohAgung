package com.agunganamiroh.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.agunganamiroh.ui.screen.riwayat.RiwayatScreen
import com.agunganamiroh.ui.screen.paket.PackageCatalogScreen
import com.agunganamiroh.ui.screen.paket.PackageDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

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

        composable(route = "login") {

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

        ) {
            AdminDashboardScreen(navController = navController)
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

        ) {
            InputJamaahScreen(
                navController = navController
            )
        }

        composable(
            route = "detail_jamaah/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),

        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            DetailJamaahScreen(
                navController = navController,
                jamaahId = id
            )
        }

        composable(
            route = "pembayaran",

        ) {
            PaymentScreen(navController = navController)
        }

        composable(
            route = "payment_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),

        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            PaymentDetailScreen(navController = navController, jamaahId = id)
        }

        composable(
            route = "agent_invoice_list",

        ) {
            AgentInvoiceListScreen(navController = navController)
        }

        composable(
            route = "agent_invoice_detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),

        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AgentInvoiceDetailScreen(navController = navController, invoiceId = id)
        }

        composable(
            route = "admin_invoice_list",

        ) {
            AdminInvoiceListScreen(navController = navController)
        }

        composable(
            route = "admin_create_invoice/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),

        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AdminCreateInvoiceScreen(navController = navController, jamaahId = id)
        }

        composable(
            route = "package_catalog",

        ) {
            PackageCatalogScreen(navController = navController)
        }

        composable(
            route = "package_detail/{paketId}",
            arguments = listOf(navArgument("paketId") { type = NavType.StringType }),

        ) {
            PackageDetailScreen(navController = navController)
        }

        composable(
            route = "keberangkatan",

        ) {
            KeberangkatanScreen(navController = navController)
        }

        composable(
            route = "departure_detail/{paketId}",
            arguments = listOf(navArgument("paketId") { type = NavType.StringType }),

        ) { backStackEntry ->
            val paketId = backStackEntry.arguments?.getString("paketId") ?: ""
            DepartureDetailScreen(
                navController = navController,
                paketId = paketId
            )
        }

        composable(
            route = "laporan",

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
            route = "riwayat",

        ) {
            RiwayatScreen(navController = navController)
        }

        composable(
            route = "notification_center",

        ) {
            NotificationCenterScreen(navController = navController)
        }
    }
}
