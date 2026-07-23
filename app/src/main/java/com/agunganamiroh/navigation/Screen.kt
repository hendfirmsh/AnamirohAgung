package com.agunganamiroh.navigation

sealed class Screen(
    val route: String
) {

    object Login : Screen("login")

    object AdminDashboard :
        Screen("admin_dashboard")

    object AgentMain :
        Screen("agent_main")

    object AgentDashboard :
        Screen("agent_dashboard")

    object AgentHistory :
        Screen("agent_history")

    object AgentProfile :
        Screen("agent_profile")

    object DataJamaah :
        Screen("data_jamaah")

    object DetailJamaah :
        Screen("detail_jamaah/{id}") {
        fun createRoute(id: String) = "detail_jamaah/$id"
    }

}