package com.agunganamiroh.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val companyName: String = "",
    val branch: String = "",
    val role: String = "agent",
    val photoUrl: String = "",
    val address: String = "",
    val agentCode: String = "",
    val accountStatus: String = "active",
    val notificationPrefs: Map<String, Boolean> = emptyMap(),
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null
)
