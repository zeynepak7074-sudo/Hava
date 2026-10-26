package com.example.hava

import com.google.gson.annotations.SerializedName

data class HavaDurumuYaniti(
    @SerializedName("main") val anaVeri: AnaVeri,
    @SerializedName("weather") val havaDurumuBilgisi: List<HavaBilgisi>,
    @SerializedName("name") val sehirAdi: String
)

data class AnaVeri(
    @SerializedName("temp") val sicaklik: Double

)

data class HavaBilgisi(
    @SerializedName("description") val aciklama: String
)