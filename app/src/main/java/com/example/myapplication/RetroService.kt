package com.example.myapplication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface RetroService {

    @POST("historique")
    @Headers("Content-Type: application/json")
    fun ajouterTemperature(@Body params: Temperature): Call<TemperatureResponse>
}