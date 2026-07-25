package com.agunganamiroh.data.model

import com.google.firebase.Timestamp

data class Activity(
    val id: String = "",
    val type: ActivityType = ActivityType.REGISTRATION,
    val title: String = "",
    val subtitle: String = "",
    val status: String = "pending",
    val timestamp: Timestamp = Timestamp.now()
)

enum class ActivityType(val displayName: String) {
    REGISTRATION("Registrasi Jamaah"),
    PAYMENT("Pembayaran"),
    STATUS_UPDATE("Update Status"),
    PROFILE_UPDATE("Update Profil"),
    OTHER("Lainnya")
}
