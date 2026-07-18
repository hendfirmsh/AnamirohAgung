package com.agunganamiroh.navigation

sealed class Screen(
    val route: String
) {

    object Login : Screen("login")

    object AdminDashboard :
        Screen("admin_dashboard")

    object AgentDashboard :
        Screen("agent_dashboard")

    object DataJamaah :
        Screen("data_jamaah")

    object DetailJamaah :
        Screen("detail_jamaah/{id}") {
        fun createRoute(id: String) = "detail_jamaah/$id"
    }

}