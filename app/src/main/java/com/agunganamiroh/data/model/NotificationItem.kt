package com.agunganamiroh.data.model

enum class NotificationCategory(val displayName: String) {
    PAYMENT("Pembayaran"),
    INVOICE("Invoice"),
    JAMAAH("Jamaah"),
    SYSTEM("Sistem"),
    ANNOUNCEMENT("Pengumuman"),
    SECURITY("Keamanan")
}

enum class NotificationPriority {
    HIGH, NORMAL, LOW
}

data class NotificationItem(
    val id: String,
    val type: String,
    val category: NotificationCategory,
    val priority: NotificationPriority,
    val title: String,
    val description: String,
    val timestamp: Long,
    val isRead: Boolean,
    val jamaahId: String = "",
    val jamaahName: String = "",
    val paymentId: String = "",
    val invoiceId: String = "",
    val amount: Long = 0,
    val status: String = ""
)
