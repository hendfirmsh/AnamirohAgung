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
    val createdAt: Timestamp? = null,
    val lastLogin: Timestamp? = null
)