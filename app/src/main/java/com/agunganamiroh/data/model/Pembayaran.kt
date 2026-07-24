package com.agunganamiroh.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Pembayaran(
    @DocumentId
    val id: String = "",
    val jamaahId: String = "",
    val agentEmail: String = "",
    val nominal: Long = 0,
    val tanggal: String = "",
    val metode: String = "",
    val catatan: String = "",
    val createdAt: Timestamp? = null
)
