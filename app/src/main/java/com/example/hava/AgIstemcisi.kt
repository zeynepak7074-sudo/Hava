package com.example.hava

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AgIstemcisi {
    private const val TEMEL_URL = "https://api.openweathermap.org/"

    val api: HavaDurumuApi by lazy {
        Retrofit.Builder()
            .baseUrl(TEMEL_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HavaDurumuApi::class.java)
    }
}