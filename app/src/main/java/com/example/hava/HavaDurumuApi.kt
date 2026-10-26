package com.example.hava

import retrofit2.http.GET
import retrofit2.http.Query

interface HavaDurumuApi {
    @GET("data/2.5/weather")
    suspend fun havaDurumunuGetir(
        @Query("q") sehirIsmi: String,
        @Query("appid") apiAnahtari: String,
        @Query("units") birim: String = "metric",
        @Query("lang") dil: String = "tr"
    ): HavaDurumuYaniti
}
