package com.agunganamiroh.navigation

sealed class Screen(
    val route: String
) {

    object Login : Screen("login")

    object AdminDashboard :
        Screen("admin_dashboard")

    object AgentDashboard :
        Screen("agent_dashboard")

}