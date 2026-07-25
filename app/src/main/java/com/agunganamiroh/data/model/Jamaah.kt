package com.agunganamiroh.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Jamaah(
    @DocumentId
    val id: String = "",
    val nama: String = "",
    val alamat: String = "",
    val gender: String = "",
    val tempatLahir: String = "",
    val tanggalLahir: String = "",
    val binBinti: String = "",
    val program: String = "",
    val paketId: String = "",
    val keberangkatan: String = "",
    val hargaPaket: Long = 0,
    val dp: Long = 0,
    val noHp: String = "",
    val noPaspor: String = "",
    val akte: Boolean = false,
    val ktp: Boolean = false,
    val kk: Boolean = false,
    val paspor: Boolean = false,
    val meningitis: Boolean = false,
    val pelunasan: Boolean = false,
    val status: String = "pending",
    val requestTambahan: String = "",
    val input_by: String = "",
    val createdAt: Timestamp? = null
)
