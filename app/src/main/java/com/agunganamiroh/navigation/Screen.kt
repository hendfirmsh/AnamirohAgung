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

    object AgentInvoiceList :
        Screen("agent_invoice_list")

    object AgentInvoiceDetail :
        Screen("agent_invoice_detail/{id}") {
        fun createRoute(id: String) = "agent_invoice_detail/$id"
    }

    object AdminInvoiceList :
        Screen("admin_invoice_list")

    object AdminCreateInvoice :
        Screen("admin_create_invoice/{id}") {
        fun createRoute(id: String) = "admin_create_invoice/$id"
    }

    object AccountCenter :
        Screen("account_center")

    object NotificationCenter :
        Screen("notification_center")

}