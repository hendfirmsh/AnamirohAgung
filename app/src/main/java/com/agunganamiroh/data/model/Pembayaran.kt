package com.agunganamiroh.data.model

data class Pembayaran(
    val id: String = "",

    val jamaahId: String = "",

    val jumlah: Long = 0,

    val tanggal: String = "",

    val keterangan: String = "",

    val status: String = "pending",

    val invoiceNumber: String = "",

    val dibuatOleh: String = ""
)