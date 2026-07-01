package com.agunganamiroh.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val companyName: String = "",
    val role: String = "agent",
    val createdAt: Long = 0L
)