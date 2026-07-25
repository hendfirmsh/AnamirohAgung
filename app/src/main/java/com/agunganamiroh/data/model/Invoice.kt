package com.agunganamiroh.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Invoice(
    @DocumentId
    val id: String = "",
    val noInvoice: String = "",
    val jamaahId: String = "",
    val jamaahName: String = "",
    val agentEmail: String = "",
    val agentName: String = "",
    val createdBy: String = "",
    val createdByEmail: String = "",
    val totalTagihan: Long = 0,
    val subtotal: Long = 0,
    val upgradeDouble: Long = 0,
    val upgradeTriple: Long = 0,
    val koper: Long = 0,
    val paspor: Long = 0,
    val vaksin: Long = 0,
    val diskon: Long = 0,
    val biayaLain: Long = 0,
    val keteranganBiayaLain: String = "",
    val status: String = "published",
    val totalDibayar: Long = 0,
    val tanggalInvoice: String = "",
    val tanggalJatuhTempo: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val paidAt: Timestamp? = null
)
