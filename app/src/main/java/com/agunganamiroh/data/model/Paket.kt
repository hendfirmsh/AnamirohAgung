package com.agunganamiroh.data.model

data class Paket(
    val id: String = "",
    val title: String = "",
    val tanggal: String = "",
    val durasi: String = "",
    val hotelMakkah: String = "",
    val hotelMadinah: String = "",
    val maskapai: String = "",
    val harga: Long = 0,
    val sisaSeat: Long = 0,
    val brosurName: String = ""
)