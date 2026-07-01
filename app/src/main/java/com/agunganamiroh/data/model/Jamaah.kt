package com.agunganamiroh.data.model

data class Jamaah(
    val id: String = "",

    val nama: String = "",
    val alamat: String = "",
    val noHp: String = "",

    val gender: String = "",
    val binBinti: String = "",

    val tempatLahir: String = "",
    val tanggalLahir: String = "",

    val noPaspor: String = "",

    val program: String = "",
    val keberangkatan: String = "",

    val dp: Long = 0,

    val inputBy: String = "",

    val status: String = "pending",

    val ktp: Boolean = false,
    val kk: Boolean = false,
    val akte: Boolean = false,
    val meningitis: Boolean = false
)