package com.agunganamiroh.data.model

data class Kwitansi(

    val id: String = "",

    val jamaahId: String = "",

    val noInvoice: String = "",

    val tanggalInvoice: String = "",

    val subtotal: Long = 0,

    val totalTagihan: Long = 0,

    val dp: Long = 0,

    val upgradeDouble: Long = 0,

    val upgradeTriple: Long = 0,

    val koper: Long = 0,

    val paspor: Long = 0,

    val vaksin: Long = 0
)